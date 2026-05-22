package com.example.data.repository

import android.util.Log
import com.example.data.local.HistoryLogDao
import com.example.data.local.MemberDao
import com.example.data.local.TrashStateDao
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import com.example.data.remote.EmailSender
import com.example.data.remote.FirebaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TrashRepository(
    private val memberDao: MemberDao,
    private val trashStateDao: TrashStateDao,
    private val historyLogDao: HistoryLogDao
) {
    private val firebaseClient = FirebaseClient()
    private val emailSender = EmailSender()

    val membersFlow: Flow<List<Member>> = memberDao.getAllMembersFlow()
    val trashStateFlow: Flow<TrashState?> = trashStateDao.getTrashStateFlow()
    val recentLogsFlow: Flow<List<HistoryLog>> = historyLogDao.getRecentLogsFlow()

    // Initialize default dormitory data representing 7 roommates if empty
    suspend fun initDefaultData() {
        val existingMembers = memberDao.getAllMembers()
        if (existingMembers.isEmpty()) {
            val defaults = listOf(
                Member(1, "Thành viên 1", "member1@example.com"),
                Member(2, "Thành viên 2", "member2@example.com"),
                Member(3, "Thành viên 3", "member3@example.com"),
                Member(4, "Thành viên 4", "member4@example.com"),
                Member(5, "Thành viên 5", "member5@example.com"),
                Member(6, "Thành viên 6", "member6@example.com"),
                Member(7, "Thành viên 7", "member7@example.com")
            )
            memberDao.insertMembers(defaults)
            Log.d("TrashRepository", "Inserted 7 default members")
        }

        val existingState = trashStateDao.getTrashState()
        if (existingState == null) {
            val defaultState = TrashState(id = 1, currentTurnIndex = 0)
            trashStateDao.insertTrashState(defaultState)
            Log.d("TrashRepository", "Inserted default TrashState")
        }

        // Add a system initialization log if empty
        val logs = historyLogDao.getRecentLogsFlow().firstOrNull()
        if (logs.isNullOrEmpty()) {
            historyLogDao.insertLog(
                HistoryLog(
                    message = "Hệ thống quản lý lịch đổ rác KTX 7 người khởi động thành công!",
                    type = "SYSTEM"
                )
            )
        }
    }

    suspend fun updateMember(member: Member) {
        memberDao.insertMember(member)
        // Push state up to Firebase to keep settings synced
        syncToFirebase()
    }

    suspend fun updateMembers(members: List<Member>) {
        memberDao.insertMembers(members)
        // Push state up to Firebase to keep settings synced
        syncToFirebase()
    }

    suspend fun updateTrashStateOnly(state: TrashState) {
        trashStateDao.insertTrashState(state)
        syncToFirebase()
    }

    // Mark trash as FULL and send email notification to the person on turn
    suspend fun reportTrashFull(reporterName: String): Pair<Boolean, String?> {
        val currentState = trashStateDao.getTrashState() ?: return Pair(false, "Không tìm thấy trạng thái rác.")
        if (currentState.isTrashFull) {
            return Pair(false, "Thùng rác đã được báo đầy từ trước rồi!")
        }

        val members = memberDao.getAllMembers()
        if (members.isEmpty()) {
            return Pair(false, "Không tìm thấy danh sách thành viên.")
        }

        val currentTurnMember = members.getOrNull(currentState.currentTurnIndex)
            ?: return Pair(false, "Thành viên hiện tại không hợp lệ.")

        // Update state locally
        val updatedState = currentState.copy(
            isTrashFull = true,
            reportedByName = reporterName,
            reportedAt = System.currentTimeMillis()
        )
        trashStateDao.insertTrashState(updatedState)

        // Log to history
        val logMsg = "$reporterName đã báo rác đầy! Đến lượt ${currentTurnMember.name} (T${currentTurnMember.id}) đi đổ."
        historyLogDao.insertLog(HistoryLog(message = logMsg, type = "FULL"))

        // Sync to Firebase
        syncToFirebase(updatedState, members)

        // Try background email sending with Resend if configured
        var emailSentMessage = "Báo rác đầy thành công!"
        if (updatedState.resendApiKey.isNotBlank() && currentTurnMember.email.isNotBlank()) {
            val emailSuccess = emailSender.sendEmailViaResend(
                apiKey = updatedState.resendApiKey,
                toEmail = currentTurnMember.email,
                recipientName = currentTurnMember.name,
                sequenceId = currentTurnMember.id,
                webConfirmUrl = updatedState.webConfirmUrl,
                firebaseDbUrl = updatedState.firebaseDbUrl,
                firebaseApiKey = updatedState.firebaseApiKey
            )
            emailSentMessage = if (emailSuccess) {
                "Báo rác đầy thành công và đã tự động gửi email nhắc nhở tới ${currentTurnMember.name}!"
            } else {
                "Đã báo rác đầy nhưng không gửi được email qua Resend. Vui lòng gửi bằng app mail!"
            }
        } else {
            emailSentMessage = "Báo rác đầy thành công! Sử dụng Mail Client để gửi nhắc nhở."
        }

        return Pair(true, emailSentMessage)
    }

    // Confirm that trash is dumped, advancing the sequence (0-6)
    suspend fun confirmTrashDumped(dumperName: String): Boolean {
        val currentState = trashStateDao.getTrashState() ?: return false
        val members = memberDao.getAllMembers()
        if (members.isEmpty()) return false

        val previousIndex = currentState.currentTurnIndex
        val previousMember = members.getOrNull(previousIndex)

        // Next Index (cycle 1-7 in index 0-6)
        val nextIndex = (previousIndex + 1) * 1 % 7 // Or simple (previousIndex + 1) % 7
        val nextMember = members.getOrNull(nextIndex)

        // Update state locally
        val updatedState = currentState.copy(
            currentTurnIndex = nextIndex,
            isTrashFull = false,
            reportedByName = "",
            reportedAt = 0L
        )
        trashStateDao.insertTrashState(updatedState)

        // Log to history
        val previousName = previousMember?.name ?: "Thành viên $previousIndex"
        val nextName = nextMember?.name ?: "Thành viên $nextIndex"
        val logMsg = "$dumperName đã hoàn thành đổ rác! Lượt tiếp theo chuyển giao từ $previousName sang $nextName."
        historyLogDao.insertLog(HistoryLog(message = logMsg, type = "DUMPED"))

        // Sync to Firebase
        syncToFirebase(updatedState, members)
        return true
    }

    // Change settings for Firebase config
    suspend fun updateFirebaseSettings(url: String, secret: String, projectId: String) {
        val currentState = trashStateDao.getTrashState() ?: TrashState(id = 1)
        val updatedState = currentState.copy(
            firebaseDbUrl = url,
            firebaseApiKey = secret,
            firebaseProjectId = projectId
        )
        trashStateDao.insertTrashState(updatedState)
        historyLogDao.insertLog(HistoryLog(message = "Đã cập nhật thông tin đồng bộ Firebase.", type = "CONFIG"))
        
        // Try immediate sync to establish database collection
        syncToFirebase(updatedState, memberDao.getAllMembers())
    }

    // Change settings for Resend key
    suspend fun updateResendSettings(apiKey: String, webConfirmUrl: String) {
        val currentState = trashStateDao.getTrashState() ?: TrashState(id = 1)
        val updatedState = currentState.copy(
            resendApiKey = apiKey,
            webConfirmUrl = webConfirmUrl
        )
        trashStateDao.insertTrashState(updatedState)
        historyLogDao.insertLog(HistoryLog(message = "Đã cấu hình lại dịch vụ gửi mail tự động Resend và link Xác nhận nhanh.", type = "CONFIG"))
    }

    // Manually push current state to Firebase
    suspend fun syncToFirebase(): Boolean {
        val state = trashStateDao.getTrashState() ?: return false
        val members = memberDao.getAllMembers()
        return syncToFirebase(state, members)
    }

    private suspend fun syncToFirebase(state: TrashState, members: List<Member>): Boolean {
        if (state.firebaseDbUrl.isNotBlank()) {
            val success = firebaseClient.syncToFirebase(
                dbUrl = state.firebaseDbUrl,
                apiKey = state.firebaseApiKey,
                state = state,
                members = members
            )
            if (success) {
                // Update temporary sync feedback locally
                val latest = trashStateDao.getTrashState() ?: state
                trashStateDao.insertTrashState(latest.copy(lastSyncedAt = System.currentTimeMillis()))
            }
            return success
        }
        return false
    }

    // Pull current state from Firebase and overwrite local state if new
    suspend fun fetchFromFirebase(): Boolean {
        val state = trashStateDao.getTrashState() ?: return false
        if (state.firebaseDbUrl.isBlank()) return false

        val payload = firebaseClient.fetchFromFirebase(
            dbUrl = state.firebaseDbUrl,
            apiKey = state.firebaseApiKey
        )

        if (payload != null) {
            // Overwrite Room database
            val updatedState = state.copy(
                currentTurnIndex = payload.currentTurnIndex,
                isTrashFull = payload.isTrashFull,
                reportedByName = payload.reportedByName,
                reportedAt = payload.reportedAt,
                lastSyncedAt = System.currentTimeMillis()
            )
            trashStateDao.insertTrashState(updatedState)

            // Overwrite members if they were customized remotely
            val remoteMembers = payload.members.map { Member(it.id, it.name, it.email) }
            if (remoteMembers.isNotEmpty()) {
                memberDao.insertMembers(remoteMembers)
            }

            historyLogDao.insertLog(HistoryLog(message = "Đồng bộ hóa dữ liệu trực tuyến từ Firebase thành công!", type = "SYSTEM"))
            return true
        }
        return false
    }
}

package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.DormRoomDao
import com.example.data.local.HistoryLogDao
import com.example.data.local.MemberDao
import com.example.data.local.TrashStateDao
import com.example.data.model.DormRoom
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import com.example.data.remote.EmailSender
import com.example.data.remote.FirebaseClient
import kotlinx.coroutines.flow.Flow

class TrashRepository(
    private val dormRoomDao: DormRoomDao,
    private val memberDao: MemberDao,
    private val trashStateDao: TrashStateDao,
    private val historyLogDao: HistoryLogDao
) {
    private val firebaseClient = FirebaseClient()
    private val emailSender = EmailSender()

    val allRoomsFlow: Flow<List<DormRoom>> = dormRoomDao.getAllRoomsFlow()

    fun getMembersFlow(roomName: String): Flow<List<Member>> = memberDao.getMembersByRoomFlow(roomName)
    fun getTrashStateFlow(roomName: String): Flow<TrashState?> = trashStateDao.getTrashStateFlow(roomName)
    fun getRecentLogsFlow(roomName: String): Flow<List<HistoryLog>> = historyLogDao.getRecentLogsFlow(roomName)

    suspend fun getAllRooms(): List<DormRoom> = dormRoomDao.getAllRooms()

    // Initialize default Room & Dormitory data representing D514 if empty
    suspend fun initDefaultData() {
        val existingRooms = dormRoomDao.getAllRooms()
        if (existingRooms.isEmpty()) {
            // Register default Room D514
            registerRoom(
                roomName = "Phòng D514",
                adminName = "Hữu Hào",
                adminEmail = "nguyenhaohuu9@gmail.com",
                adminPassword = "admin999"
            )
            Log.d("TrashRepository", "Created default room D514")
        }
    }

    suspend fun registerRoom(
        roomName: String,
        adminName: String,
        adminEmail: String,
        adminPassword: String
    ): Boolean {
        val trimmedRoom = roomName.trim()
        if (trimmedRoom.isEmpty()) return false

        val existing = dormRoomDao.getRoom(trimmedRoom)
        if (existing != null) {
            return false // Already exists
        }

        val room = DormRoom(
            roomName = trimmedRoom,
            adminName = adminName.trim(),
            adminEmail = adminEmail.trim().lowercase(),
            adminPassword = adminPassword.trim()
        )
        dormRoomDao.insertRoom(room)

        // Initialize state for this room
        val state = TrashState(
            roomName = trimmedRoom,
            adminEmail = adminEmail.trim().lowercase(),
            adminPassword = adminPassword.trim()
        )
        trashStateDao.insertTrashState(state)

        // Initialize 7 default members for this room
        val defaults = listOf(
            Member(trimmedRoom, 1, "Thành viên 1", "member1@example.com"),
            Member(trimmedRoom, 2, "Thành viên 2", "member2@example.com"),
            Member(trimmedRoom, 3, "Thành viên 3", "member3@example.com"),
            Member(trimmedRoom, 4, "Thành viên 4", "member4@example.com"),
            Member(trimmedRoom, 5, "Thành viên 5", "member5@example.com"),
            Member(trimmedRoom, 6, "Thành viên 6", "member6@example.com"),
            Member(trimmedRoom, 7, "Thành viên 7", "member7@example.com")
        )
        memberDao.insertMembers(defaults)

        // Initialize log
        historyLogDao.insertLog(
            HistoryLog(
                roomName = trimmedRoom,
                message = "Phòng $trimmedRoom đã được đăng ký thành công bởi Admin ${adminName.trim()}!",
                type = "SYSTEM"
            )
        )
        return true
    }

    suspend fun updateMember(member: Member) {
        memberDao.insertMember(member)
        syncToFirebase(member.roomName)
    }

    suspend fun deleteMember(member: Member) {
        memberDao.deleteMember(member)
        syncToFirebase(member.roomName)
    }

    suspend fun updateMembers(roomName: String, members: List<Member>) {
        memberDao.insertMembers(members)
        syncToFirebase(roomName)
    }

    suspend fun updateTrashStateOnly(state: TrashState) {
        trashStateDao.insertTrashState(state)
        syncToFirebase(state.roomName)
    }

    // Mark trash as FULL and send email notification to the person on turn
    suspend fun reportTrashFull(roomName: String, reporterName: String): Pair<Boolean, String?> {
        val currentState = trashStateDao.getTrashState(roomName) ?: return Pair(false, "Không tìm thấy trạng thái rác.")
        if (currentState.isTrashFull) {
            return Pair(false, "Thùng rác đã được báo đầy từ trước rồi!")
        }

        val members = memberDao.getMembersByRoom(roomName)
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
        historyLogDao.insertLog(HistoryLog(roomName = roomName, message = logMsg, type = "FULL"))

        // Sync to Firebase
        syncToFirebase(roomName, updatedState, members)

        // Try background email sending with Gmail SMTP if configured
        var emailSentMessage = "Báo rác đầy thành công!"
        val gmailSender = BuildConfig.SENDER_GMAIL_ADDRESS
        val gmailPassword = BuildConfig.SENDER_GMAIL_PASSWORD

        val basicDbUrl = BuildConfig.FIREBASE_DB_URL.ifBlank { updatedState.firebaseDbUrl }
        val targetApiKey = BuildConfig.FIREBASE_API_KEY.ifBlank { updatedState.firebaseApiKey }
        val safeRoomName = roomName.filter { it.isLetterOrDigit() }.lowercase()
        val roomSpecificDbUrl = if (basicDbUrl.isNotBlank()) {
            if (basicDbUrl.endsWith("/")) basicDbUrl + safeRoomName else "$basicDbUrl/$safeRoomName"
        } else {
            ""
        }

        if (gmailSender.isNotBlank() && gmailPassword.isNotBlank() && currentTurnMember.email.isNotBlank()) {
            val emailSuccess = emailSender.sendEmailViaGmail(
                senderEmail = gmailSender,
                senderPassword = gmailPassword,
                toEmail = currentTurnMember.email,
                recipientName = currentTurnMember.name,
                sequenceId = currentTurnMember.id,
                webConfirmUrl = BuildConfig.WEB_CONFIRM_URL.ifBlank { updatedState.webConfirmUrl },
                firebaseDbUrl = roomSpecificDbUrl,
                firebaseApiKey = targetApiKey
            )
            emailSentMessage = if (emailSuccess) {
                "Báo rác đầy thành công và đã tự động gửi email nhắc nhở tới ${currentTurnMember.name}!"
            } else {
                "Đã báo rác đầy nhưng không gửi được email nhắc nhở qua Gmail SMTP. Hãy kiểm tra lại cấu hình .env!"
            }
        } else {
            emailSentMessage = "Báo rác đầy thành công! (Chưa cấu hình tài khoản Gmail trong .env)"
        }

        return Pair(true, emailSentMessage)
    }

    // Confirm that trash is dumped, advancing the sequence (0-n)
    suspend fun confirmTrashDumped(roomName: String, dumperName: String): Boolean {
        val currentState = trashStateDao.getTrashState(roomName) ?: return false
        val members = memberDao.getMembersByRoom(roomName)
        if (members.isEmpty()) return false

        val previousIndex = currentState.currentTurnIndex
        val previousMember = members.getOrNull(previousIndex)

        // Next Index dynamically cycled based on members size
        val nextIndex = if (members.isNotEmpty()) (previousIndex + 1) % members.size else 0
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
        historyLogDao.insertLog(HistoryLog(roomName = roomName, message = logMsg, type = "DUMPED"))

        // Sync to Firebase
        syncToFirebase(roomName, updatedState, members)
        return true
    }

    // Change settings for Firebase config
    suspend fun updateFirebaseSettings(roomName: String, url: String, secret: String, projectId: String) {
        val currentState = trashStateDao.getTrashState(roomName) ?: TrashState(roomName = roomName)
        val updatedState = currentState.copy(
            firebaseDbUrl = url,
            firebaseApiKey = secret,
            firebaseProjectId = projectId
        )
        trashStateDao.insertTrashState(updatedState)
        historyLogDao.insertLog(HistoryLog(roomName = roomName, message = "Đã cập nhật thông tin đồng bộ Firebase.", type = "CONFIG"))

        // Try immediate sync to establish database collection
        syncToFirebase(roomName, updatedState, memberDao.getMembersByRoom(roomName))
    }

    // Change settings for Resend key
    suspend fun updateResendSettings(roomName: String, apiKey: String, webConfirmUrl: String) {
        val currentState = trashStateDao.getTrashState(roomName) ?: TrashState(roomName = roomName)
        val updatedState = currentState.copy(
            resendApiKey = apiKey,
            webConfirmUrl = webConfirmUrl
        )
        trashStateDao.insertTrashState(updatedState)
        historyLogDao.insertLog(HistoryLog(roomName = roomName, message = "Đã cấu hình lại dịch vụ gửi mail tự động Resend và link Xác nhận nhanh.", type = "CONFIG"))
    }

    // Change settings for Admin credentials
    suspend fun updateAdminCredentials(roomName: String, email: String, secret: String) {
        val currentState = trashStateDao.getTrashState(roomName) ?: TrashState(roomName = roomName)
        val updatedState = currentState.copy(
            adminEmail = email,
            adminPassword = secret
        )
        trashStateDao.insertTrashState(updatedState)

        // Update the DormRoom entity record as well to keep in sync
        val room = dormRoomDao.getRoom(roomName)
        if (room != null) {
            dormRoomDao.insertRoom(room.copy(adminEmail = email, adminPassword = secret))
        }

        historyLogDao.insertLog(HistoryLog(roomName = roomName, message = "Đã cập nhật thông tin bảo mật tài khoản quản trị Admin.", type = "CONFIG"))
    }

    // Manually push current state of a room to Firebase
    suspend fun syncToFirebase(roomName: String): Boolean {
        val state = trashStateDao.getTrashState(roomName) ?: return false
        val members = memberDao.getMembersByRoom(roomName)
        return syncToFirebase(roomName, state, members)
    }

    private suspend fun syncToFirebase(roomName: String, state: TrashState, members: List<Member>): Boolean {
        val targetDbUrl = state.firebaseDbUrl.ifBlank { BuildConfig.FIREBASE_DB_URL }
        val targetApiKey = state.firebaseApiKey.ifBlank { BuildConfig.FIREBASE_API_KEY }

        if (targetDbUrl.isNotBlank()) {
            val safeRoomName = roomName.filter { it.isLetterOrDigit() }.lowercase()
            // Sync with a clean per-room Firebase DB Url path (using a distinct subnode)
            val subUrl = if (targetDbUrl.endsWith("/")) {
                targetDbUrl + safeRoomName
            } else {
                targetDbUrl + "/" + safeRoomName
            }

            val success = firebaseClient.syncToFirebase(
                dbUrl = subUrl,
                apiKey = targetApiKey,
                state = state,
                members = members
            )
            if (success) {
                // Update temporary sync feedback locally
                val latest = trashStateDao.getTrashState(roomName) ?: state
                trashStateDao.insertTrashState(latest.copy(lastSyncedAt = System.currentTimeMillis()))
            }
            return success
        }
        return false
    }

    // Pull current state of a room from Firebase and overwrite local state if new
    suspend fun fetchFromFirebase(roomName: String): Boolean {
        val state = trashStateDao.getTrashState(roomName) ?: return false
        val targetDbUrl = state.firebaseDbUrl.ifBlank { BuildConfig.FIREBASE_DB_URL }
        val targetApiKey = state.firebaseApiKey.ifBlank { BuildConfig.FIREBASE_API_KEY }

        if (targetDbUrl.isBlank()) return false

        val safeRoomName = roomName.filter { it.isLetterOrDigit() }.lowercase()
        val subUrl = if (targetDbUrl.endsWith("/")) {
            targetDbUrl + safeRoomName
        } else {
            targetDbUrl + "/" + safeRoomName
        }

        val payload = firebaseClient.fetchFromFirebase(
            dbUrl = subUrl,
            apiKey = targetApiKey
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
            val remoteMembers = payload.members.map { Member(roomName, it.id, it.name, it.email, it.password) }
            if (remoteMembers.isNotEmpty()) {
                memberDao.insertMembers(remoteMembers)
            }

            historyLogDao.insertLog(HistoryLog(roomName = roomName, message = "Đồng bộ hóa dữ liệu trực tuyến từ Firebase thành công!", type = "SYSTEM"))
            return true
        }
        return false
    }
}

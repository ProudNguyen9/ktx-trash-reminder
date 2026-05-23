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
import java.util.UUID

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
        val currentMembers = memberDao.getMembersByRoom(member.roomName)
        val updatedMembers = if (currentMembers.any { it.id == member.id }) {
            currentMembers.map { current -> if (current.id == member.id) member else current }
        } else {
            currentMembers + member
        }
        updateMembers(member.roomName, updatedMembers)
    }

    suspend fun deleteMember(member: Member) {
        memberDao.deleteMember(member)
        syncToFirebase(member.roomName)
    }

    suspend fun updateMembers(roomName: String, members: List<Member>) {
        val currentState = trashStateDao.getTrashState(roomName)
        val oldMembers = memberDao.getMembersByRoom(roomName)
        val oldOrderById = oldMembers.mapIndexed { index, member -> member.id to index }.toMap()
        val requestedOrderById = members.mapIndexed { index, member -> member.id to index }.toMap()
        val movedMembers = members.filter { member ->
            val oldOrder = oldOrderById[member.id]
            val requestedOrder = requestedOrderById[member.id]
            oldOrder != null && requestedOrder != null && oldOrder != requestedOrder
        }
        val queueUpdate = applyAbsenceReturnQueueRule(roomName, oldMembers, members, currentState)

        memberDao.deleteAllMembersInRoom(roomName)
        memberDao.insertMembers(queueUpdate.members)

        if (currentState != null) {
            trashStateDao.insertTrashState(currentState.copy(currentTurnIndex = queueUpdate.currentTurnIndex))
        }

        if (movedMembers.isNotEmpty()) {
            val details = movedMembers.joinToString(", ") { member ->
                val oldPosition = (oldOrderById[member.id] ?: 0) + 1
                val newPosition = (requestedOrderById[member.id] ?: 0) + 1
                "${member.name}: $oldPosition → $newPosition"
            }
            historyLogDao.insertLog(
                HistoryLog(
                    roomName = roomName,
                    message = "Admin đã chỉnh sửa vị trí thành viên: $details.",
                    type = "CONFIG"
                )
            )
        }

        normalizeCurrentTurnAfterMemberChange(roomName)
    }

    suspend fun updateCurrentTurnMember(roomName: String, memberId: Int): Boolean {
        val currentState = trashStateDao.getTrashState(roomName) ?: return false
        val members = memberDao.getMembersByRoom(roomName)
        if (members.isEmpty()) return false

        val targetIndex = members.indexOfFirst { it.id == memberId }
        if (targetIndex == -1) return false

        val targetMember = members[targetIndex]
        if (targetMember.isAbsent) return false

        val updatedState = currentState.copy(currentTurnIndex = targetIndex)
        trashStateDao.insertTrashState(updatedState)
        historyLogDao.insertLog(
            HistoryLog(
                roomName = roomName,
                message = "Admin đã đổi lượt đổ rác hiện tại sang ${targetMember.name}.",
                type = "CONFIG"
            )
        )
        syncToFirebase(roomName, updatedState, members)
        return true
    }

    suspend fun deleteRoom(roomName: String): Boolean {
        val room = dormRoomDao.getRoom(roomName) ?: return false
        memberDao.deleteAllMembersInRoom(roomName)
        historyLogDao.clearAllLogs(roomName)
        trashStateDao.deleteTrashState(roomName)
        dormRoomDao.deleteRoom(room)
        return true
    }

    suspend fun updateTrashStateOnly(state: TrashState) {
        trashStateDao.insertTrashState(state)
        syncToFirebase(state.roomName)
    }

    // Mark trash as FULL and send email notification to the person on turn
    suspend fun reportTrashFull(roomName: String, reporterName: String): Pair<Boolean, String?> {
        val currentState = trashStateDao.getTrashState(roomName) ?: return Pair(false, "Không tìm thấy trạng thái rác.")
        val members = memberDao.getMembersByRoom(roomName)
        if (members.isEmpty()) {
            return Pair(false, "Không tìm thấy danh sách thành viên.")
        }

        val safeTurnIndex = currentState.currentTurnIndex.floorMod(members.size)
        val currentTurnIndex = findNextAvailableTurnIndex(members, safeTurnIndex, includeStart = true)
            ?: return Pair(false, "Tất cả thành viên đang vắng mặt, không thể gửi email nhắc rác.")
        val currentTurnMember = members.getOrNull(currentTurnIndex)
            ?: return Pair(false, "Thành viên hiện tại không hợp lệ.")

        val confirmToken = UUID.randomUUID().toString().replace("-", "")

        // Update state locally
        val updatedState = currentState.copy(
            currentTurnIndex = currentTurnIndex,
            isTrashFull = true,
            reportedByName = reporterName,
            reportedAt = System.currentTimeMillis(),
            confirmToken = confirmToken,
            confirmEmail = currentTurnMember.email.trim().lowercase()
        )
        trashStateDao.insertTrashState(updatedState)

        // Log to history
        val logMsg = "$reporterName đã thông báo rác đầy."
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
                roomName = roomName,
                webConfirmUrl = BuildConfig.WEB_CONFIRM_URL.ifBlank { updatedState.webConfirmUrl },
                firebaseDbUrl = roomSpecificDbUrl,
                firebaseApiKey = targetApiKey,
                confirmToken = confirmToken
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
    suspend fun confirmTrashDumped(roomName: String, dumperName: String, loggedInEmail: String): Boolean {
        val currentState = trashStateDao.getTrashState(roomName) ?: return false
        val members = memberDao.getMembersByRoom(roomName)
        if (members.isEmpty()) return false

        val safeTurnIndex = currentState.currentTurnIndex.floorMod(members.size)
        val previousIndex = findNextAvailableTurnIndex(members, safeTurnIndex, includeStart = true) ?: return false
        val previousMember = members.getOrNull(previousIndex)
        if (
            previousMember == null ||
            dumperName.trim() != previousMember.name.trim() ||
            loggedInEmail.trim().lowercase() != previousMember.email.trim().lowercase()
        ) {
            return false
        }

        // Next Index dynamically cycles and skips absent members
        val nextIndex = findNextAvailableTurnIndex(members, previousIndex + 1, includeStart = true) ?: previousIndex

        // Update state locally
        val updatedState = currentState.copy(
            currentTurnIndex = nextIndex,
            isTrashFull = false,
            reportedByName = "",
            reportedAt = 0L,
            confirmToken = "",
            confirmEmail = ""
        )
        trashStateDao.insertTrashState(updatedState)

        // Log to history
        val logMsg = "$dumperName đã đổ rác."
        historyLogDao.insertLog(HistoryLog(roomName = roomName, message = logMsg, type = "DUMPED"))

        // Sync to Firebase
        syncToFirebase(roomName, updatedState, members)
        return true
    }

    private data class QueueUpdateResult(
        val members: List<Member>,
        val currentTurnIndex: Int
    )

    private suspend fun applyAbsenceReturnQueueRule(
        roomName: String,
        oldMembers: List<Member>,
        requestedMembers: List<Member>,
        currentState: TrashState?
    ): QueueUpdateResult {
        if (requestedMembers.isEmpty()) {
            return QueueUpdateResult(emptyList(), 0)
        }

        if (oldMembers.isEmpty() || currentState == null) {
            val initializedMembers = requestedMembers
                .distinctBy { it.id }
                .sortedBy { it.turnOrder }
                .mapIndexed { index, member ->
                    member.copy(
                        roomName = roomName,
                        turnOrder = index,
                        absentReturnDistance = if (member.isAbsent) 0 else -1
                    )
                }
            return QueueUpdateResult(initializedMembers, 0)
        }

        val requestedById = requestedMembers.distinctBy { it.id }.associateBy { it.id }
        val oldById = oldMembers.associateBy { it.id }
        val originalTurnIndex = currentState.currentTurnIndex.floorMod(oldMembers.size)
        val currentMemberId = oldMembers.getOrNull(originalTurnIndex)?.id
        val returningMembers = mutableListOf<Pair<Member, Int>>()

        val keptOldMembers = oldMembers
            .filter { requestedById.containsKey(it.id) }
            .mapNotNull { oldMember ->
                val requested = requestedById[oldMember.id] ?: return@mapNotNull null
                when {
                    !oldMember.isAbsent && requested.isAbsent -> {
                        val oldIndex = oldMembers.indexOfFirst { it.id == oldMember.id }
                        val distance = (oldIndex - originalTurnIndex).floorMod(oldMembers.size)
                        historyLogDao.insertLog(
                            HistoryLog(
                                roomName = roomName,
                                message = "${requested.name} đã bật vắng mặt. Hệ thống lưu khoảng cách tới lượt hiện tại là $distance.",
                                type = "SYSTEM"
                            )
                        )
                        oldMember.copy(
                            name = requested.name,
                            email = requested.email,
                            password = requested.password,
                            isAbsent = true,
                            absentReturnDistance = distance
                        )
                    }
                    oldMember.isAbsent && !requested.isAbsent -> {
                        val savedDistance = if (oldMember.absentReturnDistance >= 0) {
                            oldMember.absentReturnDistance
                        } else {
                            val oldIndex = oldMembers.indexOfFirst { it.id == oldMember.id }
                            (oldIndex - originalTurnIndex).floorMod(oldMembers.size)
                        }
                        returningMembers += oldMember.copy(
                            name = requested.name,
                            email = requested.email,
                            password = requested.password,
                            isAbsent = false,
                            absentReturnDistance = -1
                        ) to savedDistance
                        null
                    }
                    else -> oldMember.copy(
                        name = requested.name,
                        email = requested.email,
                        password = requested.password,
                        isAbsent = requested.isAbsent,
                        turnOrder = requested.turnOrder,
                        absentReturnDistance = if (requested.isAbsent) oldMember.absentReturnDistance else -1
                    )
                }
            }
            .toMutableList()

        val newMembers = requestedMembers
            .filter { oldById[it.id] == null }
            .map { member -> member.copy(roomName = roomName, absentReturnDistance = if (member.isAbsent) 0 else -1) }
        keptOldMembers.addAll(newMembers)

        val requestedOrderRank = requestedMembers.mapIndexed { index, member -> member.id to index }.toMap()
        keptOldMembers.sortBy { requestedOrderRank[it.id] ?: Int.MAX_VALUE }

        returningMembers.forEach { (member, savedDistance) ->
            val currentIndex = currentMemberId?.let { id -> keptOldMembers.indexOfFirst { it.id == id } } ?: -1
            val baseIndex = if (currentIndex >= 0) currentIndex else 0
            val insertIndex = if (keptOldMembers.isEmpty()) {
                0
            } else {
                (baseIndex + savedDistance + 1).floorMod(keptOldMembers.size + 1)
            }
            keptOldMembers.add(insertIndex, member)
            historyLogDao.insertLog(
                HistoryLog(
                    roomName = roomName,
                    message = "${member.name} đã quay lại. Hệ thống xếp vào vị trí cách lượt hiện tại ${savedDistance + 1} bước.",
                    type = "SYSTEM"
                )
            )
        }

        val normalizedMembers = keptOldMembers.mapIndexed { index, member -> member.copy(turnOrder = index) }
        val nextCurrentIndex = if (normalizedMembers.isEmpty()) {
            0
        } else {
            originalTurnIndex.floorMod(normalizedMembers.size)
        }

        return QueueUpdateResult(normalizedMembers, nextCurrentIndex)
    }

    private suspend fun normalizeCurrentTurnAfterMemberChange(roomName: String) {
        val currentState = trashStateDao.getTrashState(roomName)
        val members = memberDao.getMembersByRoom(roomName)
        if (currentState == null || members.isEmpty()) {
            syncToFirebase(roomName)
            return
        }

        val safeTurnIndex = currentState.currentTurnIndex.floorMod(members.size)
        val nextAvailableIndex = findNextAvailableTurnIndex(members, safeTurnIndex, includeStart = true)
        val normalizedState = if (nextAvailableIndex != null && nextAvailableIndex != safeTurnIndex) {
            val skippedMember = members.getOrNull(safeTurnIndex)
            val nextMember = members.getOrNull(nextAvailableIndex)
            historyLogDao.insertLog(
                HistoryLog(
                    roomName = roomName,
                    message = "${skippedMember?.name ?: "Thành viên hiện tại"} đang vắng mặt nên lượt đổ rác được chuyển sang ${nextMember?.name ?: "thành viên tiếp theo"}.",
                    type = "SYSTEM"
                )
            )
            currentState.copy(currentTurnIndex = nextAvailableIndex)
        } else {
            currentState.copy(currentTurnIndex = safeTurnIndex)
        }

        trashStateDao.insertTrashState(normalizedState)
        syncToFirebase(roomName, normalizedState, members)
    }

    private fun findNextAvailableTurnIndex(
        members: List<Member>,
        startIndex: Int,
        includeStart: Boolean
    ): Int? {
        if (members.isEmpty()) return null
        val normalizedStart = startIndex.floorMod(members.size)
        val firstOffset = if (includeStart) 0 else 1
        for (offset in firstOffset until members.size + firstOffset) {
            val index = (normalizedStart + offset).floorMod(members.size)
            if (!members[index].isAbsent) return index
        }
        return null
    }

    private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor

    // Change settings for Firebase config
    suspend fun updateFirebaseSettings(roomName: String, url: String, secret: String, projectId: String) {
        val currentState = trashStateDao.getTrashState(roomName) ?: TrashState(roomName = roomName)
        val updatedState = currentState.copy(
            firebaseDbUrl = url,
            firebaseApiKey = secret,
            firebaseProjectId = projectId
        )
        trashStateDao.insertTrashState(updatedState)

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

    suspend fun saveFcmToken(roomName: String, email: String, token: String): Boolean {
        val state = trashStateDao.getTrashState(roomName) ?: return false
        val targetDbUrl = state.firebaseDbUrl.ifBlank { BuildConfig.FIREBASE_DB_URL }
        val targetApiKey = state.firebaseApiKey.ifBlank { BuildConfig.FIREBASE_API_KEY }
        return firebaseClient.saveFcmToken(
            dbUrl = targetDbUrl,
            apiKey = targetApiKey,
            roomName = roomName,
            email = email,
            token = token
        )
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
                confirmToken = payload.confirmToken,
                confirmEmail = payload.confirmEmail,
                lastSyncedAt = System.currentTimeMillis()
            )
            trashStateDao.insertTrashState(updatedState)

            // Overwrite members if they were customized remotely
            val remoteMembers = payload.members.map {
                Member(
                    roomName = roomName,
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    password = it.password,
                    isAbsent = it.isAbsent,
                    turnOrder = it.turnOrder,
                    absentReturnDistance = it.absentReturnDistance
                )
            }
            if (remoteMembers.isNotEmpty()) {
                memberDao.insertMembers(remoteMembers)
            }

            return true
        }
        return false
    }
}

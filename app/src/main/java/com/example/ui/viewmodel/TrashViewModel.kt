package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DormRoom
import com.example.data.model.HistoryLog
import com.example.data.model.LoginMatch
import com.example.data.model.Member
import com.example.data.model.TrashState
import com.example.data.repository.TrashRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalCoroutinesApi::class)
class TrashViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TrashRepository(
        database.dormRoomDao(),
        database.memberDao(),
        database.trashStateDao(),
        database.historyLogDao()
    )

    // Exposed Flows
    val allRooms: StateFlow<List<DormRoom>> = repository.allRoomsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeRoomName = MutableStateFlow<String?>(null)
    val activeRoomName: StateFlow<String?> = _activeRoomName.asStateFlow()

    val members: StateFlow<List<Member>> = _activeRoomName
        .flatMapLatest { room ->
            if (room != null) repository.getMembersFlow(room) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trashState: StateFlow<TrashState?> = _activeRoomName
        .flatMapLatest { room ->
            if (room != null) repository.getTrashStateFlow(room) else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentLogs: StateFlow<List<HistoryLog>> = _activeRoomName
        .flatMapLatest { room ->
            if (room != null) repository.getRecentLogsFlow(room) else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Loading overlay indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Status message for Snackbars
    private val _statusMessage = MutableSharedFlow<String>()
    val statusMessage: SharedFlow<String> = _statusMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            repository.initDefaultData()
            // Set initial active room to default if rooms are loaded
            val rooms = repository.getAllRooms()
            if (rooms.isNotEmpty()) {
                _activeRoomName.value = rooms.first().roomName
                try {
                    repository.fetchFromFirebase(rooms.first().roomName)
                } catch (e: Exception) {
                    // Fail silently on first launch
                }
            }
            _isLoading.value = false
        }
    }

    suspend fun checkLoginCredentials(email: String, secret: String): List<LoginMatch> {
        val emailNorm = email.trim().lowercase()
        val passwordNorm = secret.trim()
        val matches = mutableListOf<LoginMatch>()

        // 1. Check all rooms to find where they are Admin
        val rooms = database.dormRoomDao().getAllRooms()
        for (room in rooms) {
            if (room.adminEmail.lowercase() == emailNorm && room.adminPassword == passwordNorm) {
                matches.add(LoginMatch(room.roomName, "admin", room.adminName))
            }
        }

        // 2. Check all members of all rooms
        val allMembers = database.memberDao().getAllMembers()
        for (member in allMembers) {
            if (member.email.lowercase() == emailNorm && member.password == passwordNorm) {
                // Avoid duplicates for the same room if they have admin privileges
                if (matches.none { it.roomName == member.roomName }) {
                    matches.add(LoginMatch(member.roomName, "user", member.name))
                }
            }
        }
        return matches
    }

    fun selectRoom(roomName: String) {
        viewModelScope.launch {
            _activeRoomName.value = roomName
            _isLoading.value = true
            try {
                repository.fetchFromFirebase(roomName)
            } catch (e: Exception) {
                // Fail silently
            }
            _isLoading.value = false
        }
    }

    fun registerFcmTokenForLogin(roomName: String, email: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                repository.saveFcmToken(roomName, email, token)
            } catch (e: Exception) {
                _statusMessage.emit("Không lưu được token thông báo đẩy: ${e.localizedMessage}")
            }
        }
    }

    fun registerRoom(
        roomName: String,
        adminName: String,
        adminEmail: String,
        adminPassword: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.registerRoom(roomName, adminName, adminEmail, adminPassword)
            if (success) {
                _statusMessage.emit("Đăng ký thành công phòng: $roomName!")
                _activeRoomName.value = roomName // Set active room
            } else {
                _statusMessage.emit("Phòng '$roomName' đã tồn tại hoặc tên phòng không hợp lệ!")
            }
            _isLoading.value = false
            onResult(success)
        }
    }

    // Actions
    fun updateMember(member: Member) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateMember(member)
            _statusMessage.emit("Đã cập nhật: ${member.name}")
            _isLoading.value = false
        }
    }

    fun updateMembers(membersList: List<Member>) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateMembers(room, membersList)
            _statusMessage.emit("Đã cập nhật danh sách thành viên!")
            _isLoading.value = false
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteMember(member)
            _statusMessage.emit("Đã xóa thành viên: ${member.name}")
            _isLoading.value = false
        }
    }

    fun deleteCurrentRoom(onDeleted: () -> Unit) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val deleted = repository.deleteRoom(room)
            if (deleted) {
                _activeRoomName.value = null
                _statusMessage.emit("Đã xóa phòng $room và toàn bộ dữ liệu liên quan.")
                onDeleted()
            } else {
                _statusMessage.emit("Không thể xóa phòng hiện tại.")
            }
            _isLoading.value = false
        }
    }

    fun reportTrashFull(reporterName: String) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.reportTrashFull(room, reporterName)
            _statusMessage.emit(result.second ?: "Báo rác đầy thành công!")
            _isLoading.value = false
        }
    }

    fun confirmTrashDumped(dumperName: String, loggedInEmail: String) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.confirmTrashDumped(room, dumperName, loggedInEmail)
            if (success) {
                _statusMessage.emit("Cám ơn $dumperName! Lượt đổ rác đã được cập nhật thành công.")
            } else {
                _statusMessage.emit("Bạn chưa tới lượt đổ rác hoặc thông tin đăng nhập không khớp.")
            }
            _isLoading.value = false
        }
    }

    fun saveFirebaseSettings(url: String, apiKey: String, projectId: String) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateFirebaseSettings(room, url, apiKey, projectId)
            _statusMessage.emit("Lưu thành công cài đặt Firebase Realtime Database!")
            _isLoading.value = false
        }
    }

    fun saveResendSettings(resendKey: String, webConfirmUrl: String) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateResendSettings(room, resendKey, webConfirmUrl)
            _statusMessage.emit("Đã lưu cấu hình Email & Link Xác nhận nhanh!")
            _isLoading.value = false
        }
    }

    fun saveAdminCredentials(email: String, secret: String) {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateAdminCredentials(room, email, secret)
            _statusMessage.emit("Đã cập nhật thông tin bảo mật tài khoản Admin mới thành công!")
            _isLoading.value = false
        }
    }

    fun syncNow() {
        val room = _activeRoomName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = trashState.value
                if (state != null && state.firebaseDbUrl.isNotBlank()) {
                    // Try fetch first
                    val fetched = repository.fetchFromFirebase(room)
                    if (fetched) {
                        _statusMessage.emit("🔄 Đã đồng bộ trạng thái mới nhất từ Firebase!")
                    } else {
                        // If empty or fetch failed, push our local state to Firebase
                        val pushed = repository.syncToFirebase(room)
                        if (pushed) {
                            _statusMessage.emit("📤 Đã tải trạng thái hiện tại lên Firebase!")
                        } else {
                            _statusMessage.emit("⚠️ Thất bại khi đồng bộ với Firebase. Kiểm tra cấu hình Database!")
                        }
                    }
                } else {
                    _statusMessage.emit("⚠️ Chưa cấu hình Firebase! Vui lòng vào Cấu hình Admin để cài đặt.")
                }
            } catch (e: Exception) {
                _statusMessage.emit("Lỗi đồng bộ: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

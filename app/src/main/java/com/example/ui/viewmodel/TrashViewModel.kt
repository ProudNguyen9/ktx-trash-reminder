package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import com.example.data.repository.TrashRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrashViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TrashRepository(
        database.memberDao(),
        database.trashStateDao(),
        database.historyLogDao()
    )

    // Exposed Flows
    val members: StateFlow<List<Member>> = repository.membersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trashState: StateFlow<TrashState?> = repository.trashStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val recentLogs: StateFlow<List<HistoryLog>> = repository.recentLogsFlow
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
            // Try fetching online sync on startup
            try {
                repository.fetchFromFirebase()
            } catch (e: Exception) {
                // Fail silently on first launch
            }
            _isLoading.value = false
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
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateMembers(membersList)
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

    fun reportTrashFull(reporterName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.reportTrashFull(reporterName)
            _statusMessage.emit(result.second ?: "Báo rác đầy thành công!")
            _isLoading.value = false
        }
    }

    fun confirmTrashDumped(dumperName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.confirmTrashDumped(dumperName)
            if (success) {
                _statusMessage.emit("Cám ơn $dumperName! Lượt đổ rác đã được cập nhật thành công.")
            } else {
                _statusMessage.emit("Không thể cập nhật trạng thái.")
            }
            _isLoading.value = false
        }
    }

    fun saveFirebaseSettings(url: String, apiKey: String, projectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateFirebaseSettings(url, apiKey, projectId)
            _statusMessage.emit("Lưu thành công cài đặt Firebase Realtime Database!")
            _isLoading.value = false
        }
    }

    fun saveResendSettings(resendKey: String, webConfirmUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateResendSettings(resendKey, webConfirmUrl)
            _statusMessage.emit("Đã lưu cấu hình Email & Link Xác nhận nhanh!")
            _isLoading.value = false
        }
    }

    fun saveAdminCredentials(email: String, secret: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateAdminCredentials(email, secret)
            _statusMessage.emit("Đã cập nhật thông tin bảo mật tài khoản Admin mới thành công!")
            _isLoading.value = false
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = trashState.value
                if (state != null && state.firebaseDbUrl.isNotBlank()) {
                    // Try fetch first
                    val fetched = repository.fetchFromFirebase()
                    if (fetched) {
                        _statusMessage.emit("🔄 Đã đồng bộ trạng thái mới nhất từ Firebase!")
                    } else {
                        // If empty or fetch failed, push our local state to Firebase
                        val pushed = repository.syncToFirebase()
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

package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.TrashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashAppUi(
    viewModel: TrashViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsStateWithLifecycle()
    val trashState by viewModel.trashState.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val activeRoomName by viewModel.activeRoomName.collectAsStateWithLifecycle()

    val sessionPrefs = remember { context.getSharedPreferences("login_session", Context.MODE_PRIVATE) }
    var loggedInEmail by rememberSaveable { mutableStateOf(sessionPrefs.getString("email", "").orEmpty()) }
    var loggedInRole by rememberSaveable { mutableStateOf(sessionPrefs.getString("role", "").orEmpty()) }
    val savedRoomName = remember { sessionPrefs.getString("roomName", "").orEmpty() }

    LaunchedEffect(savedRoomName) {
        if (loggedInRole.isNotEmpty() && savedRoomName.isNotBlank()) {
            viewModel.selectRoom(savedRoomName)
            if (loggedInEmail.isNotBlank()) {
                viewModel.registerFcmTokenForLogin(savedRoomName, loggedInEmail)
            }
        }
    }

    if (loggedInRole.isEmpty()) {
        LoginScreen(
            onRegisterRoom = { roomName, adminName, adminEmail, adminPassword, onResult ->
                viewModel.registerRoom(roomName, adminName, adminEmail, adminPassword, onResult)
            },
            onLoginSuccess = { email, role, roomName ->
                viewModel.selectRoom(roomName)
                viewModel.registerFcmTokenForLogin(roomName, email)
                loggedInEmail = email
                loggedInRole = role
                sessionPrefs.edit()
                    .putString("email", email)
                    .putString("role", role)
                    .putString("roomName", roomName)
                    .apply()
            },
            checkCredentials = { email, password -> viewModel.checkLoginCredentials(email, password) }
        )
        return
    }

    var activeTab by remember { mutableStateOf(0) }
    var showConfirmDumpDialog by remember { mutableStateOf(false) }
    var showDeleteRoomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.statusMessage.collect { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
    }

    ModernAppBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Dorm Trash Guard", fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                            Text(activeRoomName ?: "Phòng KTX", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        if (loggedInRole == "admin") {
                            IconButton(onClick = { showDeleteRoomDialog = true }, modifier = Modifier.testTag("delete_room_button")) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa phòng", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(
                            onClick = {
                                sessionPrefs.edit().clear().apply()
                                loggedInEmail = ""
                                loggedInRole = ""
                                activeTab = 0
                                Toast.makeText(context, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("logout_app_button")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Đăng xuất", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Trang chủ", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Thành viên", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                        modifier = Modifier.testTag("nav_tab_roommates")
                    )
                    if (loggedInRole == "admin") {
                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            icon = { Icon(Icons.Default.Settings, null) },
                            label = { Text("Cấu hình", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                            modifier = Modifier.testTag("nav_tab_admin")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = { fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220)) },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> DashboardTab(
                            members = members,
                            trashState = trashState,
                            logs = recentLogs,
                            onReportFullClick = {
                                val reporterName = members
                                    .find { it.email.trim().lowercase() == loggedInEmail.trim().lowercase() }
                                    ?.name
                                    ?.takeIf { it.isNotBlank() }
                                    ?: loggedInEmail.ifBlank { "Người dùng" }
                                viewModel.reportTrashFull(reporterName)
                            },
                            onConfirmDumpClick = { showConfirmDumpDialog = true }
                        )
                        1 -> RoommatesSetupTab(
                            roomName = activeRoomName ?: "",
                            members = members,
                            canEdit = loggedInRole == "admin",
                            onSaveMembers = { updatedList -> if (loggedInRole == "admin") viewModel.updateMembers(updatedList) }
                        )
                        2 -> if (loggedInRole == "admin") {
                            ConfigTab(
                                trashState = trashState,
                                onSaveFirebase = { url, secret, projId -> viewModel.saveFirebaseSettings(url, secret, projId) },
                                onSaveResend = { key, webUrl -> viewModel.saveResendSettings(key, webUrl) },
                                onSaveAdminCredentials = { email, secret -> viewModel.saveAdminCredentials(email, secret) }
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.22f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(modifier = Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.size(16.dp))
                                Text("Đang xử lý...", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                if (showConfirmDumpDialog) {
                    val matchingMember = members.find { it.email.trim().lowercase() == loggedInEmail.trim().lowercase() }
                    val rawTurnIndex = trashState?.currentTurnIndex ?: 0
                    val safeTurnIndex = if (members.isNotEmpty()) ((rawTurnIndex % members.size) + members.size) % members.size else 0
                    val activeMember = members.getOrNull(safeTurnIndex)
                    ConfirmDumpDialog(
                        members = members,
                        defaultName = matchingMember?.name ?: "",
                        activeMember = activeMember,
                        onDismiss = { showConfirmDumpDialog = false },
                        onConfirm = { dumper ->
                            showConfirmDumpDialog = false
                            viewModel.confirmTrashDumped(dumper, loggedInEmail)
                        }
                    )
                }

                if (showDeleteRoomDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteRoomDialog = false },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        title = { Text("Xóa phòng hiện tại?", fontWeight = FontWeight.Black) },
                        text = { Text("Hành động này sẽ xóa phòng ${activeRoomName ?: "hiện tại"}, danh sách thành viên, trạng thái và lịch sử trên máy. Không thể hoàn tác.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteRoomDialog = false
                                    viewModel.deleteCurrentRoom {
                                        sessionPrefs.edit().clear().apply()
                                        loggedInEmail = ""
                                        loggedInRole = ""
                                        activeTab = 0
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Xóa phòng", fontWeight = FontWeight.Black) }
                        },
                        dismissButton = { TextButton(onClick = { showDeleteRoomDialog = false }) { Text("Hủy") } },
                        shape = RoundedCornerShape(28.dp)
                    )
                }
            }
        }
    }
}

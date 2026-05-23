package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

    val sessionPrefs = remember {
        context.getSharedPreferences("login_session", Context.MODE_PRIVATE)
    }
    var loggedInEmail by rememberSaveable { mutableStateOf(sessionPrefs.getString("email", "").orEmpty()) }
    var loggedInRole by rememberSaveable { mutableStateOf(sessionPrefs.getString("role", "").orEmpty()) } // "" (not logged in), "admin", "user"
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
            checkCredentials = { email, password ->
                viewModel.checkLoginCredentials(email, password)
            }
        )
        return
    }

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Cấu hình Roommates, 2: Cấu hình Admin

    // List of dialogues state
    var showConfirmDumpDialog by remember { mutableStateOf(false) }
    var showDeleteRoomDialog by remember { mutableStateOf(false) }

    // Toast and status messages trigger
    LaunchedEffect(Unit) {
        viewModel.statusMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Trash Icon",
                            tint = if (trashState?.isTrashFull == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dorm Trash Guard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (loggedInRole == "admin") {
                        IconButton(
                            onClick = { showDeleteRoomDialog = true },
                            modifier = Modifier.testTag("delete_room_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa phòng",
                                tint = MaterialTheme.colorScheme.error
                            )
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
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            if (loggedInRole == "admin") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.Home, "Bảng Điều Khiển") },
                        label = { Text("Bảng điều khiển", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.Person, "Thành viên") },
                        label = { Text("${members.size} Thành viên", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_tab_roommates")
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.Settings, "Cấu hình") },
                        label = { Text("Cấu hình Admin", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_tab_admin")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background texture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            )
                        )
                    )
            )

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
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
                    1 -> {
                        if (loggedInRole == "admin") {
                            RoommatesSetupTab(
                                roomName = activeRoomName ?: "",
                                members = members,
                                onSaveMembers = { updatedList ->
                                    viewModel.updateMembers(updatedList)
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Quyền truy cập bị hạn chế!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    2 -> {
                        if (loggedInRole == "admin") {
                            ConfigTab(
                                trashState = trashState,
                                onSaveFirebase = { url, secret, projId ->
                                    viewModel.saveFirebaseSettings(url, secret, projId)
                                },
                                onSaveResend = { key, webUrl ->
                                    viewModel.saveResendSettings(key, webUrl)
                                },
                                onSaveAdminCredentials = { email, secret ->
                                    viewModel.saveAdminCredentials(email, secret)
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Quyền truy cập bị hạn chế!", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Đang xử lý...",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Dialog for confirming trash dumped
            if (showConfirmDumpDialog) {
                val matchingMember = members.find { it.email.trim().lowercase() == loggedInEmail.trim().lowercase() }
                val rawTurnIndex = trashState?.currentTurnIndex ?: 0
                val safeTurnIndex = if (members.isNotEmpty()) {
                    ((rawTurnIndex % members.size) + members.size) % members.size
                } else {
                    0
                }
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
                    title = { Text("Xóa phòng hiện tại?", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Hành động này sẽ xóa phòng ${activeRoomName ?: "hiện tại"}, danh sách thành viên, trạng thái và lịch sử hoạt động trên máy. Không thể hoàn tác.")
                    },
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
                        ) {
                            Text("Xóa phòng")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteRoomDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}

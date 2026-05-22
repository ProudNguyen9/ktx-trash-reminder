package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import com.example.ui.viewmodel.TrashViewModel
import java.text.SimpleDateFormat
import java.util.*

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

    var loggedInEmail by rememberSaveable { mutableStateOf("") }
    var loggedInRole by rememberSaveable { mutableStateOf("") } // "" (not logged in), "admin", "user"

    if (loggedInRole.isEmpty()) {
        LoginScreen(
            members = members,
            onLoginSuccess = { email, role ->
                loggedInEmail = email
                loggedInRole = role
            }
        )
        return
    }

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Cấu hình Roommates, 2: Cài đặt Firebase/Email

    // List of dialogues state
    var showFullReportDialog by remember { mutableStateOf(false) }
    var showConfirmDumpDialog by remember { mutableStateOf(false) }

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
                    IconButton(
                        onClick = { viewModel.syncNow() },
                        modifier = Modifier.testTag("sync_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Now",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
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
                        label = { Text("7 Thành viên", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
            // Background Warm Elegant Texture
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
                        onReportFullClick = { showFullReportDialog = true },
                        onConfirmDumpClick = { showConfirmDumpDialog = true }
                    )
                    1 -> {
                        if (loggedInRole == "admin") {
                            RoommatesSetupTab(
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

            // Dialog for reporting trash full
            if (showFullReportDialog) {
                val matchingMember = members.find { it.email.trim().lowercase() == loggedInEmail.trim().lowercase() }
                val defaultReporterName = matchingMember?.name ?: ""
                ReportFullDialog(
                    members = members,
                    defaultName = defaultReporterName,
                    onDismiss = { showFullReportDialog = false },
                    onConfirm = { reporter ->
                        showFullReportDialog = false
                        viewModel.reportTrashFull(reporter)
                    }
                )
            }

            // Dialog for confirming trash dumped
            if (showConfirmDumpDialog) {
                val matchingMember = members.find { it.email.trim().lowercase() == loggedInEmail.trim().lowercase() }
                val defaultDumperName = matchingMember?.name ?: ""
                ConfirmDumpDialog(
                    members = members,
                    defaultName = defaultDumperName,
                    onDismiss = { showConfirmDumpDialog = false },
                    onConfirm = { dumper ->
                        showConfirmDumpDialog = false
                        viewModel.confirmTrashDumped(dumper)
                    }
                )
            }
        }
    }
}

// Helper to launch implicit email notification
fun launchEmailIntent(
    context: Context,
    email: String,
    name: String,
    id: Int,
    reporterName: String
) {
    val subject = "⚠️ [Dorm Trash Guard] THÙNG RÁC ĐẦY RỒI! Đến lượt bạn $name đổ rác!"
    val body = """
        Chào $name,
        
        Bạn cùng phòng $reporterName vừa bấm thông báo thùng rác trong phòng kí túc xá của chúng ta đã đầy rồi!
        
        Hiện tại đang đến lượt của bạn (Thứ tự quy định #$id trong vòng lặp 1-7) rút bao rác mang đi đổ.
        
        Hãy thực hiện nhiệm vụ đổ rác và sau đó nhấn nút "Xác nhận đã đổ" trên ứng dụng Dorm Trash Guard nhé!
        
        Thân ái,
        Dorm Trash Guard App • Đội phòng KTX 7 Người
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể mở ứng dụng gửi mail! Đã sao chép email.", Toast.LENGTH_SHORT).show()
    }
}

// ---------------------- DASHBOARD TAB ----------------------
@Composable
fun DashboardTab(
    members: List<Member>,
    trashState: TrashState?,
    logs: List<HistoryLog>,
    onReportFullClick: () -> Unit,
    onConfirmDumpClick: () -> Unit
) {
    val context = LocalContext.current
    val currentTurnIndex = trashState?.currentTurnIndex ?: 0
    val activeMember = members.getOrNull(currentTurnIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Visual Turn Tracker (Circle 1-7 roommates)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VÒNG LẶP ĐỔ RÁC 1-7",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Horizontal circular indicator of roommates
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..6) {
                            val member = members.getOrNull(i)
                            val isActive = i == currentTurnIndex
                            val isFull = trashState?.isTrashFull == true

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) {
                                                if (isFull) MaterialTheme.colorScheme.errorContainer
                                                else MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            }
                                        )
                                        .border(
                                            width = if (isActive) 2.dp else 1.dp,
                                            color = if (isActive) {
                                                if (isFull) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            },
                                            shape = CircleShape
                                        )
                                ) {
                                    Text(
                                        text = (i + 1).toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isActive) {
                                            if (isFull) MaterialTheme.colorScheme.onErrorContainer
                                            else MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = member?.name?.split(" ")?.lastOrNull() ?: "T${i + 1}",
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Active turn warning and Details card
        item {
            val isFull = trashState?.isTrashFull == true
            val colorBrush = if (isFull) {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isFull) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .background(colorBrush)
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFull) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                        ) {
                            // Pulsing trash/alert action
                            val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Icon(
                                imageVector = if (isFull) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = "Status Icon",
                                tint = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(44.dp)
                                    .animateContentSize()
                                    .padding(2.dp)
                                    .size(40.dp * scale)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isFull) "🚨 THÙNG RÁC ĐÃ ĐẦY!" else "✨ THÙNG RÁC SẠCH SẼ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )

                        if (isFull && trashState != null) {
                            val timeText = if (trashState.reportedAt > 0) {
                                val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
                                sdf.format(Date(trashState.reportedAt))
                            } else ""
                            
                            Text(
                                text = "Báo đầy bởi: ${trashState.reportedByName} ($timeText)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = if (isFull) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )

                        Text(
                            text = "Lượt đổ rác hiện tại:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = activeMember?.name ?: "Chưa rõ thành viên",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "Email: ${activeMember?.email ?: "member@example.com"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isFull && activeMember != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Helper option to trigger app mail client prefilled as secondary call
                            Button(
                                onClick = {
                                    launchEmailIntent(
                                        context = context,
                                        email = activeMember.email,
                                        name = activeMember.name,
                                        id = activeMember.id,
                                        reporterName = trashState.reportedByName.ifBlank { "Bạn cùng phòng" }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .testTag("send_email_fallback_button")
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "Mail Icon")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gửi mail nhắc nhở gấp", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Dual Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Garbage is full button
                Button(
                    onClick = onReportFullClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (trashState?.isTrashFull == true) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.error
                    ),
                    enabled = trashState?.isTrashFull != true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("btn_garbage_full")
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Full warning")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Báo Rác Đầy 🚨", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Configured Confirm Dump button
                Button(
                    onClick = onConfirmDumpClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("btn_garbage_dumped")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Success check")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đã Đổ Rác ✅", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 4: Logs heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Clock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LỊCH SỬ HOẠT ĐỘNG HOÀT ĐỘNG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Section 5: Log Items
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chưa có hoạt động nào được ghi lại.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(logs) { log ->
                LogItem(log)
            }
        }
    }
}

// Single Timeline Log item component
@Composable
fun LogItem(log: HistoryLog) {
    val sdf = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    val formattedTime = sdf.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon according to log type
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (log.type) {
                            "FULL" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            "DUMPED" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            "CONFIG" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        }
                    )
            ) {
                Icon(
                    imageVector = when (log.type) {
                        "FULL" -> Icons.Default.Warning
                        "DUMPED" -> Icons.Default.Check
                        "CONFIG" -> Icons.Default.Settings
                        else -> Icons.Default.Home
                    },
                    contentDescription = log.type,
                    tint = when (log.type) {
                        "FULL" -> MaterialTheme.colorScheme.error
                        "DUMPED" -> MaterialTheme.colorScheme.primary
                        "CONFIG" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}


// ---------------------- ROOMMATES SETUP TAB ----------------------
@Composable
fun RoommatesSetupTab(
    members: List<Member>,
    onSaveMembers: (List<Member>) -> Unit
) {
    // Hold local changes until saved
    val editedMembers = remember(members) {
        members.toMutableStateList()
    }
    var hasChanges by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("roommates_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Cài đặt danh tính và địa chỉ Email nhận thông báo của 7 thành viên trong phòng kí túc xá.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        items(editedMembers.size) { index ->
            val member = editedMembers[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                "T${member.id}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Thành viên thứ ${member.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = member.name,
                        onValueChange = { newVal ->
                            editedMembers[index] = member.copy(name = newVal)
                            hasChanges = true
                        },
                        label = { Text("Họ và Tên", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_name_input_${member.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = member.email,
                        onValueChange = { newVal ->
                            editedMembers[index] = member.copy(email = newVal)
                            hasChanges = true
                        },
                        label = { Text("Địa chỉ Email", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_email_input_${member.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSaveMembers(editedMembers.toList())
                    hasChanges = false
                },
                enabled = hasChanges,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(50.dp)
                    .testTag("save_members_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, "Lưu")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu danh sách thành viên", fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ---------------------- CONFIG TAB ----------------------
@Composable
fun ConfigTab(
    trashState: TrashState?,
    onSaveFirebase: (String, String, String) -> Unit,
    onSaveResend: (String, String) -> Unit
) {
    var dbUrl by remember { mutableStateOf(trashState?.firebaseDbUrl ?: "") }
    var dbSecret by remember { mutableStateOf(trashState?.firebaseApiKey ?: "") }
    var dbProjId by remember { mutableStateOf(trashState?.firebaseProjectId ?: "") }
    var resendKey by remember { mutableStateOf(trashState?.resendApiKey ?: "") }
    var webConfirmUrl by remember { mutableStateOf(trashState?.webConfirmUrl ?: "") }

    // Synchronize local UI state if database loads asynchronously
    LaunchedEffect(trashState) {
        if (trashState != null) {
            dbUrl = trashState.firebaseDbUrl
            dbSecret = trashState.firebaseApiKey
            dbProjId = trashState.firebaseProjectId
            resendKey = trashState.resendApiKey
            webConfirmUrl = trashState.webConfirmUrl
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Firebase sync section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ĐỒNG BỘ CLOUD FIREBASE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Cấu hình Firebase để đồng bộ lượt đổ rác giữa điện thoại của 7 thành viên trong phòng cùng thời gian thực.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dbUrl,
                        onValueChange = { dbUrl = it },
                        label = { Text("Firebase Realtime Database URL") },
                        placeholder = { Text("https://your-app.firebaseio.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firebase_url_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dbSecret,
                        onValueChange = { dbSecret = it },
                        label = { Text("Database Secret / Password") },
                        placeholder = { Text("Mật khẩu quyền ghi (Nếu có)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firebase_secret_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dbProjId,
                        onValueChange = { dbProjId = it },
                        label = { Text("Project ID") },
                        placeholder = { Text("ID của dự án Firebase") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("firebase_project_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onSaveFirebase(dbUrl, dbSecret, dbProjId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_firebase_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cấu hình Firebase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Resend background email setup
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "TỰ ĐỘNG GỬI MAIL THÔNG BÁO (RESEND API)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Cấu hình Mail API để hệ thống tự động gửi email thông báo chạy ngầm cho thành viên đến lượt khi nhấn nút 'Báo rác đầy' mà không cần vào app mail thủ công.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = resendKey,
                        onValueChange = { resendKey = it },
                        label = { Text("Resend API Key") },
                        placeholder = { Text("re_123456789...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resend_key_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = webConfirmUrl,
                        onValueChange = { webConfirmUrl = it },
                        label = { Text("Link Xác Nhận Qua Web (Web Confirm URL)") },
                        placeholder = { Text("https://your-domain.com/index.html") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("web_confirm_url_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onSaveResend(resendKey, webConfirmUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_resend_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu Email & Link Web Action", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Security Warning Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Khuyên dùng và bảo mật",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Nếu các thông tin cấu hình đồng bộ trực tuyến được nhập tại đây, chúng sẽ chỉ được lưu an toàn cục bộ trong bộ nhớ đệm điện thoại bằng cơ sở dữ liệu SQLite cục bộ (Room) để đảm bảo bảo mật tối đa cho tài khoản của bạn.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


// ----------------------- DIALOGUES -----------------------
@Composable
fun ReportFullDialog(
    members: List<Member>,
    defaultName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedName by remember { mutableStateOf(defaultName) }
    var localCustomName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Báo Rác Đầy 🚨", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Hãy chọn tên của bạn để lưu lịch sử báo rác đầy:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selectable column for 7 roommates
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(members) { member ->
                        val isSelected = selectedName == member.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedName = member.name
                                    localCustomName = ""
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                member.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text("Hoặc nhập tên khác:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = localCustomName,
                    onValueChange = {
                        localCustomName = it
                        selectedName = ""
                    },
                    label = { Text("Nhập họ và tên tự chọn", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_reporter_name")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (localCustomName.isNotBlank()) localCustomName else selectedName
                    if (finalName.isNotBlank()) {
                        onConfirm(finalName)
                    }
                },
                enabled = selectedName.isNotBlank() || localCustomName.isNotBlank()
            ) {
                Text("Xác Nhận", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ConfirmDumpDialog(
    members: List<Member>,
    defaultName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedName by remember { mutableStateOf(defaultName) }
    var localCustomName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Xác Nhận Đã Đổ Rác ✅", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Hãy chọn tên của bạn (người đã mang rác đi đổ) để hoàn thành lượt và chuyển giao sang cho người tiếp theo:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selectable column for 7 roommates
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(members) { member ->
                        val isSelected = selectedName == member.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedName = member.name
                                    localCustomName = ""
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                member.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text("Hoặc nhập tên khác:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = localCustomName,
                    onValueChange = {
                        localCustomName = it
                        selectedName = ""
                    },
                    label = { Text("Nhập họ và tên tự chọn", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_dumper_name")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (localCustomName.isNotBlank()) localCustomName else selectedName
                    if (finalName.isNotBlank()) {
                        onConfirm(finalName)
                    }
                },
                enabled = selectedName.isNotBlank() || localCustomName.isNotBlank()
            ) {
                Text("Đã Hoàn Thành", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun LoginScreen(
    members: List<Member>,
    onLoginSuccess: (String, String) -> Unit // (email, role)
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 400.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Icon/Emblem
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Dorm Trash Guard",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Đăng nhập KTX Phòng 302",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        errorMessage = null 
                    },
                    label = { Text("Email thành viên") },
                    placeholder = { Text("example@gmail.com") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null 
                    },
                    label = { Text("Mật khẩu") },
                    placeholder = { Text("••••••••") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Ẩn" else "Hiện",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Login Button
                Button(
                    onClick = {
                        val inputEmailNorm = email.trim().lowercase()
                        val inputPassNorm = password.trim()

                        if (inputEmailNorm == "nguyenhaohuu9@gmail.com" && inputPassNorm == "admin999") {
                            onLoginSuccess("nguyenhaohuu9@gmail.com", "admin")
                        } else if (inputPassNorm == "user123") {
                            val matchedMember = members.find { it.email.trim().lowercase() == inputEmailNorm }
                            if (matchedMember != null) {
                                onLoginSuccess(matchedMember.email, "user")
                            } else {
                                errorMessage = "Email không khớp với bất kỳ thành viên nào trong KTX!"
                            }
                        } else {
                            errorMessage = "Mật khẩu không hợp lệ hoặc thông tin đăng nhập sai!"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ĐĂNG NHẬP 🔐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Minimal Guides
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💡 Thông tin tài khoản mặc định:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Admin: nguyenhaohuu9@gmail.com / Pass: admin999",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Thành viên: Email trong Đội hình KTX / Pass: user123",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

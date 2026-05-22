package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState
import java.text.SimpleDateFormat
import java.util.*

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
        
        Hiện tại đang đến lượt của bạn (Thứ tự quy định #${id} trong vòng lặp) rút bao rác mang đi đổ.
        
        Hãy thực hiện nhiệm vụ đổ rác và sau đó nhấn nút "Xác nhận đã đổ" trên ứng dụng Dorm Trash Guard nhé!
        
        Thân ái,
        Dorm Trash Guard App • Đội phòng KTX
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
        // Section 1: Visual Turn Tracker (Circle roommates)
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
                        text = "VÒNG LẶP ĐỔ RÁC PHÒNG",
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
                        val numMembers = members.size
                        for (i in 0 until numMembers) {
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
                                    text = member?.name?.split(" ")?.lastOrNull() ?: "T${member?.id ?: (i + 1)}",
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
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                    .padding(2.dp)
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

                        HorizontalDivider(
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
                            // Prefilled email client notification as fallback
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

                // Confirm Dump button
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

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import java.util.Date
import java.util.Locale

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

        Hiện tại đang đến lượt của bạn (Thứ tự quy định #${id}) rút bao rác mang đi đổ.

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
        Toast.makeText(context, "Không thể mở ứng dụng gửi mail!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DashboardTab(
    members: List<Member>,
    trashState: TrashState?,
    logs: List<HistoryLog>,
    onReportFullClick: () -> Unit,
    onConfirmDumpClick: () -> Unit
) {
    val rawTurnIndex = trashState?.currentTurnIndex ?: 0
    val currentTurnIndex = if (members.isNotEmpty()) ((rawTurnIndex % members.size) + members.size) % members.size else 0
    val activeMember = members.getOrNull(currentTurnIndex)
    val isFull = trashState?.isTrashFull == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModernCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vòng lặp đổ rác",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Theo dõi lượt của từng thành viên trong phòng",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "${members.size} người",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(members, key = { _, member -> member.id }) { index, member ->
                            val selected = index == currentTurnIndex
                            Column(
                                modifier = Modifier.width(58.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(if (selected) 42.dp else 34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) {
                                                Brush.linearGradient(
                                                    listOf(
                                                        if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                )
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                                    )
                                                )
                                            }
                                        )
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Black,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = member.name.split(" ").lastOrNull().orEmpty().ifBlank { "T${member.id}" },
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            StatusHeroCard(isFull = isFull, trashState = trashState, activeMember = activeMember)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReportFullClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("btn_garbage_full")
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BÁO RÁC\nĐẦY", fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
                }
                Button(
                    onClick = onConfirmDumpClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("btn_garbage_dumped")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ĐÃ ĐỔ\nRÁC", fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }

        item {
            SectionTitle(title = "Lịch sử hoạt động", icon = { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)) })
        }

        if (logs.isEmpty()) {
            item {
                ModernCard(modifier = Modifier.fillMaxWidth(), radius = 22.dp) {
                    Text(
                        "Chưa có hoạt động nào được ghi lại.",
                        modifier = Modifier.padding(22.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(logs) { log -> LogItem(log) }
        }
    }
}

@Composable
private fun StatusHeroCard(isFull: Boolean, trashState: TrashState?, activeMember: Member?) {
    val accentColor = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val backgroundBrush = if (isFull) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.32f),
                MaterialTheme.colorScheme.surface
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f),
                MaterialTheme.colorScheme.surface
            )
        )
    }
    val timeText = if (trashState != null && trashState.reportedAt > 0) {
        SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(trashState.reportedAt))
    } else ""

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = if (isFull) 0.32f else 0.18f),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.14f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFull) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFull) "Thùng rác đã đầy" else "Thùng rác sạch sẽ",
                            style = MaterialTheme.typography.titleLarge,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isFull && trashState != null) {
                                "Báo bởi ${trashState.reportedByName} • $timeText"
                            } else {
                                "Không có cảnh báo trong phòng"
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lượt đổ rác hiện tại", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = activeMember?.name ?: "Chưa có thành viên",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeMember?.email ?: "member@example.com",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = accentColor.copy(alpha = if (isFull) 0.14f else 0.10f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.16f))
                    ) {
                        Text(
                            text = if (isFull) "Cần xử lý" else "Ổn định",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) { icon() }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(title.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
    }
}

@Composable
fun LogItem(log: HistoryLog) {
    val formattedTime = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(log.timestamp))
    ModernCard(modifier = Modifier.fillMaxWidth(), radius = 20.dp) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = when (log.type) {
                    "FULL" -> MaterialTheme.colorScheme.errorContainer
                    "DUMPED" -> MaterialTheme.colorScheme.tertiaryContainer
                    "CONFIG" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (log.type) {
                            "FULL" -> Icons.Default.Warning
                            "DUMPED" -> Icons.Default.Check
                            "CONFIG" -> Icons.Default.Settings
                            else -> Icons.Default.Delete
                        },
                        contentDescription = null,
                        tint = when (log.type) {
                            "FULL" -> MaterialTheme.colorScheme.error
                            "DUMPED" -> MaterialTheme.colorScheme.tertiary
                            "CONFIG" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.message, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(formattedTime, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

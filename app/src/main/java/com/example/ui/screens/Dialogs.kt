package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member

@Composable
fun ReportFullDialog(
    members: List<Member>,
    defaultName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedName by remember(defaultName) { mutableStateOf(defaultName) }
    var localCustomName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        title = { Text("Báo rác đầy", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chọn tên người báo để lưu lịch sử hoạt động.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(modifier = Modifier.heightIn(max = 220.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(members) { member ->
                        val isSelected = selectedName == member.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable {
                                    selectedName = member.name
                                    localCustomName = ""
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(9.dp))
                            Text(member.name, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                OutlinedTextField(
                    value = localCustomName,
                    onValueChange = {
                        localCustomName = it
                        selectedName = ""
                    },
                    label = { Text("Hoặc nhập tên khác") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().testTag("custom_reporter_name")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (localCustomName.isNotBlank()) localCustomName else selectedName
                    if (finalName.isNotBlank()) onConfirm(finalName)
                },
                enabled = selectedName.isNotBlank() || localCustomName.isNotBlank()
            ) { Text("Xác nhận", fontWeight = FontWeight.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun ConfirmDumpDialog(
    members: List<Member>,
    defaultName: String = "",
    activeMember: Member? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val activeName = activeMember?.name.orEmpty()
    val canConfirm = activeMember != null && defaultName.trim() == activeMember.name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(shape = CircleShape, color = if (canConfirm) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(if (canConfirm) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = if (canConfirm) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
                }
            }
        },
        title = { Text("Xác nhận đã đổ rác", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chỉ thành viên đang tới lượt mới được xác nhận. Sau khi xác nhận, lượt sẽ tự chuyển sang người tiếp theo.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!canConfirm) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Bạn chưa tới lượt đổ rác nên không thể xác nhận thay thành viên khác.",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("active_dumper_card"),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activeName.ifBlank { "Chưa xác định thành viên" }, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(activeMember?.let { "Thành viên ${it.id} • ${it.email}" } ?: "Danh sách thành viên chưa sẵn sàng", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (activeName.isNotBlank() && canConfirm) onConfirm(activeName) },
                enabled = activeName.isNotBlank() && canConfirm,
                modifier = Modifier.testTag("confirm_active_dumper_button")
            ) { Text("Đã hoàn thành", fontWeight = FontWeight.Black) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
        shape = RoundedCornerShape(28.dp)
    )
}

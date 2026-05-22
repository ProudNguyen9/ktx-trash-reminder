package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member

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

                // Selectable column for roommates
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

                // Selectable column for roommates
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

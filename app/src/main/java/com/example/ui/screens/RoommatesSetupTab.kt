package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member

// ---------------------- ROOMMATES SETUP TAB ----------------------
@Composable
fun RoommatesSetupTab(
    roomName: String,
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
                        "Cài đặt danh tính và địa chỉ Email nhận thông báo của các thành viên trong phòng kí túc xá.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        itemsIndexed(
            items = editedMembers,
            key = { _, member -> member.id }
        ) { index, member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                member.name.ifBlank { "Thành viên thứ ${member.id}" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if (editedMembers.size > 1) {
                            IconButton(
                                onClick = {
                                    editedMembers.removeAt(index)
                                    hasChanges = true
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("delete_member_${member.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa thành viên",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = member.password,
                        onValueChange = { newVal ->
                            editedMembers[index] = member.copy(password = newVal)
                            hasChanges = true
                        },
                        label = { Text("Mật khẩu riêng (đăng nhập user)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("member_password_input_${member.id}"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val nextId = (editedMembers.map { it.id }.maxOrNull() ?: 0) + 1
                    editedMembers.add(
                        Member(
                            roomName = roomName,
                            id = nextId,
                            name = "Thành viên $nextId",
                            email = "user$nextId@example.com",
                            password = "user123"
                        )
                    )
                    hasChanges = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_member_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, "Thêm")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm thành viên mới", fontWeight = FontWeight.Bold)
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

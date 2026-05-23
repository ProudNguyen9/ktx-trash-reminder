package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.TrashState

@Composable
fun RoommatesSetupTab(
    roomName: String,
    members: List<Member>,
    trashState: TrashState?,
    canEdit: Boolean = true,
    onSaveMembers: (List<Member>) -> Unit,
    onUpdateCurrentTurn: (Int) -> Unit
) {
    val editedMembers = remember(members) { members.toMutableStateList() }
    var hasChanges by remember { mutableStateOf(false) }
    val activeTurnIndex = if (editedMembers.isNotEmpty()) {
        ((trashState?.currentTurnIndex ?: 0) % editedMembers.size + editedMembers.size) % editedMembers.size
    } else {
        -1
    }
    val activeTurnMemberId = editedMembers.getOrNull(activeTurnIndex)?.id

    fun moveMember(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in editedMembers.indices || toIndex !in editedMembers.indices || fromIndex == toIndex) return
        val movingMember = editedMembers.removeAt(fromIndex)
        editedMembers.add(toIndex, movingMember)
        hasChanges = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("roommates_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ModernCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            Icon(if (canEdit) Icons.Default.Person else Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Thành viên phòng", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(
                            if (canEdit) "Sắp xếp lượt, email, mật khẩu và trạng thái vắng."
                            else "Chỉ xem danh sách thành viên, không sửa hoặc xóa.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Surface(
                        modifier = Modifier.widthIn(min = 58.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Text("${editedMembers.size} người", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        itemsIndexed(items = editedMembers, key = { _, member -> member.id }) { index, member ->
            ModernCard(modifier = Modifier.fillMaxWidth(), radius = 26.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                        ) {
                            Text("${index + 1}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name.ifBlank { "Thành viên ${member.id}" }, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Vị trí lượt ${index + 1}/${editedMembers.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (activeTurnMemberId == member.id && !member.isAbsent) {
                                Text("Đang là lượt đổ rác hiện tại", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }
                            if (member.isAbsent) {
                                Text("Đang vắng mặt", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (canEdit && editedMembers.size > 1) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { moveMember(index, index - 1) }, enabled = index > 0, modifier = Modifier.size(30.dp).testTag("move_member_up_${member.id}")) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(21.dp))
                                }
                                IconButton(onClick = { moveMember(index, index + 1) }, enabled = index < editedMembers.lastIndex, modifier = Modifier.size(30.dp).testTag("move_member_down_${member.id}")) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(21.dp))
                                }
                                IconButton(onClick = { editedMembers.remove(member); hasChanges = true }, modifier = Modifier.size(30.dp).testTag("delete_member_${member.id}")) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    MemberInput(
                        value = member.name,
                        onValueChange = { newVal ->
                            val targetIndex = editedMembers.indexOfFirst { it.id == member.id }
                            if (targetIndex != -1) {
                                editedMembers[targetIndex] = member.copy(name = newVal)
                                hasChanges = true
                            }
                        },
                        label = "Họ và tên",
                        icon = { Icon(Icons.Default.Person, null) },
                        enabled = canEdit,
                        modifier = Modifier.testTag("member_name_input_${member.id}")
                    )
                    MemberInput(
                        value = member.email,
                        onValueChange = { newVal ->
                            val targetIndex = editedMembers.indexOfFirst { it.id == member.id }
                            if (targetIndex != -1) {
                                editedMembers[targetIndex] = member.copy(email = newVal)
                                hasChanges = true
                            }
                        },
                        label = "Địa chỉ email",
                        icon = { Icon(Icons.Default.Email, null) },
                        enabled = canEdit,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.testTag("member_email_input_${member.id}")
                    )
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Trạng thái", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (member.isAbsent) "Vắng mặt - bỏ qua lượt" else "Có mặt - nhận lượt bình thường", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = member.isAbsent,
                            onCheckedChange = if (canEdit) {{ checked ->
                                val targetIndex = editedMembers.indexOfFirst { it.id == member.id }
                                if (targetIndex != -1) {
                                    editedMembers[targetIndex] = member.copy(isAbsent = checked)
                                    hasChanges = true
                                }
                            }} else null,
                            modifier = Modifier.testTag("member_absent_switch_${member.id}")
                        )
                    }
                    MemberInput(
                        value = member.password,
                        onValueChange = { newVal ->
                            val targetIndex = editedMembers.indexOfFirst { it.id == member.id }
                            if (targetIndex != -1) {
                                editedMembers[targetIndex] = member.copy(password = newVal)
                                hasChanges = true
                            }
                        },
                        label = "Mật khẩu riêng",
                        icon = { Icon(Icons.Default.Lock, null) },
                        enabled = canEdit,
                        modifier = Modifier.testTag("member_password_input_${member.id}")
                    )
                    if (canEdit) {
                        OutlinedButton(
                            onClick = { onUpdateCurrentTurn(member.id) },
                            enabled = !member.isAbsent && activeTurnMemberId != member.id,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("set_current_turn_${member.id}"),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (activeTurnMemberId == member.id) "Đang là lượt hiện tại" else "Đặt làm lượt đổ rác hiện tại",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (canEdit) {
            item {
                OutlinedButton(
                    onClick = {
                        val nextId = (editedMembers.map { it.id }.maxOrNull() ?: 0) + 1
                        editedMembers.add(Member(roomName = roomName, id = nextId, name = "Thành viên $nextId", email = "user$nextId@example.com", password = "user123", turnOrder = editedMembers.size))
                        hasChanges = true
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("add_member_button"),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm thành viên mới", fontWeight = FontWeight.Black)
                }
            }
            item {
                Button(
                    onClick = {
                        onSaveMembers(editedMembers.mapIndexed { order, member -> member.copy(turnOrder = order) })
                        hasChanges = false
                    },
                    enabled = hasChanges,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(54.dp).testTag("save_members_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu danh sách thành viên", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun MemberInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = icon,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.TrashState

@Composable
fun ConfigTab(
    trashState: TrashState?,
    onSaveFirebase: (String, String, String) -> Unit,
    onSaveResend: (String, String) -> Unit,
    onSaveAdminCredentials: (String, String) -> Unit
) {
    var adminEmail by remember { mutableStateOf(trashState?.adminEmail ?: "nguyenhaohuu9@gmail.com") }
    var adminPassword by remember { mutableStateOf(trashState?.adminPassword ?: "admin999") }

    LaunchedEffect(trashState) {
        if (trashState != null) {
            adminEmail = trashState.adminEmail
            adminPassword = trashState.adminPassword
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("config_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cấu hình hệ thống", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Quản lý email, đồng bộ Firebase và tài khoản admin.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        modifier = Modifier.widthIn(min = 58.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Text("Admin", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            ConfigInfoCard(
                icon = { Icon(Icons.Default.Email, null) },
                title = "Email tự động Gmail SMTP",
                badge = "Active",
                rows = listOf(
                    "Tài khoản gửi thư" to BuildConfig.SENDER_GMAIL_ADDRESS.ifBlank { "Chưa cấu hình" },
                    "SMTP Server" to "smtp.gmail.com • SSL 465",
                    "Link xác nhận nhanh" to BuildConfig.WEB_CONFIRM_URL.ifBlank { "Chưa cấu hình" }
                )
            )
        }

        item {
            ConfigInfoCard(
                icon = { Icon(Icons.Default.Settings, null) },
                title = "Đồng bộ Firebase Cloud",
                badge = "Realtime",
                rows = listOf(
                    "Realtime Database" to BuildConfig.FIREBASE_DB_URL.ifBlank { "Chưa cấu hình" },
                    "Project ID" to BuildConfig.FIREBASE_PROJECT_ID.ifBlank { "Chưa cấu hình" }
                )
            )
        }

        item {
            ModernCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Tài khoản Admin phòng", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Đổi email và mật khẩu quản trị cho phòng hiện tại", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Email Admin mới") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_config_input")
                    )
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Mật khẩu Admin mới") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_config_input")
                    )
                    Button(
                        onClick = { onSaveAdminCredentials(adminEmail, adminPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_admin_credentials_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cập nhật tài khoản Admin", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigInfoCard(
    icon: @Composable () -> Unit,
    title: String,
    badge: String,
    rows: List<Pair<String, String>>
) {
    ModernCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { icon() }
                }
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            rows.forEach { (label, value) ->
                Column {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
        // Announcement Box for Environments
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Quản lý bằng Biến môi trường (.env)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tất cả các dịch vụ nền ảo như Gmail SMTP gửi thư tự động và đồng bộ trực tuyến Firebase Realtime Database hiện đã được bảo mật & tối ưu cấu hình tĩnh thông qua biến môi trường (.env / BuildConfig). Không cần chỉnh sửa trực tiếp trên giao diện để tránh rò rỉ mã bảo mật trên thiết bị.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Card: Gmail SMTP Config Display
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "GỬI EMAIL TỰ ĐỘNG GMAIL SMTP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Active (Env)", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Tài khoản Gmail gửi thư:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BuildConfig.SENDER_GMAIL_ADDRESS.ifBlank { "Chưa cấu hình tài khoản gửi thư (Trống)" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Trực tuyến SMTP Server:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "smtp.gmail.com (Cổng 465 - SSL Bảo mật)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Link hành động xác nhận nhanh (Web Portal):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BuildConfig.WEB_CONFIRM_URL.ifBlank { "Chưa cấu hình link xác nhận nhanh (Trống)" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Card: Firebase Sync Config Display
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ĐỒNG BỘ ĐÁM MÂY (FIREBASE CLOUD)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Active (Env)", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Link Firebase Realtime Database:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BuildConfig.FIREBASE_DB_URL.ifBlank { "Chưa cấu hình cơ sở dữ liệu Firebase (Trống)" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("ID Dự án (Project ID):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BuildConfig.FIREBASE_PROJECT_ID.ifBlank { "Chưa cấu hình mã dự án (Trống)" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Card: Admin setup config
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CẤU HÌNH TÀI KHOẢN ADMIN PHÒNG",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Thiết lập Email Admin và Mật khẩu đăng nhập Admin dành riêng cho phòng của bạn để quản lý thành viên & cài đặt hệ thống.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Email Admin mới") },
                        placeholder = { Text("admin@example.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_config_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Mật khẩu Admin mới") },
                        placeholder = { Text("Mặc định: admin999") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_config_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onSaveAdminCredentials(adminEmail, adminPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_admin_credentials_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cập nhật Tài khoản Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

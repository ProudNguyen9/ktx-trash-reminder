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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrashState

// ---------------------- CONFIG TAB ----------------------
@Composable
fun ConfigTab(
    trashState: TrashState?,
    onSaveFirebase: (String, String, String) -> Unit,
    onSaveResend: (String, String) -> Unit,
    onSaveAdminCredentials: (String, String) -> Unit
) {
    var dbUrl by remember { mutableStateOf(trashState?.firebaseDbUrl ?: "") }
    var dbSecret by remember { mutableStateOf(trashState?.firebaseApiKey ?: "") }
    var dbProjId by remember { mutableStateOf(trashState?.firebaseProjectId ?: "") }
    var resendKey by remember { mutableStateOf(trashState?.resendApiKey ?: "") }
    var webConfirmUrl by remember { mutableStateOf(trashState?.webConfirmUrl ?: "") }
    var adminEmail by remember { mutableStateOf(trashState?.adminEmail ?: "nguyenhaohuu9@gmail.com") }
    var adminPassword by remember { mutableStateOf(trashState?.adminPassword ?: "admin999") }

    // Synchronize local UI state if database loads asynchronously
    LaunchedEffect(trashState) {
        if (trashState != null) {
            dbUrl = trashState.firebaseDbUrl
            dbSecret = trashState.firebaseApiKey
            dbProjId = trashState.firebaseProjectId
            resendKey = trashState.resendApiKey
            webConfirmUrl = trashState.webConfirmUrl
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
                        "Cấu hình Firebase để đồng bộ lượt đổ rác giữa điện thoại của các thành viên trong phòng cùng thời gian thực.",
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

        // Admin credentials config
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

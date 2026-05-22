package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoginMatch
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onRegisterRoom: (String, String, String, String, (Boolean) -> Unit) -> Unit,
    onLoginSuccess: (String, String, String) -> Unit, // (email, role, roomName)
    checkCredentials: suspend (String, String) -> List<LoginMatch>
) {
    val coroutineScope = rememberCoroutineScope()

    var isRegisterMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var matchesList by remember { mutableStateOf<List<LoginMatch>?>(null) }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Register Form State
    var regRoomName by remember { mutableStateOf("") }
    var regAdminName by remember { mutableStateOf("") }
    var regAdminEmail by remember { mutableStateOf("") }
    var regAdminPassword by remember { mutableStateOf("") }

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
                .widthIn(max = 420.dp),
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
                // Header emblem
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRegisterMode) Icons.Default.AddCircle else Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dorm Trash Guard",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Hệ thống Quản lý Rác KTX Phòng",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (matchesList != null) {
                    // Room selection screen for multi-room matches
                    Text(
                        text = "Phát hiện nhiều phòng trùng khớp!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Vui lòng chọn phòng bạn muốn đăng nhập dưới đây:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matchesList!!.forEach { match ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onLoginSuccess(loginEmail.trim().lowercase(), match.role, match.roomName)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Room Icon",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = match.roomName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Admin: ${match.memberName}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = if (match.role == "admin") "Admin 👑" else "Thành viên 🧑",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (match.role == "admin") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = { matchesList = null }) {
                        Text("Quay lại màn hình đăng nhập")
                    }

                } else if (!isRegisterMode) {
                    // --- LOGIN PORTION ---
                    // Email Input
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it
                            errorMessage = null
                        },
                        label = { Text("Email hoặc tài khoản") },
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
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
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
                            val emailNorm = loginEmail.trim().lowercase()
                            val passNorm = loginPassword.trim()

                            if (emailNorm.isEmpty()) {
                                errorMessage = "Vui lòng nhập Email!"
                                return@Button
                            }
                            if (passNorm.isEmpty()) {
                                errorMessage = "Vui lòng nhập mật khẩu!"
                                return@Button
                            }

                            coroutineScope.launch {
                                val matches = checkCredentials(emailNorm, passNorm)
                                if (matches.isEmpty()) {
                                    errorMessage = "Email hoặc mật khẩu không chính xác!"
                                } else if (matches.size == 1) {
                                    // Exactly one matching room => enter directly
                                    val match = matches.first()
                                    onLoginSuccess(emailNorm, match.role, match.roomName)
                                } else {
                                    // Multiple rooms matched => show selector
                                    matchesList = matches
                                }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chưa có phòng?",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đăng ký ngay tại đây",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    isRegisterMode = true
                                    errorMessage = null
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guide Info Box
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
                                text = "💡 Hướng dẫn đăng nhập phòng:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Nhập email & mật khẩu đã đăng ký để vào phòng. Bạn sẽ được tự động nhận đúng quyền (Admin hoặc thành viên).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• Mặc định khởi tạo: Phòng 'Phòng D514' / Email 'nguyenhaohuu9@gmail.com' / Pass 'admin999'.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                } else {
                    // --- REGISTER ADMIN & ROOM PORTION ---
                    OutlinedTextField(
                        value = regRoomName,
                        onValueChange = {
                            regRoomName = it
                            errorMessage = null
                        },
                        label = { Text("Tên phòng đăng ký") },
                        placeholder = { Text("Ví dụ: Phòng D514") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Room Name Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_room_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regAdminName,
                        onValueChange = {
                            regAdminName = it
                            errorMessage = null
                        },
                        label = { Text("Họ tên Trưởng Phòng / Admin") },
                        placeholder = { Text("Ví dụ: Nguyễn Hữu Hào") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Person Name Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regAdminEmail,
                        onValueChange = {
                            regAdminEmail = it
                            errorMessage = null
                        },
                        label = { Text("Email Admin đăng ký") },
                        placeholder = { Text("example@gmail.com") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Admin Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regAdminPassword,
                        onValueChange = {
                            regAdminPassword = it
                            errorMessage = null
                        },
                        label = { Text("Mật khẩu Admin") },
                        placeholder = { Text("Tối thiểu 6 ký tự") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Pass Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input"),
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

                    Button(
                        onClick = {
                            val rName = regRoomName.trim()
                            val aName = regAdminName.trim()
                            val aEmail = regAdminEmail.trim()
                            val aPass = regAdminPassword.trim()

                            if (rName.isEmpty()) {
                                errorMessage = "Vui lòng nhập tên phòng!"
                                return@Button
                            }
                            if (aName.isEmpty()) {
                                errorMessage = "Vui lòng nhập họ tên Admin!"
                                return@Button
                            }
                            if (aEmail.isEmpty()) {
                                errorMessage = "Vui lòng nhập Email Admin!"
                                return@Button
                            }
                            if (aPass.length < 6) {
                                errorMessage = "Mật khẩu Admin phải tối thiểu 6 ký tự!"
                                return@Button
                            }

                            onRegisterRoom(rName, aName, aEmail, aPass) { success ->
                                if (success) {
                                    // Successfully registered & active room set. Auto log in.
                                    onLoginSuccess(aEmail.lowercase(), "admin", rName)
                                } else {
                                    errorMessage = "Đăng ký lỗi! Phòng này đã có người đăng ký, hoặc tên phòng trống!"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ĐĂNG KÝ PHÒNG & ADMIN 📝",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        isRegisterMode = false
                        errorMessage = null
                    }) {
                        Text("Quay lại màn hình đăng nhập")
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoginMatch
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onRegisterRoom: (String, String, String, String, (Boolean) -> Unit) -> Unit,
    onLoginSuccess: (String, String, String) -> Unit,
    checkCredentials: suspend (String, String) -> List<LoginMatch>
) {
    val coroutineScope = rememberCoroutineScope()
    var isRegisterMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var matchesList by remember { mutableStateOf<List<LoginMatch>?>(null) }

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var regRoomName by remember { mutableStateOf("") }
    var regAdminName by remember { mutableStateOf("") }
    var regAdminEmail by remember { mutableStateOf("") }
    var regAdminPassword by remember { mutableStateOf("") }

    ModernAppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ModernCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp),
                radius = 32.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginHeader(isRegisterMode = isRegisterMode)
                    Spacer(modifier = Modifier.height(22.dp))

                    AnimatedContent(
                        targetState = matchesList != null,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "login_content"
                    ) { choosingRoom ->
                        when {
                            choosingRoom -> RoomPickerContent(
                                matches = matchesList.orEmpty(),
                                email = loginEmail,
                                onPick = { match -> onLoginSuccess(loginEmail.trim().lowercase(), match.role, match.roomName) },
                                onBack = { matchesList = null }
                            )

                            !isRegisterMode -> LoginFormContent(
                                email = loginEmail,
                                password = loginPassword,
                                passwordVisible = passwordVisible,
                                errorMessage = errorMessage,
                                onEmailChange = {
                                    loginEmail = it
                                    errorMessage = null
                                },
                                onPasswordChange = {
                                    loginPassword = it
                                    errorMessage = null
                                },
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                onSubmit = {
                                    val emailNorm = loginEmail.trim().lowercase()
                                    val passNorm = loginPassword.trim()
                                    when {
                                        emailNorm.isEmpty() -> errorMessage = "Vui lòng nhập email hoặc tài khoản!"
                                        passNorm.isEmpty() -> errorMessage = "Vui lòng nhập mật khẩu!"
                                        else -> coroutineScope.launch {
                                            val matches = checkCredentials(emailNorm, passNorm)
                                            when {
                                                matches.isEmpty() -> errorMessage = "Email hoặc mật khẩu không chính xác!"
                                                matches.size == 1 -> {
                                                    val match = matches.first()
                                                    onLoginSuccess(emailNorm, match.role, match.roomName)
                                                }
                                                else -> matchesList = matches
                                            }
                                        }
                                    }
                                },
                                onGoRegister = {
                                    isRegisterMode = true
                                    errorMessage = null
                                }
                            )

                            else -> RegisterFormContent(
                                roomName = regRoomName,
                                adminName = regAdminName,
                                adminEmail = regAdminEmail,
                                adminPassword = regAdminPassword,
                                passwordVisible = passwordVisible,
                                errorMessage = errorMessage,
                                onRoomNameChange = {
                                    regRoomName = it
                                    errorMessage = null
                                },
                                onAdminNameChange = {
                                    regAdminName = it
                                    errorMessage = null
                                },
                                onAdminEmailChange = {
                                    regAdminEmail = it
                                    errorMessage = null
                                },
                                onAdminPasswordChange = {
                                    regAdminPassword = it
                                    errorMessage = null
                                },
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                onSubmit = {
                                    val rName = regRoomName.trim()
                                    val aName = regAdminName.trim()
                                    val aEmail = regAdminEmail.trim()
                                    val aPass = regAdminPassword.trim()
                                    when {
                                        rName.isEmpty() -> errorMessage = "Vui lòng nhập tên phòng!"
                                        aName.isEmpty() -> errorMessage = "Vui lòng nhập họ tên Admin!"
                                        aEmail.isEmpty() -> errorMessage = "Vui lòng nhập Email Admin!"
                                        aPass.length < 6 -> errorMessage = "Mật khẩu Admin phải tối thiểu 6 ký tự!"
                                        else -> onRegisterRoom(rName, aName, aEmail, aPass) { success ->
                                            if (success) {
                                                onLoginSuccess(aEmail.lowercase(), "admin", rName)
                                            } else {
                                                errorMessage = "Đăng ký lỗi! Phòng này đã tồn tại hoặc tên phòng trống."
                                            }
                                        }
                                    }
                                },
                                onBackLogin = {
                                    isRegisterMode = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "© 2026 Dorm Trash Guard • KTX xanh, sạch, gọn",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoginHeader(isRegisterMode: Boolean) {
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isRegisterMode) Icons.Default.Add else Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(38.dp)
        )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        text = "Dorm Trash Guard",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
    Text(
        text = if (isRegisterMode) "Tạo phòng KTX và quản lý đội nhóm" else "Hệ thống quản lý rác KTX phòng",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LoginFormContent(
    email: String,
    password: String,
    passwordVisible: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onGoRegister: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ModernInput(
            value = email,
            onValueChange = onEmailChange,
            label = "Email hoặc tài khoản",
            placeholder = "example@gmail.com",
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.testTag("login_email_input")
        )
        ModernInput(
            value = password,
            onValueChange = onPasswordChange,
            label = "Mật khẩu",
            placeholder = "••••••••",
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = onTogglePassword) {
                    Text(if (passwordVisible) "Ẩn" else "Hiện", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.testTag("login_password_input")
        )
        ErrorText(errorMessage)
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text("ĐĂNG NHẬP", fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Chưa có phòng?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "Đăng ký ngay",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onGoRegister)
            )
        }
        LoginTipCard()
    }
}

@Composable
private fun RegisterFormContent(
    roomName: String,
    adminName: String,
    adminEmail: String,
    adminPassword: String,
    passwordVisible: Boolean,
    errorMessage: String?,
    onRoomNameChange: (String) -> Unit,
    onAdminNameChange: (String) -> Unit,
    onAdminEmailChange: (String) -> Unit,
    onAdminPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onBackLogin: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier
                .size(68.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
        }
        ModernInput(roomName, onRoomNameChange, "Tên phòng đăng ký", "Phòng D514", { Icon(Icons.Default.Home, null) }, Modifier.testTag("register_room_input"))
        ModernInput(adminName, onAdminNameChange, "Họ tên Trưởng phòng / Admin", "Nguyễn Văn A", { Icon(Icons.Default.Person, null) }, Modifier.testTag("register_name_input"))
        ModernInput(adminEmail, onAdminEmailChange, "Email Admin đăng ký", "admin@gmail.com", { Icon(Icons.Default.Email, null) }, Modifier.testTag("register_email_input"))
        ModernInput(
            value = adminPassword,
            onValueChange = onAdminPasswordChange,
            label = "Mật khẩu Admin",
            placeholder = "Tối thiểu 6 ký tự",
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = onTogglePassword) {
                    Text(if (passwordVisible) "Ẩn" else "Hiện", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.testTag("register_password_input")
        )
        ErrorText(errorMessage)
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("register_submit_button"),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text("ĐĂNG KÝ PHÒNG & ADMIN", fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
        TextButton(onClick = onBackLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Quay lại màn hình đăng nhập", fontWeight = FontWeight.Bold)
        }
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f))
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Thông tin đăng ký được duyệt bởi ban quản lý KTX và đồng bộ theo phòng.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun RoomPickerContent(
    matches: List<LoginMatch>,
    email: String,
    onPick: (LoginMatch) -> Unit,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Chọn phòng để tiếp tục",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = email.ifBlank { "Tài khoản của bạn" },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            modifier = Modifier.heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            matches.forEach { match ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(match) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(match.roomName, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(match.memberName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        SuggestionChip(
                            onClick = { onPick(match) },
                            label = { Text(if (match.role == "admin") "Admin" else "User", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (match.role == "admin") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = if (match.role == "admin") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                }
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Quay lại đăng nhập", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ModernInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ErrorText(errorMessage: String?) {
    if (errorMessage != null) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
private fun LoginTipCard() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Hướng dẫn đăng nhập", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    "Nhập email và mật khẩu được cấp theo phòng. App tự nhận quyền Admin hoặc User.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

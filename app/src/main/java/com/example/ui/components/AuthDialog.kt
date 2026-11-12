package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.User
import com.example.ui.theme.AmberBookmark
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldLearned
import com.example.ui.theme.Pd
import com.example.ui.theme.PdSize
import com.example.ui.theme.PdSpacing
import com.example.ui.theme.PdShapes
import com.example.ui.theme.PdType

private enum class AuthMode { LOGIN, REGISTER, FORGOT }

/**
 * درخواست ورود با گوگل.
 *
 * امضای نام‌دار دارد تا فراخوانی در کد خوانا بماند؛ روی یک fun interface سوار می‌شود
 * چون آرگومان‌های نام‌دار روی نوع‌های تابع مجاز نیستند.
 */
fun interface GoogleLoginRequester {
    /**
     * @param onSuccess ورود کامل شد.
     * @param onError ورود ناموفق بود و [String] پیام قابل‌نمایش است.
     * @param onCancel کاربر انتخابگر را بست یا پشت برد؛ نباید خطایی نشان داده شود.
     */
    fun request(onSuccess: () -> Unit, onError: (String) -> Unit, onCancel: () -> Unit)
}

@Composable
fun AuthDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onLogin: (String, String, () -> Unit, (String) -> Unit) -> Unit,
    onRegister: (String, String, String?, () -> Unit, (String) -> Unit) -> Unit,
    onForgotPassword: (String, (String?) -> Unit, (String) -> Unit) -> Unit,
    onResetPassword: (String, String, String, (String?) -> Unit, (String) -> Unit) -> Unit,
    onGoogleLogin: GoogleLoginRequester = GoogleLoginRequester { success, _, _ -> success() }
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // دریافت نشانه‌ی گوگل (و انتظار برای باز شدن انتخابگر حساب) را جدا نشان
    // می‌دهیم تا روی اولین تلاش — که Play Services سرد است — دکمه خاموش نماند.
    var googleLoading by remember { mutableStateOf(false) }

    fun resetError() {
        errorMessage = null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = PdShapes.xxl,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(PdSpacing.sm)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(PdSpacing.xl)
            ) {
                // Header: Clinical Brand Icon + Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.compact)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(PdShapes.md)
                                .background(CrimsonPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    AuthMode.LOGIN -> Icons.Default.AccountCircle
                                    AuthMode.REGISTER -> Icons.Default.PersonAdd
                                    AuthMode.FORGOT -> Icons.Default.Key
                                },
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = when (mode) {
                                    AuthMode.LOGIN -> "ورود به حساب کاربری"
                                    AuthMode.REGISTER -> "ثبت‌نام کاربر جدید"
                                    AuthMode.FORGOT -> "بازیابی رمز عبور"
                                },
                                fontSize = PdType.sp16,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "سامانه تخصصی PathoDex",
                                fontSize = PdType.sp11,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.lg))

                // Modern Pill Tab Switcher (hidden in forgot mode)
                if (mode != AuthMode.FORGOT) {
                    Surface(
                        shape = PdShapes.md,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PdSpacing.xs)
                        ) {
                            // Login Tab Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(PdShapes.md)
                                    .background(if (mode == AuthMode.LOGIN) CrimsonPrimary else Color.Transparent)
                                    .clickable {
                                        mode = AuthMode.LOGIN
                                        resetError()
                                    }
                                    .testTag("tab_login"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        tint = if (mode == AuthMode.LOGIN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "ورود به سیستم",
                                        fontSize = PdType.sp12_5,
                                        fontWeight = if (mode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                                        color = if (mode == AuthMode.LOGIN) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Register Tab Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(PdShapes.md)
                                    .background(if (mode == AuthMode.REGISTER) CrimsonPrimary else Color.Transparent)
                                    .clickable {
                                        mode = AuthMode.REGISTER
                                        resetError()
                                    }
                                    .testTag("tab_register"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = if (mode == AuthMode.REGISTER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "ثبت‌نام جدید",
                                        fontSize = PdType.sp12_5,
                                        fontWeight = if (mode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium,
                                        color = if (mode == AuthMode.REGISTER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PdSpacing.lg))
                }

                when (mode) {
                    AuthMode.LOGIN -> {
                        // Identifier (username or email)
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                resetError()
                            },
                            label = { Text("نام کاربری یا ایمیل", fontSize = PdType.sp12) },
                            placeholder = { Text("مثال: doctor_alavi یا you@mail.com", fontSize = PdType.sp11_5) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = PdShapes.md,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_username_input")
                        )

                        Spacer(modifier = Modifier.height(PdSpacing.md))

                        PasswordField(
                            value = password,
                            onValueChange = { password = it; resetError() },
                            label = "رمز عبور (حداقل ۶ کاراکتر)",
                            placeholder = "رمز عبور امن",
                            visible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible },
                            testTag = "auth_password_input"
                        )

                        Spacer(modifier = Modifier.height(PdSpacing.tight))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                mode = AuthMode.FORGOT
                                codeSent = false
                                code = ""
                                newPassword = ""
                                resetError()
                            }) {
                                Text(
                                    text = "رمز عبور را فراموش کرده‌اید؟",
                                    fontSize = PdType.sp11_5,
                                    fontWeight = FontWeight.Medium,
                                    color = CrimsonPrimary
                                )
                            }
                        }
                    }

                    AuthMode.REGISTER -> {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                resetError()
                            },
                            label = { Text("نام کاربری (انگلیسی)", fontSize = PdType.sp12) },
                            placeholder = { Text("مثال: doctor_alavi", fontSize = PdType.sp11_5) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            shape = PdShapes.md,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_username_input")
                        )

                        Spacer(modifier = Modifier.height(PdSpacing.md))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                resetError()
                            },
                            label = { Text("ایمیل (اختیاری، برای بازیابی رمز)", fontSize = PdType.sp12) },
                            placeholder = { Text("you@mail.com", fontSize = PdType.sp11_5) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = PdShapes.md,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(PdSpacing.md))

                        PasswordField(
                            value = password,
                            onValueChange = { password = it; resetError() },
                            label = "رمز عبور (حداقل ۶ کاراکتر)",
                            placeholder = "رمز عبور امن",
                            visible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible },
                            testTag = "auth_password_input"
                        )
                    }

                    AuthMode.FORGOT -> {
                        Text(
                            text = "ایمیل تأییدشده‌ی حساب خود را وارد کنید تا کد بازیابی ارسال شود.",
                            fontSize = PdType.sp11_5,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(PdSpacing.md))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                resetError()
                            },
                            label = { Text("ایمیل حساب کاربری", fontSize = PdType.sp12) },
                            placeholder = { Text("you@mail.com", fontSize = PdType.sp11_5) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = PdShapes.md,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_forgot_email_input")
                        )

                        AnimatedVisibility(visible = codeSent, enter = fadeIn(), exit = fadeOut()) {
                            Column {
                                Spacer(modifier = Modifier.height(PdSpacing.md))

                                OutlinedTextField(
                                    value = code,
                                    onValueChange = {
                                        code = it.filter { ch -> ch.isDigit() }.take(8)
                                        resetError()
                                    },
                                    label = { Text("کد بازیابی (۸ رقم)", fontSize = PdType.sp12) },
                                    placeholder = { Text("کد ارسال‌شده به ایمیل", fontSize = PdType.sp11_5) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = CrimsonPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    shape = PdShapes.md,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonPrimary,
                                        focusedLabelColor = CrimsonPrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_forgot_code_input")
                                )

                                Spacer(modifier = Modifier.height(PdSpacing.md))

                                PasswordField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it; resetError() },
                                    label = "رمز عبور جدید (حداقل ۶ کاراکتر)",
                                    placeholder = "رمز عبور جدید",
                                    visible = passwordVisible,
                                    onToggleVisibility = { passwordVisible = !passwordVisible },
                                    testTag = "auth_forgot_newpass_input"
                                )
                            }
                        }
                    }
                }

                // Error Banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = PdShapes.md,
                        color = Pd.palette.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = PdSpacing.compact)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm),
                            modifier = Modifier.padding(horizontal = PdSpacing.md, vertical = PdSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Pd.palette.error,
                                modifier = Modifier.size(PdSize.iconSm)
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = Pd.palette.onErrorContainer,
                                fontSize = PdType.sp11_5,
                                fontWeight = FontWeight.Medium,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                // Submit Button
                Button(
                    onClick = {
                        when (mode) {
                            AuthMode.LOGIN -> {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "لطفاً نام کاربری (یا ایمیل) و رمز عبور را وارد کنید"
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "رمز عبور باید حداقل ۶ کاراکتر باشد"
                                    return@Button
                                }
                                onLogin(username.trim(), password, { onDismiss() }, { errorMessage = it })
                            }
                            AuthMode.REGISTER -> {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "لطفاً نام کاربری و رمز عبور را وارد کنید"
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "رمز عبور باید حداقل ۶ کاراکتر باشد"
                                    return@Button
                                }
                                onRegister(
                                    username.trim(),
                                    password,
                                    email.trim().takeIf { it.isNotEmpty() },
                                    { onDismiss() },
                                    { errorMessage = it }
                                )
                            }
                            AuthMode.FORGOT -> {
                                if (!codeSent) {
                                    if (email.isBlank()) {
                                        errorMessage = "لطفاً ایمیل حساب خود را وارد کنید"
                                        return@Button
                                    }
                                    onForgotPassword(email, { codeSent = true }, { errorMessage = it })
                                } else {
                                    if (code.isBlank() || newPassword.length < 6) {
                                        errorMessage = "کد بازیابی و رمز عبور جدید (حداقل ۶ کاراکتر) الزامی است"
                                        return@Button
                                    }
                                    onResetPassword(email, code, newPassword, { onDismiss() }, { errorMessage = it })
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = PdShapes.md,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(PdSpacing.sm))
                        Text(
                            text = "در حال ارتباط با سرور...",
                            fontSize = PdType.sp12_5,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    AuthMode.LOGIN -> Icons.AutoMirrored.Filled.Login
                                    AuthMode.REGISTER -> Icons.Default.PersonAdd
                                    AuthMode.FORGOT -> Icons.Default.Key
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when (mode) {
                                    AuthMode.LOGIN -> "ورود به حساب کاربری"
                                    AuthMode.REGISTER -> "ثبت‌نام و ورود خودکار"
                                    AuthMode.FORGOT -> if (codeSent) "تعیین رمز عبور جدید" else "ارسال کد بازیابی"
                                },
                                fontSize = PdType.sp13,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                // ورود سریع با گوگل (فقط در حالت‌های ورود و ثبت‌نام)
                if (mode != AuthMode.FORGOT) {
                    PdGoogleButton(
                        isLoading = googleLoading || isLoading,
                        onClick = {
                            resetError()
                            googleLoading = true
                            onGoogleLogin.request(
                                onSuccess = {
                                    googleLoading = false
                                    onDismiss()
                                },
                                onError = { message ->
                                    googleLoading = false
                                    errorMessage = message
                                },
                                // لغو کاربر خطا نیست؛ فقط دکمه دوباره فعال می‌شود
                                onCancel = { googleLoading = false }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_google_button")
                    )
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                // Bottom Switcher helper link
                if (mode != AuthMode.FORGOT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mode == AuthMode.LOGIN) "حساب کاربری ندارید؟" else "قبلاً ثبت‌نام کرده‌اید؟",
                            fontSize = PdType.sp11_5,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                                resetError()
                            }
                        ) {
                            Text(
                                text = if (mode == AuthMode.LOGIN) "ایجاد حساب جدید" else "ورود به سیستم",
                                fontSize = PdType.sp12,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            mode = AuthMode.LOGIN
                            resetError()
                        }) {
                            Text(
                                text = "بازگشت به ورود",
                                fontSize = PdType.sp12,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = PdType.sp12) },
        placeholder = { Text(placeholder, fontSize = PdType.sp11_5) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = CrimsonPrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "نمایش رمز",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = PdShapes.md,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CrimsonPrimary,
            focusedLabelColor = CrimsonPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
fun ProfileDialog(
    user: User,
    learnedCount: Int,
    bookmarkCount: Int,
    favoriteCount: Int,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onUpdatePassword: (old: String, new: String, () -> Unit, (String) -> Unit) -> Unit,
    onChangeUsername: (String, () -> Unit, (String) -> Unit) -> Unit,
    onSendEmailCode: (String, (String?) -> Unit, (String) -> Unit) -> Unit,
    onVerifyEmail: (String, String, (String?) -> Unit, (String) -> Unit) -> Unit,
    onResetPassword: (String, String, String, () -> Unit, (String) -> Unit) -> Unit
) {
    var newUsername by remember(user.username) { mutableStateOf(user.username) }
    var email by remember(user.email) { mutableStateOf(user.email ?: "") }
    var emailCode by remember { mutableStateOf("") }
    var emailCodeSent by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var forgotMode by remember { mutableStateOf(false) }
    var resetCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = PdShapes.xxl,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(PdSpacing.sm)
                .testTag("profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(PdSpacing.xl)
            ) {
                // Header with User Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.md)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(PdShapes.md)
                                .background(CrimsonPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (user.role == "owner") Icons.Default.Shield else Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = CrimsonPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = user.username,
                                fontSize = PdType.sp16,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val roleLabel = when (user.role) {
                                "owner" -> "مالک سامانه (Owner)"
                                "admin" -> "مدیر علمی (Admin)"
                                else -> "کاربر بالینی و پژوهشگر"
                            }
                            Text(
                                text = roleLabel,
                                fontSize = PdType.sp11_5,
                                color = CrimsonPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                // Stats Dashboard Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PdShapes.lg)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = PdSpacing.md, horizontal = PdSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)) {
                            Icon(Icons.Default.School, contentDescription = null, tint = EmeraldLearned, modifier = Modifier.size(15.dp))
                            Text(text = "$learnedCount", fontSize = PdType.sp16, fontWeight = FontWeight.Bold, color = EmeraldLearned)
                        }
                        Spacer(modifier = Modifier.height(PdSpacing.xxs))
                        Text(text = "یادگرفته‌شده", fontSize = PdType.sp10_5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = AmberBookmark, modifier = Modifier.size(15.dp))
                            Text(text = "$bookmarkCount", fontSize = PdType.sp16, fontWeight = FontWeight.Bold, color = AmberBookmark)
                        }
                        Spacer(modifier = Modifier.height(PdSpacing.xxs))
                        Text(text = "نشان‌گذاری", fontSize = PdType.sp10_5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs)) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(15.dp))
                            Text(text = "$favoriteCount", fontSize = PdType.sp16, fontWeight = FontWeight.Bold, color = CrimsonPrimary)
                        }
                        Spacer(modifier = Modifier.height(PdSpacing.xxs))
                        Text(text = "علاقه‌مندی", fontSize = PdType.sp10_5, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                // ===== Change Username =====
                SectionTitle(icon = { Icon(Icons.Default.Person, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp)) }, title = "تغییر نام کاربری")

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it; errorMsg = null; successMsg = null },
                        label = { Text("نام کاربری جدید", fontSize = PdType.sp11_5) },
                        singleLine = true,
                        shape = PdShapes.md,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            focusedLabelColor = CrimsonPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            isBusy = true
                            onChangeUsername(newUsername, {
                                isBusy = false
                                successMsg = "نام کاربری با موفقیت تغییر کرد"
                                errorMsg = null
                            }, {
                                isBusy = false
                                errorMsg = it
                            })
                        },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = PdShapes.md,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(text = "ذخیره", fontSize = PdType.sp12, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                // ===== Email =====
                SectionTitle(icon = { Icon(Icons.Default.Email, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp)) }, title = "ایمیل حساب")

                Spacer(modifier = Modifier.height(PdSpacing.tight))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)) {
                    if (user.email_verified == true && !user.email.isNullOrBlank()) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLearned, modifier = Modifier.size(14.dp))
                        Text(
                            text = "ایمیل تأییدشده: ${user.email}",
                            fontSize = PdType.sp11_5,
                            color = EmeraldLearned,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (!user.email.isNullOrBlank()) {
                        Text(
                            text = "تأییدنشده: ${user.email}",
                            fontSize = PdType.sp11_5,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "ایمیلی به حساب شما متصل نیست",
                            fontSize = PdType.sp11_5,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = null; successMsg = null },
                    label = { Text("ایمیل جدید", fontSize = PdType.sp11_5) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = PdShapes.md,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        focusedLabelColor = CrimsonPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = emailCodeSent) {
                    Column {
                        Spacer(modifier = Modifier.height(PdSpacing.sm))
                        OutlinedTextField(
                            value = emailCode,
                            onValueChange = { emailCode = it.filter { ch -> ch.isDigit() }.take(8); errorMsg = null; successMsg = null },
                            label = { Text("کد تأیید (۸ رقم)", fontSize = PdType.sp11_5) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = PdShapes.md,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                focusedLabelColor = CrimsonPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PdSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (email.isBlank()) {
                                errorMsg = "لطفاً یک ایمیل وارد کنید"
                                return@OutlinedButton
                            }
                            isBusy = true
                            onSendEmailCode(email, {
                                isBusy = false
                                emailCodeSent = true
                                successMsg = "کد تأیید به ایمیل شما ارسال شد"
                                errorMsg = null
                            }, {
                                isBusy = false
                                errorMsg = it
                            })
                        },
                        enabled = !isBusy,
                        shape = PdShapes.md,
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(text = "ارسال کد تأیید", fontSize = PdType.sp11_5, fontWeight = FontWeight.Bold)
                    }

                    if (emailCodeSent) {
                        Button(
                            onClick = {
                                if (emailCode.isBlank()) {
                                    errorMsg = "کد تأیید را وارد کنید"
                                    return@Button
                                }
                                isBusy = true
                                onVerifyEmail(email, emailCode, {
                                    isBusy = false
                                    successMsg = "ایمیل شما با موفقیت تأیید شد"
                                    errorMsg = null
                                }, {
                                    isBusy = false
                                    errorMsg = it
                                })
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = PdShapes.md,
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(text = "تأیید ایمیل", fontSize = PdType.sp11_5, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.roomy))

                // ===== Change Password =====
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(icon = { Icon(Icons.Default.Key, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp)) }, title = "تغییر رمز عبور")

                    val canForgot = !user.email.isNullOrBlank() && user.email_verified == true
                    TextButton(
                        onClick = { forgotMode = !forgotMode; errorMsg = null; successMsg = null },
                        enabled = canForgot
                    ) {
                        Text(
                            text = if (forgotMode) "تغییر با رمز فعلی" else "رمز را فراموش کرده‌ام",
                            fontSize = PdType.sp10_5,
                            fontWeight = FontWeight.Bold,
                            color = if (canForgot) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!user.email.isNullOrBlank() && user.email_verified != true) {
                    Text(
                        text = "برای بازیابی رمز از طریق ایمیل، ابتدا ایمیل حساب را تأیید کنید.",
                        fontSize = PdType.sp10_5,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = PdSpacing.xs)
                    )
                }

                Spacer(modifier = Modifier.height(PdSpacing.sm))

                if (!forgotMode) {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it; errorMsg = null; successMsg = null },
                        label = { Text("رمز عبور فعلی", fontSize = PdType.sp11_5) },
                        singleLine = true,
                        shape = PdShapes.md,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            focusedLabelColor = CrimsonPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.sm))
                } else {
                    if (!user.email.isNullOrBlank()) {
                        Text(
                            text = "کد بازیابی به ایمیل تأییدشده‌ی ${user.email} ارسال می‌شود.",
                            fontSize = PdType.sp10_5,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(PdSpacing.sm))
                    }

                    OutlinedTextField(
                        value = resetCode,
                        onValueChange = { resetCode = it.filter { ch -> ch.isDigit() }.take(8); errorMsg = null; successMsg = null },
                        label = { Text("کد بازیابی (۸ رقم)", fontSize = PdType.sp11_5) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = PdShapes.md,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            focusedLabelColor = CrimsonPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(PdSpacing.sm))
                }

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it; errorMsg = null; successMsg = null },
                    label = { Text("رمز عبور جدید (حداقل ۶ کاراکتر)", fontSize = PdType.sp11_5) },
                    singleLine = true,
                    shape = PdShapes.md,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        focusedLabelColor = CrimsonPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = CrimsonPrimary,
                        fontSize = PdType.sp11,
                        modifier = Modifier.padding(top = PdSpacing.tight)
                    )
                }

                if (successMsg != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.xs),
                        modifier = Modifier.padding(top = PdSpacing.tight)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLearned, modifier = Modifier.size(14.dp))
                        Text(
                            text = successMsg ?: "",
                            color = EmeraldLearned,
                            fontSize = PdType.sp11,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.cozy))

                Button(
                    onClick = {
                        if (newPass.length < 6) {
                            errorMsg = "رمز عبور جدید باید حداقل ۶ کاراکتر باشد"
                            return@Button
                        }
                        isBusy = true
                        if (forgotMode) {
                            val targetEmail = user.email?.takeIf { it.isNotBlank() }
                            if (targetEmail == null || resetCode.isBlank()) {
                                isBusy = false
                                errorMsg = "برای بازیابی رمز، ایمیل تأییدشده و کد بازیابی لازم است"
                                return@Button
                            }
                            onResetPassword(targetEmail, resetCode, newPass, {
                                isBusy = false
                                successMsg = "رمز عبور با موفقیت بازیابی شد"
                                resetCode = ""
                                newPass = ""
                                errorMsg = null
                            }, {
                                isBusy = false
                                errorMsg = it
                            })
                        } else {
                            if (oldPass.isBlank()) {
                                isBusy = false
                                errorMsg = "رمز عبور فعلی را وارد کنید"
                                return@Button
                            }
                            onUpdatePassword(oldPass, newPass, {
                                isBusy = false
                                successMsg = "رمز عبور با موفقیت بروزرسانی شد"
                                oldPass = ""
                                newPass = ""
                                errorMsg = null
                            }, {
                                isBusy = false
                                errorMsg = it
                            })
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = PdShapes.md,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = if (forgotMode) "بازیابی رمز عبور" else "بروزرسانی رمز عبور", fontSize = PdType.sp12, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(PdSpacing.md))

                Button(
                    onClick = { onLogout(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary.copy(alpha = 0.12f)),
                    shape = PdShapes.md,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("logout_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "خروج از حساب کاربری",
                            fontSize = PdType.sp12,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: @Composable () -> Unit, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PdSpacing.tight)
    ) {
        icon()
        Text(
            text = title,
            fontSize = PdType.sp12_5,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

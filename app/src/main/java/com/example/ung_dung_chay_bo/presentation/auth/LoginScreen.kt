package com.example.ung_dung_chay_bo.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.components.AppLogoBadge
import com.example.ung_dung_chay_bo.core.components.GoogleLogoIcon
import com.example.ung_dung_chay_bo.core.theme.LightBorder
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onLoginErrorConsumed()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1E293B),
                    contentColor = PureWhite,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = PureWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PureWhite)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            AppLogoBadge(size = 56.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Đăng nhập",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Chào mừng bạn trở lại với đường chạy",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            val fieldShape = RoundedCornerShape(14.dp)
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SportOrange,
                unfocusedBorderColor = LightBorder,
                focusedLabelColor = SportOrange,
                unfocusedLabelColor = TextSecondary,
                cursorColor = SportOrange,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onLoginEmailChange,
                label = { Text("Email") },
                placeholder = { Text("name@example.com", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onLoginPasswordChange,
                label = { Text("Mật khẩu") },
                placeholder = { Text("Nhập mật khẩu", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::onLoginPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.loginWithEmail()
                    }
                ),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.loginWithEmail()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportOrange,
                    contentColor = PureWhite,
                    disabledContainerColor = SportOrange.copy(alpha = 0.5f)
                ),
                enabled = !uiState.isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PureWhite,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightBorder)
                Text(
                    text = "  hoặc tiếp tục với  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightBorder)
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PureWhite,
                    contentColor = TextPrimary
                ),
                enabled = !uiState.isLoading
            ) {
                GoogleLogoIcon(size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tiếp tục với Google",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextSecondary)) {
                            append("Chưa có tài khoản? ")
                        }
                        withStyle(SpanStyle(color = SportOrange, fontWeight = FontWeight.Bold)) {
                            append("Đăng ký ngay")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bằng cách tiếp tục, bạn đồng ý với Điều khoản Sử dụng và Chính sách Quyền riêng tư của ứng dụng.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

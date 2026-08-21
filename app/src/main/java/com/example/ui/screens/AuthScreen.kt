package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeaconCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SleekCardBlue
import com.example.ui.theme.SleekGrayCard
import com.example.ui.theme.SleekHeaderNavy
import com.example.ui.theme.SleekMutedText
import com.example.ui.theme.SleekNavy
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AuthInputTextColor = Color(0xFF111111)
private val AuthLabelTextColor = Color(0xFF2B2B2B)

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isSignIn by remember { mutableStateOf(true) }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetModeOtp by remember { mutableStateOf(false) }

    fun showError(message: String) {
        errorMessage = message
        successMessage = null
    }

    fun showSuccess(message: String) {
        successMessage = message
        errorMessage = null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FBFF),
                        Color(0xFFE9F1FB),
                        Color(0xFFDCE8F4)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, BeaconCyan.copy(alpha = 0.38f), Color.Transparent)))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .background(BeaconCyan.copy(alpha = 0.10f), CircleShape)
                    .align(Alignment.TopEnd)
            )
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(SignalRed.copy(alpha = 0.08f), CircleShape)
                    .align(Alignment.TopStart)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SleekGrayCard),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = SleekCardBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SS",
                                color = SleekNavy,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Silent Signal",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekHeaderNavy
                        )
                        Text(
                            text = "Secure account access for emergency protection",
                            color = SleekMutedText,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("Private login") },
                        leadingIcon = {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SleekCardBlue)
                    )
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("Email / Username") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFDCEFFF))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { isSignIn = true },
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSignIn) SleekNavy else Color.Transparent, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = "Sign In",
                            color = if (isSignIn) Color.White else SleekHeaderNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(
                        onClick = { isSignIn = false },
                        modifier = Modifier
                            .weight(1f)
                            .background(if (!isSignIn) SleekNavy else Color.Transparent, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = "Sign Up",
                            color = if (!isSignIn) Color.White else SleekHeaderNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isSignIn) "Welcome back" else "Create your account",
                            fontWeight = FontWeight.Bold,
                            color = SleekHeaderNavy,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSignIn) {
                                "Use your email or username to continue."
                            } else {
                                "Set up a profile for fast, secure access."
                            },
                            color = SleekMutedText,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSignIn) {
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = {
                            Icon(
                                imageVector = if (identifier.contains("@")) Icons.Default.Email else Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        label = { Text("Email / Username", color = AuthLabelTextColor) },
                        placeholder = { Text("name@example.com or your username", color = AuthLabelTextColor.copy(alpha = 0.65f)) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        label = { Text("Password", color = AuthLabelTextColor) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                            Text(text = "Remember me", color = SleekHeaderNavy, fontSize = 12.sp)
                        }
                        TextButton(onClick = { showResetDialog = true }) {
                            Text(text = "Forgot Password?", color = BeaconCyan, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            coroutineScope.launch {
                                val trimmedIdentifier = identifier.trim()
                                val trimmedPassword = password.trim()

                                if (trimmedIdentifier.isBlank()) {
                                    showError("Enter your email or username.")
                                    return@launch
                                }
                                if (trimmedPassword.isBlank()) {
                                    showError("Enter your password.")
                                    return@launch
                                }
                                if (trimmedIdentifier.contains("@") && !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedIdentifier).matches()) {
                                    showError("Enter a valid email format.")
                                    return@launch
                                }

                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                delay(450)
                                val result = viewModel.signIn(trimmedIdentifier, trimmedPassword, rememberMe)
                                isLoading = false
                                result.onSuccess {
                                    showSuccess("Signed in successfully.")
                                }.onFailure {
                                    showError(it.message ?: "Server/network error. Try again.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekNavy),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Signing in...", color = Color.White, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Sign In", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Full name", color = AuthLabelTextColor) },
                        placeholder = { Text("Enter your name", color = AuthLabelTextColor.copy(alpha = 0.65f)) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.lowercase() },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        label = { Text("Username", color = AuthLabelTextColor) },
                        placeholder = { Text("silent.signal", color = AuthLabelTextColor.copy(alpha = 0.65f)) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        label = { Text("Email", color = AuthLabelTextColor) },
                        placeholder = { Text("name@example.com", color = AuthLabelTextColor.copy(alpha = 0.65f)) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        label = { Text("Phone number", color = AuthLabelTextColor) },
                        placeholder = { Text("+1 555 000 1234", color = AuthLabelTextColor.copy(alpha = 0.65f)) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AuthInputTextColor),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPassword) "Hide confirm password" else "Show confirm password"
                                )
                            }
                        },
                        label = { Text("Confirm password", color = AuthLabelTextColor) },
                        singleLine = true,
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                            Text(text = "Remember me", color = SleekHeaderNavy, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            coroutineScope.launch {
                                val trimmedName = fullName.trim()
                                val trimmedUsername = username.trim()
                                val trimmedEmail = email.trim()
                                val trimmedPhone = phoneNumber.trim()
                                val trimmedPassword = password.trim()
                                val trimmedConfirm = confirmPassword.trim()

                                if (trimmedName.isBlank()) {
                                    showError("Enter your full name.")
                                    return@launch
                                }
                                if (trimmedUsername.isBlank()) {
                                    showError("Enter a username.")
                                    return@launch
                                }
                                if (trimmedEmail.isBlank()) {
                                    showError("Enter your email address.")
                                    return@launch
                                }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                    showError("Enter a valid email format.")
                                    return@launch
                                }
                                if (trimmedPassword.isBlank()) {
                                    showError("Enter your password.")
                                    return@launch
                                }
                                if (trimmedConfirm.isBlank()) {
                                    showError("Confirm your password.")
                                    return@launch
                                }
                                if (trimmedPassword != trimmedConfirm) {
                                    showError("Passwords do not match.")
                                    return@launch
                                }

                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                delay(550)
                                val result = viewModel.signUp(
                                    fullName = trimmedName,
                                    username = trimmedUsername,
                                    email = trimmedEmail,
                                    phoneNumber = trimmedPhone,
                                    password = trimmedPassword,
                                    rememberMe = rememberMe
                                )
                                isLoading = false
                                result.onSuccess {
                                    showSuccess("Account created. You are signed in.")
                                }.onFailure {
                                    showError(it.message ?: "Server/network error. Try again.")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekNavy),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Creating account...", color = Color.White, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Create account", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFD7E2ED))
                    Text(
                        text = "  OR  ",
                        color = SleekMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color(0xFFD7E2ED))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (isLoading) return@Button
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            delay(350)
                            val result = viewModel.continueWithGoogle()
                            isLoading = false
                            result.onSuccess {
                                showSuccess("Signed in with Google.")
                            }.onFailure {
                                showError(it.message ?: "Server/network error. Google login is not connected yet.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335))))
                    ) {
                        Text(
                            text = "G",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Continue with Google", color = SleekHeaderNavy, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Your account stays local in this demo. A backend should handle password hashing, HTTPS, rate limiting, verification, and logout in production.",
                    color = SleekMutedText,
                    fontSize = 12.sp
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = SignalRed, fontWeight = FontWeight.SemiBold)
                }

                successMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = SafeGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Need help?", color = SleekMutedText, fontSize = 12.sp)
            TextButton(onClick = { showResetDialog = true }) {
                Text(text = "Forgot Password?", color = BeaconCyan, fontWeight = FontWeight.SemiBold)
            }
        }

        Text(
            text = "Secure access. Clean login. Built for speed.",
            color = SleekHeaderNavy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how you want to recover your account.",
                        color = SleekMutedText,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !resetModeOtp,
                            onClick = { resetModeOtp = false },
                            label = { Text("Email") }
                        )
                        FilterChip(
                            selected = resetModeOtp,
                            onClick = { resetModeOtp = true },
                            label = { Text("OTP") }
                        )
                    }
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email address") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val result = if (resetModeOtp) {
                            Result.failure<Unit>(IllegalStateException("OTP reset requires backend or SMS provider setup."))
                        } else {
                            viewModel.requestPasswordReset(resetEmail)
                        }
                        result.onSuccess {
                            showSuccess("Password reset request sent to your email.")
                            showResetDialog = false
                        }.onFailure {
                            showError(it.message ?: "Server/network error. Try again.")
                        }
                    }
                }) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
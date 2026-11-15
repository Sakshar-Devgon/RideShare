package com.beast.rideconnect.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideconnect.AuthState
import com.beast.rideconnect.AuthViewModel
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.components.OrichuButton
import com.beast.rideshare.ui.components.OrichuTextField
import com.beast.rideshare.ui.components.RideShareLogo
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Forgot password bottom sheet
    var showForgotPasswordSheet by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                isLoading = false
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.SignIn.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = state.message
            }
            is AuthState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            else -> {
                isLoading = false
            }
        }
    }

    fun handleSignIn() {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            errorMessage = "Enter a valid email address."
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }
        errorMessage = null
        authViewModel.login(trimmedEmail, password)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            val minContentHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = minContentHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(36.dp))

            // Official RideShare Brand Logo & Two-Tone Logotype
            RideShareLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = BrandDeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Ride")
                    }
                    withStyle(
                        SpanStyle(
                            color = BrandAccentViolet,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    ) {
                        append("Share")
                    }
                },
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 24.sp,
                    letterSpacing = (-0.4).sp
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Title and Subtitle
            Text(
                text = "Welcome Back",
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Sign in to continue to your account",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Email Field
            OrichuTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errorMessage != null && (email.isEmpty() || !email.contains("@"))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OrichuTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingIconClick = { isPasswordVisible = !isPasswordVisible },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage != null && password.length < 6
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password Link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrichuPrimary
                    ),
                    modifier = Modifier.clickable {
                        forgotEmail = email
                        showForgotPasswordSheet = true
                    }
                )
            }

            // Error Text Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .heightIn(min = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = OrichuBad
                        )
                    )
                }
            }

            // Sign In Button
            OrichuButton(
                text = "Sign In",
                onClick = { handleSignIn() },
                loading = isLoading,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }

        // Bottom Footer (anchored gracefully across all device sizes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don’t have an account? ",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "Sign Up",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrichuPrimary
                ),
                modifier = Modifier.clickable {
                    navController.navigate(Routes.SignUp.route)
                }
            )
        }
    }
}
}

    // Forgot Password Bottom Sheet
    if (showForgotPasswordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showForgotPasswordSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Reset password",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We’ll email you a link to reset your password.",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OrichuTextField(
                    value = forgotEmail,
                    onValueChange = { forgotEmail = it },
                    placeholder = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(18.dp))

                OrichuButton(
                    text = "Send reset link",
                    onClick = {
                        val trimmed = forgotEmail.trim()
                        if (trimmed.isNotEmpty() && trimmed.contains("@")) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(trimmed)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Reset link sent to $trimmed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, task.exception?.message ?: "Failed to send reset link", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            showForgotPasswordSheet = false
                        } else {
                            Toast.makeText(context, "Enter a valid email address.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

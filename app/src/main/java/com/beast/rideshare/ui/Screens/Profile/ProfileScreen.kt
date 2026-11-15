package com.beast.rideshare.ui.Screens.Profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideshare.Model.User
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    themeManager: com.beast.rideshare.ui.theme.ThemeManager
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Bottom Sheets
    var showPersonalSheet by remember { mutableStateOf(false) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var showPrefsSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showSupportSheet by remember { mutableStateOf(false) }

    // Toggle states from HTML
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var autoAcceptEnabled by remember { mutableStateOf(false) }
    var showPhoneToRiders by remember { mutableStateOf(true) }
    var twoStepLogin by remember { mutableStateOf(false) }

    // Wallet balance
    var walletBalance by remember { mutableIntStateOf(500) }

    // Personal info fields
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf("Male") }

    val isDarkTheme = themeManager.isDarkMode

    fun loadProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Not authenticated. Please sign in again.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }

        profileViewModel.getUserProfile(
            onSuccess = { loadedUser ->
                user = loadedUser
                editName = loadedUser.name
                editEmail = loadedUser.email
                editPhone = loadedUser.phoneNumber
                editGender = loadedUser.gender.ifBlank { "Male" }
                loading = false
            },
            onFailure = { error ->
                Toast.makeText(context, "Error loading profile: $error", Toast.LENGTH_LONG).show()
                loading = false
            }
        )
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            OrichuBottomNavBar(
                currentRoute = "profile",
                onNavigate = { route ->
                    when (route) {
                        "home" -> {
                            navController.popBackStack()
                            navController.navigate(Routes.Home.route)
                        }
                        "find" -> navController.navigate(Routes.FindRide.route)
                        "offer1" -> navController.navigate(Routes.OfferRide.route)
                        "activity" -> navController.navigate(Routes.WaitingScreen.route)
                        "profile" -> Unit
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Header
            OrichuHeader(
                title = "Profile & Settings",
                subtitle = "Preferences, security & appearance",
                onBackClick = {
                    navController.popBackStack()
                    navController.navigate(Routes.Home.route)
                }
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrichuPrimary)
                }
            } else {
                val userName = user?.name?.ifBlank { "Sakshar" } ?: "Sakshar"
                val userEmail = user?.email?.ifBlank { "user@example.com" } ?: "user@example.com"
                val userInitial = userName.take(1).uppercase()

                // Profile Card (.card: avatar, name, email, verified badge, chevron)
                OrichuCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(18.dp),
                    onClick = { showPersonalSheet = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrichuAvatar(
                            initial = userInitial,
                            size = 56.dp,
                            fontSize = 20
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OrichuVerifiedBadge(text = "Verified Member")
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Settings Card with Rows (.card with padding: 2px 18px)
                OrichuCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp)
                ) {
                    // Personal Information
                    OrichuSettingsRow(
                        title = "Personal Information",
                        leadingIcon = Icons.Default.Person,
                        onClick = { showPersonalSheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Payment & Wallet
                    OrichuSettingsRow(
                        title = "Payment & Wallet",
                        leadingIcon = Icons.Default.Payment,
                        onClick = { showWalletSheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Ride History
                    OrichuSettingsRow(
                        title = "Ride History",
                        leadingIcon = Icons.Default.History,
                        onClick = { navController.navigate(Routes.RideHistory.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Notifications Toggle (.sw)
                    OrichuSettingsRow(
                        title = "Notifications",
                        leadingIcon = Icons.Default.Notifications,
                        trailingContent = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    notificationsEnabled = it
                                    Toast.makeText(context, "Notifications ${if (it) "on" else "off"}", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = OrichuPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Dark Mode Toggle (.sw)
                    OrichuSettingsRow(
                        title = "Dark Mode",
                        leadingIcon = Icons.Default.DarkMode,
                        trailingContent = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { themeManager.toggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = OrichuPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // App Preferences
                    OrichuSettingsRow(
                        title = "App Preferences",
                        leadingIcon = Icons.Default.Tune,
                        onClick = { showPrefsSheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Privacy & Security
                    OrichuSettingsRow(
                        title = "Privacy & Security",
                        leadingIcon = Icons.Default.Shield,
                        onClick = { showPrivacySheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    // Help & Support
                    OrichuSettingsRow(
                        title = "Help & Support",
                        leadingIcon = Icons.Default.HelpOutline,
                        onClick = { showSupportSheet = true }
                    )
                }

                // Logout Row Card (.row.card with color: var(--bad))
                OrichuCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.SignIn.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = OrichuBad,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Logout",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrichuBad
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = OrichuBad,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Personal Information Bottom Sheet
    if (showPersonalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPersonalSheet = false },
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
                    text = "Personal Information",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                OrichuTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    placeholder = "Full Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(10.dp))

                OrichuTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    placeholder = "Email Address",
                    leadingIcon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(10.dp))

                OrichuTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    placeholder = "Phone number",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Gender",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Male", "Female").forEach { g ->
                        val isSelected = editGender.equals(g, ignoreCase = true)
                        OrichuChip(
                            text = g,
                            selected = isSelected,
                            onClick = { editGender = g },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OrichuButton(
                    text = "Save changes",
                    onClick = {
                        val trimmedName = editName.trim()
                        val trimmedEmail = editEmail.trim()
                        if (trimmedName.isNotBlank() && trimmedEmail.contains("@")) {
                            val updatedUser = user?.copy(
                                name = trimmedName,
                                email = trimmedEmail,
                                phoneNumber = editPhone.trim(),
                                gender = editGender.lowercase(),
                                femaleMode = (editGender.equals("Female", ignoreCase = true) && user?.femaleMode == true)
                            ) ?: User(
                                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                name = trimmedName,
                                email = trimmedEmail,
                                phoneNumber = editPhone.trim(),
                                gender = editGender.lowercase(),
                                femaleMode = false
                            )

                            profileViewModel.updateUserProfile(
                                user = updatedUser,
                                onSuccess = {
                                    user = updatedUser
                                    showPersonalSheet = false
                                    Toast.makeText(context, "Changes saved", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { err ->
                                    Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Enter your name and a valid email address.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Payment & Wallet Bottom Sheet
    if (showWalletSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWalletSheet = false },
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
                    text = "Wallet",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Purple gradient balance card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Available balance",
                            style = TextStyle(fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹$walletBalance",
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Add money",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(100, 200, 500).forEach { amount ->
                        OrichuChip(
                            text = "+₹$amount",
                            selected = false,
                            onClick = {
                                walletBalance += amount
                                Toast.makeText(context, "₹$amount added to wallet", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // App Preferences Bottom Sheet
    if (showPrefsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrefsSheet = false },
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
                    text = "App Preferences",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sound Effects", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            Toast.makeText(context, "Sound ${if (it) "on" else "off"}", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = OrichuPrimary)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-accept bookings", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                    Switch(
                        checked = autoAcceptEnabled,
                        onCheckedChange = {
                            autoAcceptEnabled = it
                            Toast.makeText(context, "Auto-accept ${if (it) "on" else "off"}", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = OrichuPrimary)
                    )
                }
            }
        }
    }

    // Privacy & Security Bottom Sheet
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            var newPassword by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Privacy & Security",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show phone to riders", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                    Switch(
                        checked = showPhoneToRiders,
                        onCheckedChange = { showPhoneToRiders = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = OrichuPrimary)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Two-step login", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                    Switch(
                        checked = twoStepLogin,
                        onCheckedChange = { twoStepLogin = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = OrichuPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Change password",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OrichuTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "New password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OrichuButton(
                    text = "Update password",
                    onClick = {
                        if (newPassword.length >= 6) {
                            FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                                        showPrivacySheet = false
                                    } else {
                                        Toast.makeText(context, task.exception?.message ?: "Failed to update", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Help & Support Bottom Sheet
    if (showSupportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSupportSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            var msg by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Help & Support",
                    style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Text(
                    text = "• How do I reserve a seat?\nOpen Find a Ride, pick a journey and tap Reserve Seat.",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• How do I offer a ride?\nTap the + button, set your route, time, seats and price, then publish.",
                    style = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OrichuTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    placeholder = "Describe your issue...",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                OrichuButton(
                    text = "Send message",
                    onClick = {
                        if (msg.isNotBlank()) {
                            Toast.makeText(context, "Message sent. We will reply by email", Toast.LENGTH_SHORT).show()
                            showSupportSheet = false
                        } else {
                            Toast.makeText(context, "Write your message first", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

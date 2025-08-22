package com.beast.rideshare.ui.Screens.Profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideshare.Model.User
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
    var saving by remember { mutableStateOf(false) }
    
    var selectedGender by remember { mutableStateOf("") }
    var femaleMode by remember { mutableStateOf(false) }

    // Load user profile on screen load
    LaunchedEffect(Unit) {
        // Check authentication state first
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Not authenticated. Please sign in again.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return@LaunchedEffect
        }

        // Log authentication info for debugging
        println("DEBUG: Current user: ${currentUser.uid}")
        println("DEBUG: Email: ${currentUser.email}")
        println("DEBUG: Email verified: ${currentUser.isEmailVerified}")

        profileViewModel.getUserProfile(
            onSuccess = { loadedUser ->
                user = loadedUser
                selectedGender = loadedUser.gender
                femaleMode = loadedUser.femaleMode
                loading = false
            },
            onFailure = { error ->
                Toast.makeText(context, "Error loading profile: $error", Toast.LENGTH_LONG).show()
                loading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF8C52FF))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // User Info Section
                user?.let { currentUser ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "User Information",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text("Name: ${currentUser.name}")
                            Text("Email: ${currentUser.email}")
                            if (currentUser.phoneNumber.isNotBlank()) {
                                Text("Phone: ${currentUser.phoneNumber}")
                            }
                        }
                    }
                }

                // Gender Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8C52FF)
                            )
                        )
                        
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (selectedGender == "male"),
                                        onClick = { selectedGender = "male" },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedGender == "male"),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "Male",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (selectedGender == "female"),
                                        onClick = { selectedGender = "female" },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedGender == "female"),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "Female",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Female Mode Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Female Mode",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8C52FF)
                            )
                        )
                        
                        Text(
                            text = "When enabled, your rides will be marked as female-only and only visible to other female users.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            fontSize = 14.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enable Female Mode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = femaleMode,
                                onCheckedChange = { femaleMode = it },
                                enabled = selectedGender == "female", // Only enable for female users
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF8C52FF),
                                    checkedTrackColor = Color(0xFF8C52FF).copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        if (selectedGender != "female" && femaleMode) {
                            // Reset female mode if gender is not female
                            LaunchedEffect(selectedGender) {
                                femaleMode = false
                            }
                        }
                        
                        if (selectedGender != "female") {
                            Text(
                                text = "Female Mode is only available for female users.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Dark Mode Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "Switch between light and dark theme for better viewing experience.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = themeManager.isDarkMode,
                                onCheckedChange = { themeManager.toggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        if (selectedGender.isNotBlank()) {
                            saving = true
                            profileViewModel.updateGenderAndFemaleMode(
                                gender = selectedGender,
                                femaleMode = femaleMode,
                                onSuccess = {
                                    saving = false
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { error ->
                                    saving = false
                                    // Show detailed error message
                                    val errorMessage = when {
                                        error.contains("permission-denied") || error.contains("PERMISSION_DENIED") ->
                                            "Permission denied. Please check Firestore security rules."
                                        error.contains("not logged in") ->
                                            "Please sign out and sign in again."
                                        error.contains("network") ->
                                            "Network error. Please check your internet connection."
                                        else -> "Error: $error"
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                                    // Also log the full error for debugging
                                    println("Profile update error: $error")
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please select your gender.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8C52FF),
                        contentColor = Color.White
                    ),
                    enabled = !saving
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            "Save Changes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

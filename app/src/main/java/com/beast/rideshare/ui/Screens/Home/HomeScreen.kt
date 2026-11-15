package com.beast.rideshare.ui.Screens.Home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideconnect.AuthState
import com.beast.rideconnect.AuthViewModel
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    var displayName by remember { mutableStateOf("Sakshar") }
    var userInitial by remember { mutableStateOf("S") }
    var isFemaleMode by remember { mutableStateOf(false) }

    // Bottom Sheet States
    var showMenuSheet by remember { mutableStateOf(false) }
    var showNotifsSheet by remember { mutableStateOf(false) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var showSupportSheet by remember { mutableStateOf(false) }

    // Wallet state
    var walletBalance by remember { mutableIntStateOf(500) }

    // Notifications state
    var hasUnreadNotifs by remember { mutableStateOf(true) }
    val notifications = remember {
        mutableStateListOf(
            "Welcome to RideShare! Your verified commute network.",
            "Special: Female-only safety carpools available in your area.",
            "Tip: Host a recurring route to save commute expenses."
        )
    }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnSuccessListener {
            val rawName = user.displayName ?: user.email?.split("@")?.firstOrNull() ?: "Sakshar"
            displayName = rawName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            userInitial = displayName.take(1).uppercase()
        }

        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        isFemaleMode = doc.getBoolean("femaleMode") ?: false
                        val firestoreName = doc.getString("name")
                        if (!firestoreName.isNullOrBlank()) {
                            displayName = firestoreName.split(" ").firstOrNull() ?: firestoreName
                            userInitial = displayName.take(1).uppercase()
                        }
                    }
                }
        }
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate(Routes.SignIn.route) {
                popUpTo(Routes.Home.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            OrichuBottomNavBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "home" -> Unit
                        "find" -> navController.navigate(Routes.FindRide.route)
                        "offer1" -> navController.navigate(Routes.OfferRide.route)
                        "activity" -> navController.navigate(Routes.WaitingScreen.route)
                        "profile" -> navController.navigate(Routes.Profile.route)
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(horizontal = 20.dp)
        ) {
            val minContentHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = minContentHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Content Block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

            // .h Header: Menu icon button, Spacer, Bell icon button, Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OrichuIconButton(
                    icon = Icons.Default.Menu,
                    onClick = { showMenuSheet = true },
                    contentDescription = "Menu"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrichuIconButton(
                        icon = Icons.Default.Notifications,
                        onClick = {
                            hasUnreadNotifs = false
                            showNotifsSheet = true
                        },
                        hasBadgeDot = hasUnreadNotifs,
                        contentDescription = "Notifications"
                    )

                    OrichuAvatar(
                        initial = userInitial,
                        size = 44.dp,
                        onClick = { navController.navigate(Routes.Profile.route) }
                    )
                }
            }

            // Hello Header (.h1 + .mu)
            Column {
                Text(
                    text = "Hello, $displayName",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Where would you like to travel today?",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Verified Community Pill (.ver)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrichuVerifiedBadge(text = "Verified Community")

                if (isFemaleMode) {
                    FemaleModeBadge()
                }
            }

            // Two Main Service Tiles (.tiles: grid 1fr 1fr)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tile 1: Find a Ride
                OrichuCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(16.dp),
                    onClick = { navController.navigate(Routes.FindRide.route) }
                ) {
                    // Soft Purple Icon Container (.ic)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OrichuLightSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = OrichuPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Find a Ride",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Join verified drivers going your way",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }

                // Tile 2: Offer a Ride (.tile.a)
                OrichuCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(16.dp),
                    onClick = { navController.navigate(Routes.OfferRide.route) }
                ) {
                    // Soft Amber Icon Container (.tile.a .ic: #fff1dd, #d97706)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OrichuOfferRideIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = OrichuOfferRideIconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Offer a Ride",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Share your route and save cost",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            // Quick Actions 4-Column Grid (.qk)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: My Rides
                QuickActionItem(
                    label = "My Rides",
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.WaitingScreen.route) }
                )

                // Action 2: Wallet
                QuickActionItem(
                    label = "Wallet",
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f),
                    onClick = { showWalletSheet = true }
                )

                // Action 3: Support
                QuickActionItem(
                    label = "Support",
                    icon = Icons.Default.HelpOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { showSupportSheet = true }
                )

                // Action 4: More
                QuickActionItem(
                    label = "More",
                    icon = Icons.Default.Tune,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.Profile.route) }
                )
            }
        }

        // Bottom Content Block: Promo Banner (.ban)
        // Positioned at the bottom, exactly 20dp away from the bottom navigation bar!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6))
                        )
                    )
                    .clickable { navController.navigate(Routes.FindRide.route) }
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Travel Together,\nSpend Less",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            lineHeight = 23.sp
                        )
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
}

    // Menu Bottom Sheet (.sheet)
    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Menu",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OrichuSettingsRow(
                    title = "Home",
                    leadingIcon = Icons.Default.Home,
                    onClick = {
                        showMenuSheet = false
                    }
                )

                OrichuSettingsRow(
                    title = "Find a Ride",
                    leadingIcon = Icons.Default.DirectionsCar,
                    onClick = {
                        showMenuSheet = false
                        navController.navigate(Routes.FindRide.route)
                    }
                )

                OrichuSettingsRow(
                    title = "Offer a Ride",
                    leadingIcon = Icons.Default.Add,
                    onClick = {
                        showMenuSheet = false
                        navController.navigate(Routes.OfferRide.route)
                    }
                )

                OrichuSettingsRow(
                    title = "Active Ride Offers",
                    leadingIcon = Icons.Default.History,
                    onClick = {
                        showMenuSheet = false
                        navController.navigate(Routes.WaitingScreen.route)
                    }
                )

                OrichuSettingsRow(
                    title = "Profile & Settings",
                    leadingIcon = Icons.Default.Person,
                    onClick = {
                        showMenuSheet = false
                        navController.navigate(Routes.Profile.route)
                    }
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OrichuSettingsRow(
                    title = "Logout",
                    leadingIcon = Icons.Default.Logout,
                    onClick = {
                        showMenuSheet = false
                        authViewModel.signOut()
                        navController.navigate(Routes.SignIn.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    // Notifications Bottom Sheet
    if (showNotifsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotifsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Notifications",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                notifications.forEach { notifText ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = OrichuPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = notifText,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    // Wallet Bottom Sheet
    if (showWalletSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWalletSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
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

                // Purple Balance Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Available balance",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹$walletBalance",
                            style = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Add money",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // +₹100, +₹200, +₹500 chips
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

    // Support Bottom Sheet
    if (showSupportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSupportSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            var supportMessage by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Help & Support",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // FAQs
                FaqItem(
                    question = "How do I reserve a seat?",
                    answer = "Open Find a Ride, pick a journey and tap Reserve Seat. The fare is deducted from your wallet."
                )
                FaqItem(
                    question = "How do I offer a ride?",
                    answer = "Tap the + button, set your route, time, seats and price, then publish."
                )
                FaqItem(
                    question = "How are drivers verified?",
                    answer = "Every member in the community is verified with official credentials before they can host or join rides."
                )

                Spacer(modifier = Modifier.height(14.dp))

                OrichuTextField(
                    value = supportMessage,
                    onValueChange = { supportMessage = it },
                    placeholder = "Describe your issue...",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                OrichuButton(
                    text = "Send message",
                    onClick = {
                        if (supportMessage.isNotBlank()) {
                            Toast.makeText(context, "Message sent. We will reply to your registered email.", Toast.LENGTH_SHORT).show()
                            showSupportSheet = false
                            supportMessage = ""
                        } else {
                            Toast.makeText(context, "Write your message first", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = OrichuPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = question,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = answer,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

package com.beast.rideshare.ui.Screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideconnect.AuthViewModel
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.R
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * RideShare Onboarding Splash Screen
 * Features:
 * - High-impact purple sunset mountain highway hero image (splash_hero) covering the top screen
 * - Seamless bottom gradient dissolve into clean card surface
 * - Modern typography ("Share Rides, Save More, Travel Together.")
 * - 3-dot pager indicator
 * - Interactive "Slide to get started ›››" slider button
 */
@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    fun navigateNext() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.SignIn.route) {
                popUpTo(Routes.Splash.route) { inclusive = true }
            }
        }
    }

    // Subtle smooth entrance fade
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, animationSpec = tween(600, easing = LinearOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // -----------------------------------------------------------------
        // 1. Hero Section: Sunset Highway Car filling the upper ~65%
        // -----------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.66f)
                .align(Alignment.TopCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_hero),
                contentDescription = "RideShare Sunset Highway",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )

            // Seamless gradient dissolve into bottom white surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.25f to Color.Transparent,
                            0.50f to Color.White.copy(alpha = 0.25f),
                            0.70f to Color.White.copy(alpha = 0.60f),
                            0.88f to Color.White.copy(alpha = 0.92f),
                            1.0f to Color.White
                        )
                    )
            )
        }

        // -----------------------------------------------------------------
        // 2. Bottom Content Section: Clean Onboarding Card + Slider
        // -----------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .alpha(contentAlpha.value)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.15f to Color.White.copy(alpha = 0.85f),
                        0.30f to Color.White,
                        1.0f to Color.White
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 26.dp)
                .padding(bottom = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Main Headline
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = BrandDeepNavy,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Share Rides,\nSave More,\n")
                    }
                    withStyle(
                        SpanStyle(
                            brush = Brush.linearGradient(
                                listOf(BrandPrimaryPurple, BrandAccentViolet)
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append("Travel Together.")
                    }
                },
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 32.sp,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 39.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "A verified and affordable carpooling network for your daily commute.",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6C6685),
                    lineHeight = 21.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3-dot Pager Indicator (Active purple pill + 2 soft lilac dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 7.dp)
                        .clip(CircleShape)
                        .background(BrandPrimaryPurple)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(BrandSoftLavender)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(BrandSoftLavender)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive "Slide to Get Started" Slider
            SlideToStartButton(
                onSlideComplete = { navigateNext() }
            )
        }
    }
}

/**
 * Modern Interactive "Slide to Get Started" Pill Button
 * Users slide the circular arrow thumb from left to right to unlock the next screen
 */
@Composable
fun SlideToStartButton(
    modifier: Modifier = Modifier,
    onSlideComplete: () -> Unit
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(30.dp),
                spotColor = Color(0x225B21B6)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(BrandWhite)
    ) {
        val density = LocalDensity.current
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val thumbSizePx = with(density) { 52.dp.toPx() }
        val thumbPaddingPx = with(density) { 4.dp.toPx() }
        val maxDragPx = (trackWidthPx - thumbSizePx - (thumbPaddingPx * 2)).coerceAtLeast(0f)

        val animatedThumbOffset by animateFloatAsState(
            targetValue = dragOffset,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "thumb_offset"
        )

        val currentOffsetPx = if (isDragging) dragOffset else animatedThumbOffset
        val progress = if (maxDragPx > 0f) (currentOffsetPx / maxDragPx).coerceIn(0f, 1f) else 0f

        // 1. Dynamic Gradient Fill behind the thumb
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { (thumbSizePx + (thumbPaddingPx * 2) + currentOffsetPx).toDp() })
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            BrandPrimaryPurple.copy(alpha = 0.20f + 0.40f * progress),
                            BrandAccentViolet.copy(alpha = 0.30f + 0.50f * progress)
                        )
                    )
                )
        )

        // 2. Centered track hint text that fades as user drags across
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Slide to get started  ›››",
                style = TextStyle(
                    fontFamily = PoppinsFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandPrimaryPurple.copy(alpha = (1f - progress * 1.6f).coerceIn(0f, 0.90f)),
                    letterSpacing = 0.4.sp
                )
            )
        }

        // 3. Draggable Circular Gradient Thumb
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .offset(x = with(density) { currentOffsetPx.toDp() })
                .size(52.dp)
                .align(Alignment.CenterStart)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = Color(0x665B21B6),
                    ambientColor = Color(0x335B21B6)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(BrandPrimaryPurple, BrandAccentViolet)
                    )
                )
                .pointerInput(maxDragPx, isCompleted) {
                    if (isCompleted) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            if (dragOffset >= maxDragPx * 0.70f) {
                                isCompleted = true
                                dragOffset = maxDragPx
                                coroutineScope.launch {
                                    onSlideComplete()
                                }
                            } else {
                                dragOffset = 0f
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!isCompleted) {
                                dragOffset = (dragOffset + dragAmount).coerceIn(0f, maxDragPx)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Slide Arrow",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

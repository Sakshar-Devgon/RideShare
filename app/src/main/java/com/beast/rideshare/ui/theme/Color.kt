package com.beast.rideshare.ui.theme

import androidx.compose.ui.graphics.Color

// Default M3 baseline values
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// =========================================================================
// RideShare Exact Color System Tokens (per RideShare_Colors_and_Fonts.docx)
// =========================================================================

// =========================================================================
// RideShare Final Approved Brand Identity Color System
// =========================================================================
val BrandPrimaryPurple = Color(0xFF5B21B6) // Primary Purple: #5B21B6
val BrandAccentViolet  = Color(0xFFA855F7) // Accent Violet: #A855F7
val BrandSoftLavender  = Color(0xFFE9D8FD) // Soft Lavender: #E9D8FD
val BrandBackground    = Color(0xFFF8F5FF) // Background: #F8F5FF
val BrandDeepNavy      = Color(0xFF1B1233) // Deep Navy: #1B1233
val BrandWhite         = Color(0xFFFFFFFF) // White: #FFFFFF

// Core Colors (Light Theme)
val OrichuPrimary = Color(0xFF5B21B6)        // Primary purple: #5B21B6
val OrichuPrimary2 = Color(0xFFA855F7)       // Accent purple: #A855F7
val OrichuLightSoft = Color(0xFFE9D8FD)      // Soft lavender: #E9D8FD
val OrichuLightBg = Color(0xFFF8F5FF)        // Page background: #F8F5FF
val OrichuLightCard = Color(0xFFFFFFFF)      // Card surface: #FFFFFF
val OrichuLightInk = Color(0xFF1B1233)       // Deep Navy text: #1B1233
val OrichuLightMuted = Color(0xFF6F6A88)     // Muted text: #6F6A88
val OrichuLightLine = Color(0xFFECE8F6)      // Borders and dividers: #ECE8F6
val OrichuOk = Color(0xFF16A34A)             // Success and pickup dot: #16A34A
val OrichuBad = Color(0xFFE11D48)            // Error, drop-off dot and Logout: #E11D48

// Dark Theme Tokens
val OrichuDarkBg = Color(0xFF120D22)         // Page background: #120D22
val OrichuDarkCard = Color(0xFF1C1534)       // Card surface: #1C1534
val OrichuDarkInk = Color(0xFFF4F0FF)        // Main text: #F4F0FF
val OrichuDarkMuted = Color(0xFFA49FC0)      // Muted text: #A49FC0
val OrichuDarkLine = Color(0xFF2D2450)       // Borders and dividers: #2D2450
val OrichuDarkSoft = Color(0xFF2A1F52)       // Soft purple tint: #2A1F52

// Gradient Start / End stops
val OrichuPrimaryDark = Color(0xFF5B21B6)    // 135° Button & Logo gradient start: #5B21B6
val OrichuPrimaryDeep = Color(0xFF4C1D95)    // 120° Banner & Wallet start / Splash end: #4C1D95
val OrichuPrimaryLight = Color(0xFFA78BFA)   // 135° Avatar gradient end: #A78BFA
val OrichuSplashStart = Color(0xFF7C3AED)    // 170° Splash screen gradient start: #7C3AED

// Badges and Accents (per Docx Table)
val OrichuVerifiedBadgeBg = Color(0xFFDCFCE7)    // Verified badge background: #DCFCE7
val OrichuVerifiedBadgeText = Color(0xFF15803D)  // Verified badge text: #15803D
val OrichuWaitingPillBg = Color(0xFFFFF1DD)      // "Waiting for Riders" pill bg: #FFF1DD
val OrichuWaitingPillText = Color(0xFFB45309)    // "Waiting for Riders" pill text: #B45309
val OrichuActivePillBg = Color(0xFFDCFCE7)       // "Active" pill background: #DCFCE7
val OrichuActivePillText = Color(0xFF15803D)     // "Active" pill text: #15803D
val OrichuOfferRideIconBg = Color(0xFFFFF1DD)    // Offer a Ride tile icon bg: #FFF1DD
val OrichuOfferRideIconTint = Color(0xFFD97706)  // Offer a Ride tile icon text: #D97706
val OrichuSplashButtonBg = Color(0xFFFFFFFF)     // Splash button bg: #FFFFFF
val OrichuSplashButtonText = Color(0xFF4C1D95)   // Splash button text: #4C1D95
val OrichuToastBg = Color(0xFF1C1633)            // Toast message bg: #1C1633
val OrichuToastText = Color(0xFFFFFFFF)          // Toast message text: #FFFFFF
val OrichuMapRouteLine = Color(0xFF7C3AED)       // Map route line: #7C3AED
val OrichuMapCarMarker = Color(0xFF6D28D9)       // Map car marker: #6D28D9
val OrichuBackdrop = Color(0xFFE4DCF7)           // Outer backdrop behind phone: #E4DCF7

// Backward-compatibility aliases for existing codebase
val OrichuWarnPillBg = OrichuWaitingPillBg
val OrichuWarnPillText = OrichuWaitingPillText
val OrichuOkPillBg = OrichuActivePillBg
val OrichuOkPillText = OrichuActivePillText

val BrandPurple = OrichuPrimary
val BrandPurpleLight = OrichuPrimary2
val BrandPurpleDark = OrichuPrimaryDark
val NeonIndigo = OrichuPrimary2
val CyanAccent = Color(0xFF06B6D4)

val RoseNeon = OrichuBad
val RoseNeonLight = Color(0xFFFB7185)
val RoseGlow = Color(0x26E11D48)

val EmeraldNeon = OrichuOk
val EmeraldGlow = Color(0x2616A34A)
val AmberGold = Color(0xFFF59E0B)

val DarkBackground = OrichuDarkBg
val DarkSurface = OrichuDarkCard
val DarkCardBackground = OrichuDarkCard
val DarkSurfaceVariant = OrichuDarkSoft
val DarkBorder = OrichuDarkLine
val DarkBorderStrong = OrichuDarkLine
val DarkOnBackground = OrichuDarkInk
val DarkOnSurface = OrichuDarkInk
val DarkOnSurfaceVariant = OrichuDarkMuted

val LightBackground = OrichuLightBg
val LightSurface = OrichuLightCard
val LightCardBackground = OrichuLightCard
val LightSurfaceVariant = OrichuLightSoft
val LightBorder = OrichuLightLine
val LightOnBackground = OrichuLightInk
val LightOnSurface = OrichuLightInk
val LightOnSurfaceVariant = OrichuLightMuted

val Gray50 = Color(0xFFF8FAFC)
val Gray100 = Color(0xFFF1F5F9)
val Gray200 = Color(0xFFE2E8F0)
val Gray300 = Color(0xFFCBD5E1)
val Gray400 = Color(0xFF94A3B8)
val Gray500 = Color(0xFF64748B)
val Gray600 = Color(0xFF475569)
val Gray700 = Color(0xFF334155)
val Gray800 = Color(0xFF1E293B)
val Gray900 = Color(0xFF0F172A)

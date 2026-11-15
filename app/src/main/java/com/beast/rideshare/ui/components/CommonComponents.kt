package com.beast.rideshare.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beast.rideshare.ui.theme.*

// ==========================================
// Orichu Design System Components
// (Extracted from RideShare-Orichu.html)
// ==========================================

/**
 * .card: background: var(--card); border: 1px solid var(--line); border-radius: 22px; padding: 18px; box-shadow: var(--sh)
 */
@Composable
fun OrichuCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    elevation: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier
            .shadow(elevation, shape, spotColor = Color(0x1A28145A))
            .border(BorderStroke(1.dp, borderColor), shape)
            .then(clickableModifier),
        shape = shape,
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * .ib: width: 42px; height: 42px; border-radius: 14px; background: var(--card); border: 1px solid var(--line); display: grid; place-items: center; color: var(--ink)
 * Optional notification dot .ib.d
 */
@Composable
fun OrichuIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasBadgeDot: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    badgeColor: Color = OrichuBad,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        if (hasBadgeDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 9.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

/**
 * .av: width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg,#5b21b6,#a78bfa); color: #fff; display: grid; place-items: center; font-weight: 600
 */
@Composable
fun OrichuAvatar(
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    fontSize: Int = 16,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(OrichuPrimaryDark, OrichuPrimaryLight)
                )
            )
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.take(1).uppercase(),
            color = Color.White,
            style = TextStyle(
                fontSize = fontSize.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * .h: Header with 12dp gap, back button or menu button, title, and optional right actions
 */
@Composable
fun OrichuHeader(
    title: String? = null,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    menuIcon: ImageVector? = null,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBackClick != null) {
            OrichuIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBackClick,
                contentDescription = "Back"
            )
        } else if (menuIcon != null && onMenuClick != null) {
            OrichuIconButton(
                icon = menuIcon,
                onClick = onMenuClick,
                contentDescription = "Menu"
            )
        }

        if (title != null || subtitle != null) {
            Column(modifier = Modifier.weight(1f)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

/**
 * Button variants:
 * - Primary (.btn): height 54dp, gradient #5b21b6 to #8b5cf6, color #fff, font 15sp semi-bold
 * - Outline (.btn.o): height 54dp, background surface, border 1.5dp OrichuPrimary2, text OrichuPrimary
 * - Soft (.btn.l): height 54dp, background surfaceVariant, text OrichuPrimary
 */
enum class OrichuButtonStyle {
    PRIMARY,
    OUTLINE,
    SOFT
}

@Composable
fun OrichuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: OrichuButtonStyle = OrichuButtonStyle.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    height: Dp = 54.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.98f else 1f,
        animationSpec = tween(120),
        label = "btnScale"
    )

    val shape = RoundedCornerShape(16.dp)

    val (bgModifier, textColor, borderStroke) = when (style) {
        OrichuButtonStyle.PRIMARY -> {
            Triple(
                Modifier.background(
                    if (enabled) Brush.linearGradient(listOf(OrichuPrimaryDark, OrichuPrimary2))
                    else Brush.linearGradient(listOf(Gray500, Gray600)),
                    shape = shape
                ),
                Color.White,
                null
            )
        }
        OrichuButtonStyle.OUTLINE -> {
            Triple(
                Modifier.background(MaterialTheme.colorScheme.surface, shape = shape),
                OrichuPrimary,
                BorderStroke(1.5.dp, OrichuPrimary2)
            )
        }
        OrichuButtonStyle.SOFT -> {
            Triple(
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = shape),
                OrichuPrimary,
                null
            )
        }
    }

    val shadowElevation = if (style == OrichuButtonStyle.PRIMARY && enabled) 8.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(shadowElevation, shape, spotColor = OrichuPrimary)
                } else Modifier
            )
            .then(
                if (borderStroke != null) {
                    Modifier.border(borderStroke, shape)
                } else Modifier
            )
            .clip(shape)
            .then(bgModifier)
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = textColor,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                )

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/**
 * .f: height: 54px; padding: 0 16px; border-radius: 16px; background: var(--card); border: 1.5px solid var(--line); color: var(--mu)
 */
@Composable
fun OrichuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (singleLine) Modifier.height(54.dp) else Modifier.heightIn(min = 54.dp))
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                1.5.dp,
                if (isError) OrichuBad else MaterialTheme.colorScheme.outline,
                shape
            )
            .padding(horizontal = 16.dp, vertical = if (singleLine) 0.dp else 14.dp),
        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    ) {
        Row(
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = singleLine,
                    minLines = minLines,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    cursorBrush = SolidColor(OrichuPrimary)
                )
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(10.dp))
                val iconModifier = if (onTrailingIconClick != null) {
                    Modifier.clickable(onClick = onTrailingIconClick)
                } else Modifier

                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .then(iconModifier)
                )
            }
        }
    }
}

/**
 * .rt: Route timeline with dashed connector line, green origin dot, and red destination dot.
 */
@Composable
fun OrichuRouteTimeline(
    origin: String,
    destination: String,
    modifier: Modifier = Modifier,
    originLabel: String? = null,
    destinationLabel: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Timeline Graphic
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(20.dp)
                .padding(top = 4.dp)
        ) {
            // Origin Dot: Green 12dp
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(OrichuOk)
                    .border(3.dp, OrichuOk.copy(alpha = 0.2f), CircleShape)
            )

            // Dashed Line: height ~42dp
            val lineStrokeColor = MaterialTheme.colorScheme.outline
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .height(44.dp)
            ) {
                drawLine(
                    color = lineStrokeColor,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Destination Dot: Red 12dp
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(OrichuBad)
                    .border(3.dp, OrichuBad.copy(alpha = 0.2f), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Address Texts
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (originLabel != null) {
                    Text(
                        text = originLabel,
                        style = TextStyle(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrichuOk
                        )
                    )
                }
                Text(
                    text = origin.ifBlank { "Pickup location not set" },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column {
                if (destinationLabel != null) {
                    Text(
                        text = destinationLabel,
                        style = TextStyle(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrichuBad
                        )
                    )
                }
                Text(
                    text = destination.ifBlank { "Drop-off destination not set" },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * .chip: padding: 8px 16px; border-radius: 99px; font-size: 13px; font-weight: 500;
 */
@Composable
fun OrichuChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(99.dp)
    val bgColor = if (selected) OrichuPrimary else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) OrichuPrimary else MaterialTheme.colorScheme.outline
    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = bgColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
    }
}

/**
 * .pill and .ver badges:
 * - Verified Community / Member: #dcfce7, text #15803d
 * - Waiting for Riders: #fff1dd, text #b45309
 */
@Composable
fun OrichuPill(
    text: String,
    modifier: Modifier = Modifier,
    isGreen: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val bgColor = if (isGreen) OrichuOkPillBg else OrichuWarnPillBg
    val textColor = if (isGreen) OrichuOkPillText else OrichuWarnPillText

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(99.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}

/**
 * .ver: Verified Community Badge
 */
@Composable
fun OrichuVerifiedBadge(
    text: String = "Verified Community",
    modifier: Modifier = Modifier
) {
    OrichuPill(
        text = text,
        modifier = modifier,
        isGreen = true,
        leadingIcon = Icons.Default.Check
    )
}

/**
 * Stepper (- count +) as in .stp
 */
@Composable
fun OrichuStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 6,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minus Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(enabled = value > min) { onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = if (value > min) OrichuPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Count Text
        Text(
            text = value.toString(),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.widthIn(min = 20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Plus Button: solid purple
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (value < max) OrichuPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                .clickable(enabled = value < max) { onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * .nav: Floating dock navigation bar with center + FAB.
 * Exactly matches the CSS in RideShare-Orichu.html:
 * .nav { position: sticky; bottom: -8px; display: flex; justify-content: space-around; background: var(--card); border: 1px solid var(--line); border-radius: 24px; padding: 8px; box-shadow: 0 10px 30px rgba(40,20,90,.16) }
 * .nav .pl { width: 52px; height: 52px; border-radius: 50%; background: linear-gradient(135deg,#5b21b6,#8b5cf6); color: #fff; place-content: center; margin-top: -30px; border: 4px solid var(--bg) }
 */
@Composable
fun OrichuBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x2828145A))
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Home
                OrichuNavTabItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    selected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )

                // Find
                OrichuNavTabItem(
                    label = "Find",
                    icon = Icons.Default.Search,
                    selected = currentRoute == "find",
                    onClick = { onNavigate("find") }
                )

                // Spacer for Center Floating Button
                Spacer(modifier = Modifier.width(48.dp))

                // Activity
                OrichuNavTabItem(
                    label = "Activity",
                    icon = Icons.Default.AccessTime,
                    selected = currentRoute == "activity" || currentRoute == "waiting",
                    onClick = { onNavigate("activity") }
                )

                // Profile
                OrichuNavTabItem(
                    label = "Profile",
                    icon = Icons.Default.Person,
                    selected = currentRoute == "profile",
                    onClick = { onNavigate("profile") }
                )
            }
        }

        // Center Elevated Circular Action Button (.nav .pl)
        Box(
            modifier = Modifier
                .offset(y = (-24).dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(OrichuPrimaryDark, OrichuPrimary2)
                    )
                )
                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                .shadow(10.dp, CircleShape, spotColor = Color(0x666D28D9))
                .clickable { onNavigate("offer1") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Offer a Ride",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun OrichuNavTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) OrichuPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) OrichuPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Settings Row Item as seen in Profile
 */
@Composable
fun OrichuSettingsRow(
    title: String,
    leadingIcon: ImageVector,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = OrichuPrimary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Backwards compatibility components for existing screens during migration
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    OrichuCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        elevation = elevation,
        onClick = onClick,
        content = content
    )
}

@Composable
fun ShowcaseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(OrichuPrimaryDark, OrichuPrimary2),
    shape: Shape = RoundedCornerShape(16.dp)
) {
    OrichuButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        trailingIcon = icon
    )
}

@Composable
fun RouteTimeline(
    from: String,
    to: String,
    modifier: Modifier = Modifier,
    fromLabel: String = "Pickup Point",
    toLabel: String = "Drop-off Point"
) {
    OrichuRouteTimeline(
        origin = from,
        destination = to,
        modifier = modifier,
        originLabel = fromLabel,
        destinationLabel = toLabel
    )
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = OrichuOkPillBg,
    textColor: Color = OrichuOkPillText,
    borderColor: Color = Color.Transparent,
    showBeacon: Boolean = false,
    beaconColor: Color = OrichuOk
) {
    OrichuPill(
        text = text,
        modifier = modifier,
        isGreen = (textColor == OrichuOkPillText || textColor == OrichuOk)
    )
}

@Composable
fun FemaleModeBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(
            BorderStroke(1.dp, OrichuBad.copy(alpha = 0.4f)),
            RoundedCornerShape(50)
        ),
        shape = RoundedCornerShape(50),
        color = OrichuBad.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = OrichuBad,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "FEMALE MODE",
                style = TextStyle(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrichuBad
                )
            )
        }
    }
}

@Composable
fun ModernTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    OrichuHeader(
        title = title,
        subtitle = subtitle,
        onBackClick = onBackClick,
        trailingContent = actions
    )
}

@Composable
fun LuxuryBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OrichuBottomNavBar(
        currentRoute = currentRoute,
        onNavigate = onNavigate,
        modifier = modifier
    )
}

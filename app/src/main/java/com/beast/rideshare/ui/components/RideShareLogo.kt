package com.beast.rideshare.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.beast.rideshare.R

/**
 * Official RideShare Brand Logo
 * Displays the exact approved reference artwork:
 * Front-facing glossy purple car + curved upper wave + flowing lower road wave
 * on a soft white-to-lavender squircle tile with ambient shadow.
 */
@Composable
fun RideShareLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val cornerRadius = size * 0.22f

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = Color(0x335B21B6),
                ambientColor = Color(0x1A5B21B6)
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.rideshare_logo_tile),
            contentDescription = "RideShare Official Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

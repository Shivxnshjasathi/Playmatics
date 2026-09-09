package com.zincstate.playmatics.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zincstate.playmatics.ui.theme.LogoGreen

@Composable
fun PlaymaticsLogo(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp
) {
    Text(
        text = "playmatics.",
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = LogoGreen,
        letterSpacing = (-1.5).sp,
        modifier = modifier
    )
}

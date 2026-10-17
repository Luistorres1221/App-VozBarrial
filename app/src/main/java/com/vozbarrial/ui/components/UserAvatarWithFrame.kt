package com.vozbarrial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vozbarrial.ui.theme.VozGreen
import com.vozbarrial.ui.theme.VozNavy

@Composable
fun UserAvatarWithFrame(
    profilePhoto: String?,
    name: String,
    frameName: String,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val frameColor = when {
        frameName.contains("Dorado", true) -> Color(0xFFE8A800)
        frameName.contains("Verde", true) || frameName.contains("Clásico", true) -> Color(0xFF07966C)
        frameName.contains("Neón", true) -> Color(0xFF00A6C7)
        frameName.contains("Paz", true) -> Color(0xFF2453F5)
        frameName.contains("Ciudadano", true) -> Color(0xFFE84A12)
        frameName.isNotBlank() -> VozNavy
        else -> Color.Transparent
    }

    val boxModifier = if (frameName.isNotBlank()) {
        modifier
            .size(size)
            .border(2.dp, frameColor, CircleShape)
            .padding(3.dp)
            .border(1.5.dp, frameColor, CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
    } else {
        modifier
            .size(size)
            .clip(CircleShape)
    }

    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        if (!profilePhoto.isNullOrBlank()) {
            AsyncImage(
                model = profilePhoto,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFDCEBE7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase().ifBlank { "U" },
                    color = VozGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }
    }
}

package com.vozbarrial.features.auth.welcome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vozbarrial.features.auth.welcome.presentation.WelcomeUiState
import com.vozbarrial.features.auth.welcome.presentation.WelcomeViewModel
import com.vozbarrial.ui.theme.VozGreen
import com.vozbarrial.ui.theme.VozNavy

private val Gold = Color(0xFFF4B942)

@Composable
fun WelcomeRoute(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    welcomeViewModel: WelcomeViewModel = viewModel()
) {
    val uiState by welcomeViewModel.uiState.collectAsStateWithLifecycle()

    WelcomeContent(
        uiState = uiState,
        onLoginClick = { welcomeViewModel.onLoginClicked(onLoginClick) },
        modifier = modifier
    )
}

@Composable
fun WelcomeContent(
    uiState: WelcomeUiState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFE8F8F5), Color(0xFFF8FAFC)),
                    center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    radius = 1200f,
                ),
            )
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logotipo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(14.dp, RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(VozNavy, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 4.dp)
                            .size(19.dp)
                            .background(Color(0xFF2866D9), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 17.dp, bottom = 12.dp)
                            .size(6.dp)
                            .background(VozGreen, CircleShape)
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 17.dp, bottom = 12.dp)
                            .size(6.dp)
                            .background(Gold, CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(uiState.title, color = VozNavy, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(uiState.subtitle, color = Color(0xFF52606D), fontSize = 16.sp)
            Spacer(Modifier.height(14.dp))

            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.76f)) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(Modifier.size(7.dp).background(VozGreen, CircleShape))
                    Text(
                        uiState.badgeText,
                        color = Color(0xFF52606D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
            ) {
                Text("Iniciar Sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(9.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = Color(0xFF8A949E),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    uiState.footerText,
                    color = Color(0xFF8A949E),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 290, heightDp = 656)
@Composable
private fun WelcomePreview() {
    WelcomeContent(
        uiState = WelcomeUiState(),
        onLoginClick = {}
    )
}

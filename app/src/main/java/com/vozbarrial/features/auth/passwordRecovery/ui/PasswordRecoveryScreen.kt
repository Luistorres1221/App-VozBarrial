package com.vozbarrial.features.auth.passwordRecovery.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vozbarrial.R
import com.vozbarrial.core.RememberedEmailCache
import com.vozbarrial.core.RememberedEmailSuggestions
import com.vozbarrial.core.result.AuthOperationResult
import com.vozbarrial.features.auth.passwordRecovery.presentation.RecoveryUiState
import com.vozbarrial.features.auth.passwordRecovery.presentation.RecoveryViewModel
import com.vozbarrial.ui.theme.VozBackground
import com.vozbarrial.ui.theme.VozBlueSurface
import com.vozbarrial.ui.theme.VozGreen
import com.vozbarrial.ui.theme.VozMuted
import com.vozbarrial.ui.theme.VozNavy

/**
 * Punto de entrada Stateful para la Recuperación de Contraseña.
 */
@Composable
fun PasswordRecoveryRoute(
    onBack: () -> Unit,
    onRequestPasswordReset: (String, (AuthOperationResult<Unit>) -> Unit) -> Unit,
    recoveryViewModel: RecoveryViewModel = viewModel()
) {
    val context = LocalContext.current
    DisposableEffect(recoveryViewModel) {
        onDispose { recoveryViewModel.reset() }
    }

    val uiState by recoveryViewModel.uiState.collectAsStateWithLifecycle()

    PasswordRecoveryContent(
        uiState = uiState,
        onBack = onBack,
        onEmailChange = { email ->
            recoveryViewModel.updateEmail(email)
            RememberedEmailCache.remember(context, email)
        },
        onRequestReset = { recoveryViewModel.requestReset(onRequestPasswordReset) }
    )
}

/**
 * UI Stateless pura para la Recuperación de Contraseña.
 */
@Composable
fun PasswordRecoveryContent(
    uiState: RecoveryUiState,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onRequestReset: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(VozBackground)) {
        // TopBar Header
        Row(
            Modifier
                .fillMaxWidth()
                .height(49.dp)
                .background(Color.White)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = VozNavy,
                    modifier = Modifier.size(18.dp)
                )
            }
            Image(
                painter = painterResource(R.mipmap.logo_foreground),
                contentDescription = "VozBarrial",
                modifier = Modifier.size(27.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text("VozBarrial", color = VozMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("VOZBARRIAL", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Recuperar contraseña",
                    color = VozNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(5.dp))
        }

        // Cuerpo con Scroll
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.succeeded) {
                // Vista de Éxito
                Text(
                    "✉  SOLICITUD PROCESADA",
                    Modifier
                        .background(VozBlueSurface, RoundedCornerShape(30.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                    color = VozNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(18.dp))
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = VozGreen,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Revisa tu correo",
                    color = VozNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Si el correo está asociado a una cuenta, recibirás un enlace para restablecer la contraseña.",
                    color = VozMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                Surface(
                    Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Para continuar", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        RecoveryStep("1", "Revisa tu bandeja de entrada y, si no aparece, las carpetas de spam o promociones.")
                        RecoveryStep("2", "Abre el enlace del correo y cambia tu contraseña en la página que se abre.")
                        RecoveryStep("3", "Cuando termines, vuelve a VozBarrial e inicia sesión con tu nueva contraseña.")
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
                ) {
                    Text("Volver al inicio de sesión", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                // Formulario de Solicitud
                Text(
                    "✿  RECUPERACIÓN SEGURA DE CUENTA",
                    Modifier
                        .background(VozBlueSurface, RoundedCornerShape(30.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                    color = VozNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(11.dp))
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD8E5FF))
                            .padding(6.dp)
                            .background(Color.White, CircleShape)
                            .padding(4.dp)
                            .background(VozNavy, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                    Box(
                        Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(VozGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    "¿Olvidaste tu contraseña?",
                    color = VozNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Ingresa el correo asociado a tu cuenta de VozBarrial. Si está registrado, recibirás un enlace para restablecer la contraseña.",
                    color = VozMuted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("ⓘ", color = Color(0xFF507BB6), fontSize = 12.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Por seguridad, la respuesta no confirma si el correo pertenece a una cuenta.",
                        color = VozMuted,
                        fontSize = 12.sp,
                        lineHeight = 13.sp,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Correo electrónico", Modifier.fillMaxWidth(), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    placeholder = { Text("Escribe tu correo electrónico", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = VozNavy) },
                    singleLine = true,
                    enabled = !uiState.sending,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(9.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE6EBF4),
                    ),
                )
                RememberedEmailSuggestions(uiState.email, onEmailChange)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF52627A), modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Usa el mismo correo que registraste en VozBarrial.", color = VozMuted, fontSize = 12.sp, lineHeight = 13.sp)
                }
                if (uiState.message.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        uiState.message,
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.height(9.dp))
                Button(
                    onClick = onRequestReset,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.sending,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
                ) {
                    if (uiState.sending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Enviar enlace de recuperación  ➤", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(9.dp))
                Surface(
                    Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(VozBlueSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?", color = Color(0xFF315C94), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(5.dp))
                            Text("¿Qué pasará a continuación?", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        RecoveryStep("1", "Si el correo está asociado a una cuenta, recibirás un enlace de recuperación.")
                        RecoveryStep("2", "Abre el enlace del correo para continuar con el cambio de contraseña.")
                        RecoveryStep("3", "El enlace es de un solo uso y puede caducar. Si deja de funcionar, solicita uno nuevo.")
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(VozBlueSurface, RoundedCornerShape(9.dp))
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("✉", color = VozNavy, fontSize = 12.sp)
                            Spacer(Modifier.width(5.dp))
                            Text("Si no aparece, revisa las carpetas de spam o promociones.", color = VozNavy, fontSize = 12.sp, lineHeight = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryStep(number: String, content: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(VozBlueSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color(0xFF315C94), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(5.dp))
        Text(content, color = VozNavy, fontSize = 12.sp, lineHeight = 13.sp)
    }
}

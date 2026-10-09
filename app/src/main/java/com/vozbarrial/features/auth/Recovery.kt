package com.vozbarrial.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vozbarrial.R
import com.vozbarrial.domain.auth.AuthError
import com.vozbarrial.domain.auth.AuthOperationResult
import com.vozbarrial.ui.theme.*

@Composable
fun PasswordRecovery(
    onBack: () -> Unit,
    onRequestPasswordReset: (String, (AuthOperationResult<Unit>) -> Unit) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var requestSucceeded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(VozBackground)) {
        Row(
            Modifier.fillMaxWidth().height(49.dp).background(Color.White).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = VozNavy, modifier = Modifier.size(18.dp))
            }
            Image(
                painter = painterResource(R.mipmap.logo_vozbarrial_foreground),
                contentDescription = "VozBarrial",
                modifier = Modifier.size(27.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text("VozBarrial", color = VozMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("VOZBARRIAL", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Recuperar contraseña", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(Modifier.width(5.dp))
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (requestSucceeded) {
                Text(
                    "✉  SOLICITUD PROCESADA",
                    Modifier.background(VozBlueSurface, RoundedCornerShape(30.dp)).padding(horizontal = 11.dp, vertical = 5.dp),
                    color = VozNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(18.dp))
                Icon(Icons.Default.Email, contentDescription = null, tint = VozGreen, modifier = Modifier.size(54.dp))
                Spacer(Modifier.height(12.dp))
                Text("Revisa tu correo", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Si el correo está asociado a una cuenta, recibirás un enlace para restablecer la contraseña.",
                    color = VozMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(10.dp), shadowElevation = 1.dp) {
                    Column(Modifier.padding(horizontal = 13.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Text(
                "✿  RECUPERACIÓN SEGURA DE CUENTA",
                Modifier.background(VozBlueSurface, RoundedCornerShape(30.dp)).padding(horizontal = 11.dp, vertical = 5.dp),
                color = VozNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(11.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    Modifier.size(68.dp).clip(CircleShape).background(Color(0xFFD8E5FF)).padding(6.dp)
                        .background(Color.White, CircleShape).padding(4.dp).background(VozNavy, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
                }
                Box(Modifier.size(18.dp).clip(CircleShape).background(VozGreen), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                }
            }
            Spacer(Modifier.height(7.dp))
            Text("¿Olvidaste tu contraseña?", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
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
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
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
                value = email,
                onValueChange = {
                    email = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text("Escribe tu correo electrónico", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = VozNavy) },
                singleLine = true,
                enabled = !isSending,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(9.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE6EBF4),
                ),
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF52627A), modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text("Usa el mismo correo que registraste en VozBarrial.", color = VozMuted, fontSize = 12.sp, lineHeight = 13.sp)
            }
            if (message.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                Text(
                    message,
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(9.dp))
            Button(
                onClick = {
                    message = ""
                    val normalizedEmail = email.trim()
                    if (!normalizedEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                        message = "Escribe un correo válido."
                    } else {
                        isSending = true
                        onRequestPasswordReset(normalizedEmail) { result ->
                            isSending = false
                            when (result) {
                                is AuthOperationResult.Success -> {
                                    requestSucceeded = true
                                }
                                is AuthOperationResult.Failure -> when (result.error) {
                                    AuthError.AccountNotFound -> {
                                        requestSucceeded = true
                                    }
                                    AuthError.NetworkUnavailable -> message = "No hay conexión. Verifica tu conexión e inténtalo de nuevo."
                                    else -> message = "No se pudo completar la solicitud. Inténtalo de nuevo."
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isSending,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Enviar enlace de recuperación  ➤", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(9.dp))
            Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(10.dp), shadowElevation = 1.dp) {
                Column(Modifier.padding(horizontal = 13.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(20.dp).clip(CircleShape).background(VozBlueSurface), contentAlignment = Alignment.Center) {
                            Text("?", color = Color(0xFF315C94), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(5.dp))
                        Text("¿Qué pasará a continuación?", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    RecoveryStep("1", "Si el correo está asociado a una cuenta, recibirás un enlace de recuperación.")
                    RecoveryStep("2", "Abre el enlace del correo para continuar con el cambio de contraseña.")
                    RecoveryStep("3", "El enlace es de un solo uso y puede caducar. Si deja de funcionar, solicita uno nuevo.")
                    Row(
                        Modifier.fillMaxWidth().background(VozBlueSurface, RoundedCornerShape(9.dp)).padding(horizontal = 10.dp, vertical = 9.dp),
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
        Box(Modifier.size(20.dp).clip(CircleShape).background(VozBlueSurface), contentAlignment = Alignment.Center) {
            Text(number, color = Color(0xFF315C94), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(5.dp))
        Text(content, color = VozNavy, fontSize = 12.sp, lineHeight = 13.sp)
    }
}

package com.vozbarrial.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RecoveryNavy = Color(0xFF06213A)
private val RecoveryBlue = Color(0xFFEEF3FF)
private val RecoveryGreen = Color(0xFF12C991)
private val RecoveryMuted = Color(0xFF68758A)
private val RecoveryPage = Color(0xFFF7F9FE)

@Composable
fun PasswordRecovery(onBack: () -> Unit, onAccountExists: (String) -> Boolean, onResetPassword: (String, String) -> Boolean, onResolveName: (String) -> String) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var email by rememberSaveable { mutableStateOf("") }
    var accountName by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(RecoveryPage)) {
        Row(Modifier.fillMaxWidth().height(49.dp).background(Color.White).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (step == 0) onBack() else { step--; message = "" } }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = RecoveryNavy, modifier = Modifier.size(18.dp))
            }
            Box(Modifier.size(27.dp).clip(CircleShape).background(RecoveryNavy), contentAlignment = Alignment.Center) { Text("V", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            Spacer(Modifier.width(5.dp))
            Column(Modifier.weight(1f)) {
                Text(if (step == 0) "VozBarrial Logo" else "VozBarrial", color = RecoveryMuted, fontSize = 12.sp, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("VOZBARRIAL", color = RecoveryNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(if (step == 0) "Recuperar Contraseña" else if (step == 1) "Nueva Clave" else "Éxito del Registro", color = RecoveryNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(Modifier.width(5.dp))
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 9.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (step == 0) {
                Text("✿  AUTENTICACIÓN CIUDADANA SEGURA", Modifier.background(RecoveryBlue, RoundedCornerShape(30.dp)).padding(horizontal = 11.dp, vertical = 5.dp), color = RecoveryNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(11.dp))
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(Modifier.size(68.dp).clip(CircleShape).background(Color(0xFFD8E5FF)).padding(6.dp).background(Color.White, CircleShape).padding(4.dp).background(RecoveryNavy, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                    Box(Modifier.size(18.dp).clip(CircleShape).background(RecoveryGreen), contentAlignment = Alignment.Center) { Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(11.dp)) }
                }
                Spacer(Modifier.height(7.dp))
                Text("¿Olvidaste tu contraseña?", color = RecoveryNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(3.dp))
                Text("Ingresa el correo electrónico asociado a tu cuenta ciudadana o de moderación. Te enviaremos un enlace mágico y seguro para restablecer tu clave sin complicaciones.", color = RecoveryMuted, fontSize = 12.sp, lineHeight = 15.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ⓘ", color = Color(0xFF507BB6), fontSize = 12.sp)
                    Spacer(Modifier.width(5.dp))
                    Text("Recuperación para agentes de patrullaje, fiscalización y administradores comunales acreditados.", color = RecoveryMuted, fontSize = 12.sp, lineHeight = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text("Correo Electrónico Registrado", Modifier.fillMaxWidth(), color = RecoveryNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                OutlinedTextField(value = email, onValueChange = { email = it.trim(); message = "" }, modifier = Modifier.fillMaxWidth().height(56.dp), placeholder = { Text("oficial.mendoza@seguridad.vozbarrial.org", fontSize = 12.sp) }, leadingIcon = { Text("@", color = RecoveryNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp) }, trailingIcon = { Text("ⓧ", color = RecoveryMuted, fontSize = 12.sp) }, singleLine = true, shape = RoundedCornerShape(9.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = Color(0xFFE6EBF4)))
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Security, null, tint = Color(0xFF52627A), modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Asegúrate de que sea el mismo correo con el que recibes alertas barriales.", color = RecoveryMuted, fontSize = 12.sp, lineHeight = 13.sp)
                }
                if (message.isNotBlank()) { Spacer(Modifier.height(5.dp)); Text(message, Modifier.fillMaxWidth(), color = Color(0xFFB42318), fontSize = 12.sp) }
                Spacer(Modifier.height(9.dp))
                Button(onClick = {
                    message = ""
                    if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) message = "Escribe un correo válido."
                    else if (onAccountExists(email)) { accountName = onResolveName(email); step = 1 } else message = "No encontramos una cuenta con ese correo."
                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(5.dp), colors = ButtonDefaults.buttonColors(containerColor = RecoveryNavy)) {
                    Text("Enviar Enlace de Recuperación  ➤", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.height(9.dp))
                Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(10.dp), shadowElevation = 1.dp) {
                    Column(Modifier.padding(horizontal = 13.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(20.dp).clip(CircleShape).background(RecoveryBlue), contentAlignment = Alignment.Center) { Text("?", color = Color(0xFF315C94), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            Spacer(Modifier.width(5.dp)); Text("¿Qué pasará a continuación?", color = RecoveryNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        RecoveryStep("1", "Recibirás un correo seguro en menos de 2 minutos con el remitente soporte@vozbarrial.org.")
                        RecoveryStep("2", "Presiona el botón único «Restablecer mi contraseña» adjunto en el mensaje.")
                        RecoveryStep("3", "Por protección institucional, el enlace expirará en 15 minutos tras ser generado.")
                        Row(Modifier.fillMaxWidth().background(RecoveryBlue, RoundedCornerShape(9.dp)).padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("✉", color = RecoveryNavy, fontSize = 12.sp); Spacer(Modifier.width(5.dp)); Text("¿No ves tu correo? Revisa tu bandeja de Spam o Promociones.", color = RecoveryNavy, fontSize = 12.sp, lineHeight = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.height(30.dp), shape = RoundedCornerShape(30.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp), colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = Color(0xFF52627A))) {
                    Text("◷  Reenviar enlace ahora", fontSize = 12.sp)
                }
            } else {
                if (step == 1) {
                    val strong = newPassword.length >= 8 && (newPassword.any { it.isDigit() } || newPassword.any { !it.isLetterOrDigit() })
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth().background(Color(0xFFDDF8EF), RoundedCornerShape(12.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, null, tint = Color(0xFF087A55), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp))
                            Text("Enlace validado con éxito", Modifier.weight(1f), color = Color(0xFF087A55), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("◷  00:00 min", color = RecoveryMuted, fontSize = 10.sp)
                        }
                        Row(Modifier.fillMaxWidth().background(RecoveryBlue, RoundedCornerShape(12.dp)).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(34.dp).clip(CircleShape).background(RecoveryNavy), contentAlignment = Alignment.Center) { Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            Spacer(Modifier.width(10.dp)); Column { Text("RESTABLECIENDO PARA", color = RecoveryNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(email, color = RecoveryNavy, fontSize = 12.sp, maxLines = 1) }
                        }
                        Spacer(Modifier.height(2.dp))
                        Box(Modifier.size(58.dp).background(Color.White, RoundedCornerShape(15.dp)).padding(6.dp).background(RecoveryNavy, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(30.dp)) }
                        Text("Restablecer Contraseña", color = RecoveryNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                        Text("Asegura el acceso a tus reportes barriales, alertas ciudadanas y moderación comunitaria con una clave robusta.", color = RecoveryMuted, fontSize = 12.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
                        Text("Nueva Contraseña", Modifier.fillMaxWidth(), color = RecoveryNavy, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; message = "" }, modifier = Modifier.fillMaxWidth().height(54.dp), placeholder = { Text("••••••••••••••••", fontSize = 12.sp) }, leadingIcon = { Icon(Icons.Default.Lock, null) }, trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if(showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Mostrar contraseña") } }, singleLine = true, visualTransformation = if(showPassword) VisualTransformation.None else PasswordVisualTransformation(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                        Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Fortaleza:", color = RecoveryNavy, fontSize = 10.sp); Text(if(strong) "Excelente" else "Baja", color = if(strong) Color(0xFF07845D) else RecoveryMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            LinearProgressIndicator(progress = { if(strong) 1f else if(newPassword.isEmpty()) 0f else 0.35f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = if(strong) Color(0xFF07845D) else Color(0xFFE5A63A), trackColor = Color(0xFFE7ECF3))
                            Text("ⓘ  Mínimo 8 caracteres, al menos un número o símbolo", color = RecoveryMuted, fontSize = 10.sp)
                        }
                        Text("Confirmar Nueva Contraseña", Modifier.fillMaxWidth(), color = RecoveryNavy, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        OutlinedTextField(value = confirmation, onValueChange = { confirmation = it; message = "" }, modifier = Modifier.fillMaxWidth().height(54.dp), placeholder = { Text("••••••••••••••••", fontSize = 12.sp) }, leadingIcon = { Icon(Icons.Default.Lock, null) }, trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if(showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Mostrar contraseña") } }, singleLine = true, visualTransformation = if(showPassword) VisualTransformation.None else PasswordVisualTransformation(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                        if(newPassword.isNotEmpty() && confirmation.isNotEmpty()) Text(if(newPassword == confirmation) "✓  Las contraseñas coinciden" else "Las contraseñas no coinciden", Modifier.fillMaxWidth(), color = if(newPassword == confirmation) Color(0xFF07845D) else Color(0xFFB42318), fontSize = 11.sp)
                        if(message.isNotBlank()) Text(message, Modifier.fillMaxWidth(), color = Color(0xFFB42318), fontSize = 11.sp)
                        Button(onClick = {
                            message = ""
                            if (newPassword.length < 8) message = "La contraseña debe tener al menos 8 caracteres."
                            else if (newPassword.none { it.isDigit() } && newPassword.none { !it.isLetterOrDigit() }) message = "Agrega al menos un número o símbolo."
                            else if (newPassword != confirmation) message = "Las contraseñas no coinciden."
                            else if (onResetPassword(email, newPassword)) step = 2 else message = "No se pudo actualizar la contraseña. Inténtalo de nuevo."
                        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = RecoveryNavy)) { Text("Guardar Nueva Contraseña  ♙", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    }
                } else {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0xFFD8E5FF)).padding(7.dp).background(RecoveryNavy, CircleShape).padding(8.dp).background(Color(0xFF0B304D), CircleShape).padding(8.dp).background(Color(0xFF087A55), CircleShape), contentAlignment = Alignment.Center) { Text("✓", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                            Box(Modifier.size(27.dp).clip(CircleShape).background(Color(0xFF91F0C5)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Security, null, tint = Color(0xFF087A55), modifier = Modifier.size(16.dp)) }
                        }
                        Text("♧  SEGURIDAD ACTUALIZADA", Modifier.background(Color(0xFFDDF8EF), RoundedCornerShape(30.dp)).padding(horizontal = 14.dp, vertical = 6.dp), color = Color(0xFF087A55), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("¡Contraseña Actualizada!", color = RecoveryNavy, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
                        Text("Tu clave de VozBarrial ha sido actualizada exitosamente. Tus reportes y privilegios de vecindad están resguardados.", color = RecoveryMuted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
                        Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(RecoveryBlue), contentAlignment = Alignment.Center) { Icon(Icons.Default.Lock, null, tint = RecoveryNavy, modifier = Modifier.size(20.dp)) }
                                Spacer(Modifier.width(9.dp))
                                Column { Text(accountName.ifBlank { email.substringBefore("@") }, color = RecoveryNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text(email, color = RecoveryNavy, fontSize = 11.sp) }
                            }
                            Column(Modifier.fillMaxWidth().background(RecoveryBlue, RoundedCornerShape(9.dp)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF087A55), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(7.dp)); Text("Cierre preventivo global", color = RecoveryNavy, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                                Text("Sesiones finalizadas en otros equipos por protocolo de integridad.", Modifier.padding(start = 23.dp), color = RecoveryMuted, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(Modifier.weight(1f).background(RecoveryBlue, RoundedCornerShape(8.dp)).padding(9.dp)) {
                                    Text("◷  Fecha y hora", color = RecoveryNavy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Hoy, " + java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()) + " hrs", color = RecoveryNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.weight(1f).background(RecoveryBlue, RoundedCornerShape(8.dp)).padding(9.dp)) {
                                    Text("▯  Dispositivo", color = RecoveryNavy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Android · Bogotá", color = RecoveryNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = RecoveryNavy)) { Text("⇥  Iniciar Sesión en VozBarrial", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }                }
            }
        }
    }
}

@Composable
private fun RecoveryStep(number: String, content: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(RecoveryBlue), contentAlignment = Alignment.Center) {
            Text(number, color = Color(0xFF315C94), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(5.dp))
        Text(content, color = RecoveryNavy, fontSize = 12.sp, lineHeight = 13.sp)
    }
}

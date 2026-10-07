package com.vozbarrial.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF06213A)
private val Blue = Color(0xFFEEF3FF)
private val Green = Color(0xFF12C991)
private val Muted = Color(0xFF68758A)
private val Page = Color(0xFFF6F8FC)

@Composable
fun Auth(
    onBack: () -> Unit,
    onLogin: (email: String, password: String) -> Boolean,
    onRegister: (name: String, email: String, password: String) -> Boolean,
    onAuthenticated: (name: String) -> Unit,
    onAccountExists: (String) -> Boolean,
    onResetPassword: (String, String) -> Boolean,
    onResolveName: (String) -> String,
) {
    var registering by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }
    var success by rememberSaveable { mutableStateOf(false) }
    var dialogMessage by rememberSaveable { mutableStateOf("") }
    var showRecovery by rememberSaveable { mutableStateOf(false) }

    if (showRecovery) {
        PasswordRecovery(onBack = { showRecovery = false }, onAccountExists = onAccountExists, onResetPassword = onResetPassword, onResolveName = onResolveName)
        return
    }

    if (registering) {
        Register(
            onBack = { registering = false; message = "" },
            onLoginClick = { registering = false; message = "" },
            onCreateAccount = { newName, newEmail, newPassword ->
                if (onRegister(newName, newEmail, newPassword)) {
                    name = newName
                    email = newEmail
                    password = ""
                    confirmPassword = ""
                    message = "¡Cuenta creada! Ahora puedes iniciar sesión."
                    success = true
                    registering = false
                    true
                } else {
                    false
                }
            },
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Page)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Header(onBack)
        StatusRow()
        IntroCard(registering)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                if (registering) {
                    FieldLabel("Nombre completo")
                    AuthField(
                        value = name,
                        onValueChange = { name = it; message = "" },
                        placeholder = "Tu nombre",
                        leading = { Icon(Icons.Default.PersonAdd, null, tint = Muted, modifier = Modifier.size(17.dp)) },
                        keyboardType = KeyboardType.Text,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FieldLabel("Correo Electrónico", Modifier.weight(1f))
                    Text(if (registering) "Crea tu cuenta" else "Perfil Ciudadano", color = Muted, fontSize = 10.sp)
                }
                AuthField(
                    value = email,
                    onValueChange = { email = it; message = "" },
                    placeholder = "nombre@correo.com",
                    leading = { Icon(Icons.Default.MailOutline, null, tint = Muted, modifier = Modifier.size(17.dp)) },
                    keyboardType = KeyboardType.Email,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FieldLabel("Contraseña", Modifier.weight(1f))
                    if (!registering) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            color = Color(0xFF45628A),
                            fontSize = 10.sp,
                            modifier = Modifier.clickable { showRecovery = true },
                        )
                    }
                }
                AuthField(
                    value = password,
                    onValueChange = { password = it; message = "" },
                    placeholder = "Mínimo 6 caracteres",
                    leading = { Icon(Icons.Default.Lock, null, tint = Muted, modifier = Modifier.size(17.dp)) },
                    keyboardType = KeyboardType.Password,
                    password = true,
                    visible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible },
                )

                if (registering) {
                    FieldLabel("Confirmar contraseña")
                    AuthField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; message = "" },
                        placeholder = "Repite tu contraseña",
                        leading = { Icon(Icons.Default.Lock, null, tint = Muted, modifier = Modifier.size(17.dp)) },
                        keyboardType = KeyboardType.Password,
                        password = true,
                        visible = passwordVisible,
                        onToggleVisibility = { passwordVisible = !passwordVisible },
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Green, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Te enviaremos un enlace de recuperación seguro si no la recuerdas.", color = Muted, fontSize = 10.sp, lineHeight = 13.sp)
                    }
                }

                if (message.isNotBlank()) {
                    Text(
                        message,
                        color = if (success) Color(0xFF087A55) else Color(0xFFB42318),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }

                Button(
                    onClick = {
                        val cleanEmail = email.trim()
                        message = ""
                        success = false
                        if (!cleanEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                            message = "Escribe un correo electrónico válido."
                        } else if (password.length < 6) {
                            message = "La contraseña debe tener al menos 6 caracteres."
                        } else if (registering && name.isBlank()) {
                            message = "Escribe tu nombre para continuar."
                        } else if (registering && password != confirmPassword) {
                            message = "Las contraseñas no coinciden."
                        } else if (registering) {
                            if (onRegister(name.trim(), cleanEmail, password)) {
                                registering = false
                                confirmPassword = ""
                                password = ""
                                message = "¡Cuenta creada! Ahora puedes iniciar sesión."
                                success = true
                            } else {
                                message = "Ya existe una cuenta con ese correo."
                            }
                        } else if (onLogin(cleanEmail, password)) {
                            onAuthenticated(cleanEmail)
                        } else {
                            message = "El correo o la contraseña no son correctos. Regístrate si aún no tienes cuenta."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                ) {
                    Icon(if (registering) Icons.Default.PersonAdd else Icons.Default.Login, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (registering) "Crear mi cuenta" else "Ingresar a mi Barrio", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (!registering) {
                    DividerLabel("O CONTINÚA CON")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { dialogMessage = "El acceso con Google estará disponible cuando conectemos el proveedor de autenticación." },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(9.dp),
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 19.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("Google", color = Navy, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { dialogMessage = "El acceso biométrico estará disponible cuando configuremos la autenticación segura del dispositivo." },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(9.dp),
                        ) {
                            Icon(Icons.Default.Fingerprint, null, tint = Color(0xFF188A79), modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Huella / Face", color = Navy, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(17.dp),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF9AF1CA)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PersonAdd, null, tint = Color(0xFF087A55), modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (registering) "¿Ya eres miembro?" else "¿Aún no eres miembro?",
                        color = Navy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (registering) "Entra a tu comunidad vecinal." else "Súmate a la vigilancia y cuidado barrial.",
                        color = Muted,
                        fontSize = 10.sp,
                    )
                }
                Button(
                    onClick = {
                        registering = !registering
                        message = ""
                        success = false
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087A55)),
                ) {
                    Text(if (registering) "Iniciar sesión" else "Registrarme", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Security, null, tint = Color(0xFF8A949E), modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(5.dp))
            Text("Participación ciudadana segura · VozBarrial", color = Color(0xFF8A949E), fontSize = 10.sp)
        }
    }

    if (dialogMessage.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { dialogMessage = "" },
            title = { Text("VozBarrial", color = Navy) },
            text = { Text(dialogMessage) },
            confirmButton = { TextButton(onClick = { dialogMessage = "" }) { Text("Entendido") } },
        )
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Navy, modifier = Modifier.size(19.dp))
        }
        Box(
            modifier = Modifier.size(29.dp).clip(CircleShape).background(Navy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("VozBarrial", color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Acceso ciudadano", color = Muted, fontSize = 10.sp)
        }
        Icon(Icons.Default.Security, contentDescription = "Acceso seguro", tint = Color(0xFF56AA9A), modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun StatusRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
        StatusPill("Red Segura Activa", Modifier.weight(1f), true)
        StatusPill("Comunidad en línea", Modifier.weight(1f), false)
    }
}

@Composable
private fun StatusPill(text: String, modifier: Modifier, secure: Boolean) {
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = if (secure) Color(0xFFD6FAEA) else Color(0xFFF0F3F9)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (secure) Icon(Icons.Default.Security, null, tint = Color(0xFF07885F), modifier = Modifier.size(11.dp))
            else Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF6EB99B)))
            Spacer(Modifier.width(4.dp))
            Text(text, color = if (secure) Color(0xFF087A55) else Navy, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun IntroCard(registering: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFFE5FFF4), Color.White, Color(0xFFEFF4FF))))
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFFF0F4FF)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Navy), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF3884F5), modifier = Modifier.size(13.dp).align(Alignment.Center))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("VOZBARRIAL DIGITAL", color = Color(0xFF07885F), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(
                if (registering) "Únete a VozBarrial" else "Bienvenido a VozBarrial",
                color = Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                if (registering) "Crea tu cuenta y participa en tu comunidad." else "Tu red de colaboración y seguridad vecinal en tiempo real.",
                color = Muted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leading: @Composable () -> Unit,
    keyboardType: KeyboardType,
    password: Boolean = false,
    visible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(53.dp),
        singleLine = true,
        placeholder = { Text(placeholder, fontSize = 11.sp, color = Muted) },
        leadingIcon = leading,
        trailingIcon = if (password) {
            {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = Muted,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        } else null,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(9.dp),
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Navy),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Blue,
            unfocusedContainerColor = Blue,
            focusedBorderColor = Color(0xFFB8C8E8),
            unfocusedBorderColor = Color.Transparent,
            focusedLeadingIconColor = Muted,
            unfocusedLeadingIconColor = Muted,
        ),
    )
}

@Composable
private fun DividerLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE9EDF4)))
        Text(text, color = Muted, fontSize = 9.sp)
        Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE9EDF4)))
    }
}
package com.vozbarrial.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vozbarrial.ui.theme.*

@Composable
fun Register(
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccount: (name: String, email: String, phone: String, password: String, (Boolean, String?) -> Unit) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var acceptedTerms by rememberSaveable { mutableStateOf(true) }
    var receiveAlerts by rememberSaveable { mutableStateOf(true) }
    var error by rememberSaveable { mutableStateOf("") }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RegisterHeader(onBack)
        Text("Únete a tu Comunidad", color = VozNavy, fontSize = 21.sp, fontWeight = FontWeight.Bold)

        RegisterLabel("Nombre Completo")
        RegisterField(
            value = name,
            onValueChange = { name = it; error = "" },
            placeholder = "ej. Carlos Mendoza",
            icon = { Icon(Icons.Default.Person, null, tint = VozMuted, modifier = Modifier.size(17.dp)) },
            keyboardType = KeyboardType.Text,
        )

        RegisterLabel("Correo Electrónico")
        RegisterField(
            value = email,
            onValueChange = { email = it; error = "" },
            placeholder = "ej. carlos.mendoza@correo.com",
            icon = { Icon(Icons.Default.MailOutline, null, tint = VozMuted, modifier = Modifier.size(17.dp)) },
            keyboardType = KeyboardType.Email,
        )
        Text("Para validación ciudadana y recuperación de clave", color = VozNavy, fontSize = 9.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            RegisterLabel("Contraseña Segura", Modifier.weight(1f))
            Text("Fortaleza: ${passwordStrengthLabel(password)}", color = VozMuted, fontSize = 9.sp)
        }
        RegisterField(
            value = password,
            onValueChange = { password = it; error = "" },
            placeholder = "Mínimo 8 caracteres",
            icon = { Icon(Icons.Default.Lock, null, tint = VozMuted, modifier = Modifier.size(16.dp)) },
            keyboardType = KeyboardType.Password,
            password = true,
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
        )
        StrengthBar(password)
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDF3FF)).padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Security, null, tint = Color(0xFF5783C1), modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(5.dp))
            Text("Mínimo 8 caracteres, al menos un número o símbolo", color = VozMuted, fontSize = 9.sp)
        }

        RegisterLabel("Confirmar Contraseña")
        RegisterField(
            value = confirmation,
            onValueChange = { confirmation = it; error = "" },
            placeholder = "Repite tu contraseña",
            icon = { Icon(Icons.Default.Lock, null, tint = VozMuted, modifier = Modifier.size(16.dp)) },
            keyboardType = KeyboardType.Password,
            password = true,
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
        )

        ConsentRow(
            checked = acceptedTerms,
            onCheckedChange = { acceptedTerms = it; error = "" },
            text = "Acepto los términos cívicos de convivencia y veracidad de reportes.",
            onTextClick = { showTerms = true },
        )
        ConsentRow(
            checked = receiveAlerts,
            onCheckedChange = { receiveAlerts = it },
            text = "Recibir notificaciones sobre alertas de seguridad en mi sector.",
        )

        if (error.isNotBlank()) {
            Text(error, color = Color(0xFFB42318), fontSize = 11.sp, lineHeight = 15.sp)
        }

        Button(
            onClick = {
                val cleanEmail = email.trim()
                error = when {
                    name.trim().length < 2 -> "Escribe tu nombre completo."
                    !cleanEmail.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Escribe un correo electrónico válido."
                    password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
                    !password.any { it.isDigit() || !it.isLetterOrDigit() } -> "Agrega al menos un número o un símbolo a la contraseña."
                    password != confirmation -> "Las contraseñas no coinciden."
                    !acceptedTerms -> "Debes aceptar los términos para crear tu cuenta."
                    else -> ""
                }
                if (error.isBlank()) {
                    loading = true
                    onCreateAccount(name.trim(), cleanEmail, phone.trim(), password) { ok, err ->
                        loading = false
                        if (ok) {
                            onLoginClick()
                        } else {
                            error = err ?: "Ya existe una cuenta con ese correo o hubo un error."
                        }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text("Crear Cuenta Comunitaria", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("¿Ya eres miembro? ", color = VozMuted, fontSize = 10.sp)
            Text(
                "Inicia sesión",
                color = VozNavy,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onLoginClick),
            )
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text("Términos de convivencia", color = VozNavy) },
            text = { Text("Al crear tu cuenta, te comprometes a compartir información veraz, respetar a la comunidad y reportar situaciones de forma responsable.") },
            confirmButton = { TextButton(onClick = { acceptedTerms = true; showTerms = false }) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = { showTerms = false }) { Text("Cerrar") } },
        )
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = VozNavy, modifier = Modifier.size(19.dp))
        }
        Box(Modifier.size(25.dp).clip(CircleShape).background(VozNavy), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(17.dp))
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF59B2FA), modifier = Modifier.size(9.dp).align(Alignment.Center))
        }
        Spacer(Modifier.width(7.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("VozBarrial", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Registro", color = VozMuted, fontSize = 9.sp)
        }
        Icon(Icons.Default.Security, contentDescription = "Registro seguro", tint = Color(0xFF43A696), modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun RegisterLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = VozNavy, fontSize = 10.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    password: Boolean = false,
    visible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(47.dp),
        singleLine = true,
        placeholder = { Text(placeholder, color = VozMuted, fontSize = 10.sp) },
        leadingIcon = icon,
        trailingIcon = if (password) {
            {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = VozMuted,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        } else null,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(8.dp),
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = VozNavy),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFFB9C7DE),
            unfocusedBorderColor = Color(0xFFE8EDF5),
        ),
    )
}

@Composable
private fun StrengthBar(password: String) {
    val hasLength = password.length >= 8
    val hasNumberOrSymbol = password.any { it.isDigit() || !it.isLetterOrDigit() }
    val level = when {
        password.isEmpty() -> 0
        hasLength && hasNumberOrSymbol -> 3
        password.length >= 5 -> 2
        else -> 1
    }
    val activeColor = when (level) {
        3 -> Color(0xFF12A878)
        2 -> Color(0xFFF3AF3D)
        else -> Color(0xFFE07171)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(3) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(if (index < level) activeColor else Color(0xFFE7EBF2)),
            )
        }
    }
}

private fun passwordStrengthLabel(password: String): String {
    val meetsRules = password.length >= 8 && password.any { it.isDigit() || !it.isLetterOrDigit() }
    return when {
        password.isEmpty() -> "Baja"
        meetsRules && password.length >= 12 -> "Alta"
        meetsRules -> "Media"
        else -> "Baja"
    }
}

@Composable
private fun ConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    onTextClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(25.dp),
        )
        Text(
            text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 3.dp, top = 4.dp)
                .then(if (onTextClick != null) Modifier.clickable(onClick = onTextClick) else Modifier),
            color = VozNavy,
            fontSize = 9.sp,
            lineHeight = 12.sp,
        )
    }
}

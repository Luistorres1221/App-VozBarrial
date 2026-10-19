package com.vozbarrial.features.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AdminProfileNavy = Color(0xFF09243A)

@Composable
fun AdminProfileScreen(
    name: String,
    email: String,
    phone: String,
    onBack: () -> Unit,
    onSave: (String, String, String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onSaved: () -> Unit,
) {
    var fullName by remember(name) { mutableStateOf(name) }
    var mobile by remember(phone) { mutableStateOf(phone) }
    var newEmail by remember(email) { mutableStateOf(email) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FC))) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !saving) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AdminProfileNavy)
            }
            Column {
                Text("Perfil de administrador", color = AdminProfileNavy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Actualiza tus datos de acceso y contacto", color = Color(0xFF718198), fontSize = 12.sp)
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(62.dp).clip(CircleShape).background(Color(0xFFE8EFF8)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = AdminProfileNavy, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Administrador", color = AdminProfileNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Los cambios de acceso requieren tu contraseña actual", color = Color(0xFF718198), fontSize = 11.sp)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Datos personales", color = AdminProfileNavy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre completo") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it.filter { c -> c.isDigit() || c == '+' || c == ' ' || c == '-' } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Celular") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it.trim(); error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo electrónico") },
                        singleLine = true
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Cambiar contraseña", color = AdminProfileNavy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Déjala vacía si no quieres cambiarla.", color = Color(0xFF718198), fontSize = 11.sp)
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; error = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirmar nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            }
            error?.let { Text(it, color = Color(0xFFC62828), fontSize = 12.sp) }
        }

        Button(
            onClick = {
                val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()
                when {
                    fullName.trim().length < 3 -> error = "Escribe tu nombre completo."
                    !emailValid -> error = "Escribe un correo electrónico válido."
                    (newEmail != email || newPassword.isNotBlank()) && currentPassword.isBlank() -> error = "Escribe tu contraseña actual para cambiar los datos de acceso."
                    newPassword.isNotBlank() && newPassword.length < 6 -> error = "La nueva contraseña debe tener al menos 6 caracteres."
                    newPassword != confirmPassword -> error = "Las contraseñas nuevas no coinciden."
                    else -> {
                        saving = true
                        error = null
                        onSave(fullName.trim(), mobile.trim(), newEmail.trim(), currentPassword, newPassword) { success, message ->
                            saving = false
                            if (success) onSaved() else error = message ?: "No se pudieron guardar los cambios."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).height(50.dp),
            enabled = !saving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AdminProfileNavy)
        ) {
            if (saving) CircularProgressIndicator(Modifier.size(19.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Guardar cambios", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

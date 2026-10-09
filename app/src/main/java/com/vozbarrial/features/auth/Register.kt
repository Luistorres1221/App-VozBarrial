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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.vozbarrial.features.auth.presentation.viewmodel.RegisterViewModel

@Composable
fun Register(
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccount: (name: String, email: String, phone: String, password: String, (Boolean, String?) -> Unit) -> Unit,
) {
    val registerViewModel: RegisterViewModel = viewModel()
    val state by registerViewModel.uiState.collectAsStateWithLifecycle()
    val name = state.name
    val email = state.email
    val phone = state.phone
    val password = state.password
    val confirmation = state.confirmation
    val passwordVisible = state.passwordVisible
    val acceptedTerms = state.acceptedTerms
    val receiveAlerts = state.receiveAlerts
    val error = state.error.orEmpty()
    val showTerms = state.showTerms
    val loading = state.loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RegisterHeader(onBack = { registerViewModel.reset(); onBack() })
        Text("Únete a tu comunidad", color = VozNavy, fontSize = 26.sp, fontWeight = FontWeight.Bold)

        RegisterLabel("Nombre Completo")
        RegisterField(
            value = name,
            onValueChange = registerViewModel::updateName,
            placeholder = "ej. Carlos Mendoza",
            icon = { Icon(Icons.Default.Person, null, tint = VozMuted, modifier = Modifier.size(19.dp)) },
            keyboardType = KeyboardType.Text,
        )

        RegisterLabel("Correo electrónico")
        RegisterField(
            value = email,
            onValueChange = registerViewModel::updateEmail,
            placeholder = "ej. carlos.mendoza@correo.com",
            icon = { Icon(Icons.Default.MailOutline, null, tint = VozMuted, modifier = Modifier.size(19.dp)) },
            keyboardType = KeyboardType.Email,
        )
        Text("Usaremos tu correo para validar tu cuenta y recuperar el acceso.", color = VozNavy, fontSize = 11.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            RegisterLabel("Contraseña segura", Modifier.weight(1f))
            Text("Fortaleza: ${passwordStrengthLabel(password)}", color = VozMuted, fontSize = 11.sp)
        }
        RegisterField(
            value = password,
            onValueChange = registerViewModel::updatePassword,
            placeholder = "Mínimo 8 caracteres",
            icon = { Icon(Icons.Default.Lock, null, tint = VozMuted, modifier = Modifier.size(18.dp)) },
            keyboardType = KeyboardType.Password,
            password = true,
            visible = passwordVisible,
            onToggleVisibility = registerViewModel::togglePasswordVisibility,
        )
        StrengthBar(password)
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDF3FF)).padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Security, null, tint = Color(0xFF5783C1), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("Usa al menos 8 caracteres e incluye un número o un símbolo.", color = VozMuted, fontSize = 11.sp)
        }

        RegisterLabel("Confirmar contraseña")
        RegisterField(
            value = confirmation,
            onValueChange = registerViewModel::updateConfirmation,
            placeholder = "Escribe de nuevo tu contraseña",
            icon = { Icon(Icons.Default.Lock, null, tint = VozMuted, modifier = Modifier.size(18.dp)) },
            keyboardType = KeyboardType.Password,
            password = true,
            visible = passwordVisible,
            onToggleVisibility = registerViewModel::togglePasswordVisibility,
        )

        ConsentRow(
            checked = acceptedTerms,
            onCheckedChange = registerViewModel::setTermsAccepted,
            text = "Acepto los términos cívicos de convivencia y veracidad de reportes.",
            onTextClick = { registerViewModel.showTerms(true) },
        )
        ConsentRow(
            checked = receiveAlerts,
            onCheckedChange = registerViewModel::setAlertsEnabled,
            text = "Recibir notificaciones sobre alertas de seguridad en mi sector.",
        )

        if (error.isNotBlank()) {
            Text(error, color = Color(0xFFB42318), fontSize = 11.sp, lineHeight = 15.sp)
        }

        Button(
            onClick = { registerViewModel.submit(onCreateAccount, onLoginClick) },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Crear Cuenta Comunitaria", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("¿Ya eres miembro? ", color = VozMuted, fontSize = 15.sp)
            Text(
                "Inicia sesión",
                color = VozNavy,
                fontSize =  15.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onLoginClick),
            )
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { registerViewModel.showTerms(false) },
            title = { Text("Normas de convivencia", color = VozNavy) },
            text = { Text("Al crear tu cuenta, te comprometes a compartir información veraz, respetar a la comunidad y reportar situaciones de forma responsable.") },
            confirmButton = { TextButton(onClick = { registerViewModel.setTermsAccepted(true); registerViewModel.showTerms(false) }) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick = { registerViewModel.showTerms(false) }) { Text("Cerrar") } },
        )
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(46.dp).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = VozNavy, modifier = Modifier.size(19.dp))
        }
        Box(Modifier.size(28.dp).clip(CircleShape).background(VozNavy), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(19.dp))
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF59B2FA), modifier = Modifier.size(9.dp).align(Alignment.Center))
        }
        Spacer(Modifier.width(7.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("VozBarrial", color = VozNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Registro", color = VozMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.Security, contentDescription = "Registro seguro", tint = Color(0xFF43A696), modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun RegisterLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
        modifier = Modifier.fillMaxWidth().height(56.dp),
        singleLine = true,
        placeholder = { Text(placeholder, color = VozMuted, fontSize = 13.sp) },
        leadingIcon = icon,
        trailingIcon = if (password) {
            {
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = VozMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        } else null,
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp, color = VozNavy),
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
                    .height(5.dp)
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
            modifier = Modifier.size(28.dp),
        )
        Text(
            text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 5.dp, top = 3.dp)
                .then(if (onTextClick != null) Modifier.clickable(onClick = onTextClick) else Modifier),
            color = VozNavy,
            fontSize =  14.sp,
            lineHeight =  20.sp,
        )
    }
}









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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vozbarrial.R
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
    val uiState by recoveryViewModel.uiState.collectAsStateWithLifecycle()
    val emptyEmailMsg = stringResource(R.string.email_empty_error)
    val networkErrorMsg = stringResource(R.string.network_error)
    val defaultErrorMsg = stringResource(R.string.password_reset_error)
    val exit = {
        recoveryViewModel.reset()
        onBack()
    }

    androidx.activity.compose.BackHandler(onBack = exit)

    PasswordRecoveryContent(
        uiState = uiState,
        onBack = exit,
        onEmailChange = recoveryViewModel::updateEmail,
        onRequestReset = { recoveryViewModel.requestReset(onRequestPasswordReset, emptyEmailMsg, networkErrorMsg, defaultErrorMsg) }
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
                    contentDescription = stringResource(R.string.back),
                    tint = VozNavy,
                    modifier = Modifier.size(18.dp)
                )
            }
            Image(
                painter = painterResource(R.mipmap.logo_foreground),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(27.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(stringResource(R.string.app_name), color = VozMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.app_name).uppercase(), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.recover_password_header),
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
                    stringResource(R.string.request_processed),
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
                    stringResource(R.string.check_your_email),
                    color = VozNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.recovery_email_sent_msg),
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
                        Text(stringResource(R.string.to_continue), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        RecoveryStep("1", stringResource(R.string.recovery_step_1))
                        RecoveryStep("2", stringResource(R.string.recovery_step_2))
                        RecoveryStep("3", stringResource(R.string.recovery_step_3))
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
                ) {
                    Text(stringResource(R.string.back_to_login), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                // Formulario de Solicitud
                Text(
                    stringResource(R.string.secure_account_recovery),
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
                    stringResource(R.string.forgot_password_title),
                    color = VozNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(R.string.forgot_password_subtitle),
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
                        stringResource(R.string.security_notice),
                        color = VozMuted,
                        fontSize = 12.sp,
                        lineHeight = 13.sp,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.email_label), Modifier.fillMaxWidth(), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    placeholder = { Text(stringResource(R.string.email_placeholder), fontSize = 12.sp) },
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
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF52627A), modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.use_registered_email), color = VozMuted, fontSize = 12.sp, lineHeight = 13.sp)
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
                        Text(stringResource(R.string.send_recovery_link), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                            Text(stringResource(R.string.what_happens_next), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        RecoveryStep("1", stringResource(R.string.recovery_step_1_box))
                        RecoveryStep("2", stringResource(R.string.recovery_step_2_box))
                        RecoveryStep("3", stringResource(R.string.recovery_step_3_box))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(VozBlueSurface, RoundedCornerShape(9.dp))
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("✉", color = VozNavy, fontSize = 12.sp)
                            Spacer(Modifier.width(5.dp))
                            Text(stringResource(R.string.spam_folder_notice), color = VozNavy, fontSize = 12.sp, lineHeight = 13.sp)
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

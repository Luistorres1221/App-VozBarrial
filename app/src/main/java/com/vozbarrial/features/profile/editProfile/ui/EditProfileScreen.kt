package com.vozbarrial.features.profile.editProfile.ui

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vozbarrial.R
import com.vozbarrial.features.profile.editProfile.presentation.EditProfileUiState
import com.vozbarrial.features.profile.editProfile.presentation.EditProfileViewModel
import com.vozbarrial.ui.theme.VozBackground
import com.vozbarrial.ui.theme.VozBlueSurface
import com.vozbarrial.ui.theme.VozGreen
import com.vozbarrial.ui.theme.VozNavy

@Composable
fun EditProfileRoute(
    name: String,
    photo: String?,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val invalidNameError = stringResource(R.string.error_invalid_full_name)
    val invalidFormatError = stringResource(R.string.error_invalid_image_format)
    val imageSizeError = stringResource(R.string.error_image_size_exceeded)

    LaunchedEffect(name, photo) {
        viewModel.initialize(name, photo)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri)
            var size = 0L
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) size = cursor.getLong(0)
            }
            when {
                mime !in listOf("image/jpeg", "image/png") -> viewModel.showError(invalidFormatError)
                size > 5 * 1024 * 1024 -> viewModel.showError(imageSizeError)
                else -> {
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) { }
                    viewModel.setPhoto(uri.toString())
                }
            }
        }
    }

    EditProfileContent(
        uiState = uiState,
        onCancel = onCancel,
        onSave = { viewModel.save(context, onSave, invalidNameError) },
        onNameChange = viewModel::updateName,
        onPickPhotoClick = { picker.launch(arrayOf("image/jpeg", "image/png")) },
        onRemovePhoto = { viewModel.setPhoto(null) },
        onDismissError = viewModel::clearError
    )
}

/**
 * UI Stateless pura para Editar Perfil.
 */
@Composable
fun EditProfileContent(
    uiState: EditProfileUiState,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onPickPhotoClick: () -> Unit,
    onRemovePhoto: () -> Unit,
    onDismissError: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(VozBackground)) {
        // TopBar
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = VozNavy
                )
            }
            Surface(
                modifier = Modifier.size(25.dp),
                shape = CircleShape,
                color = VozNavy
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "V",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                text = stringResource(R.string.edit_profile_title),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = VozNavy
            )
        }

        // Formulario
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card de Foto
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(104.dp), contentAlignment = Alignment.BottomEnd) {
                        if (!uiState.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = uiState.photoUri,
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = Color(0xFFDCEBE7),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = uiState.name.take(1).uppercase().ifBlank { "U" },
                                        color = VozGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp
                                    )
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable(onClick = onPickPhotoClick),
                            shape = CircleShape,
                            color = VozNavy,
                            shadowElevation = 3.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.change_photo),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = uiState.name,
                        color = VozNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.photo_upload_instructions),
                        modifier = Modifier.padding(top = 2.dp),
                        textAlign = TextAlign.Center,
                        color = VozNavy,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                    if (!uiState.photoUri.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = onRemovePhoto,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Quitar foto",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Card de Datos Personales
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.personal_data_header),
                        color = VozNavy,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.full_name_label),
                        color = VozNavy,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.full_name_placeholder), fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF68788B))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VozBlueSurface,
                            unfocusedContainerColor = VozBlueSurface,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Botones de acción
            Button(
                onClick = onSave,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VozNavy),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.save_changes),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clickable(onClick = onCancel),
                shape = RoundedCornerShape(11.dp),
                color = VozBlueSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.cancel_changes),
                        color = VozNavy,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }

    // Modal de Error
    uiState.error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text(stringResource(R.string.dialog_check_data_title), color = VozNavy) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text(stringResource(R.string.dialog_understood), color = VozGreen)
                }
            }
        )
    }
}

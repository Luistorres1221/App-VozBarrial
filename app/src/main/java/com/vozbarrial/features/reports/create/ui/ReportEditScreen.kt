package com.vozbarrial.features.reports.create.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vozbarrial.R
import com.vozbarrial.domain.model.Report
import com.vozbarrial.ui.theme.*

private data class EditReportCategory(val id: String, val name: String, val description: String, val icon: String)

@Composable
private fun getEditReportCategories(): List<EditReportCategory> {
    return listOf(
        EditReportCategory("Seguridad", stringResource(R.string.category_security), stringResource(R.string.category_security_desc), "🛡"),
        EditReportCategory("Emergencias Médicas", stringResource(R.string.category_emergencies), stringResource(R.string.category_emergencies_desc), "✚"),
        EditReportCategory("Infraestructura y Vías", stringResource(R.string.category_infrastructure), stringResource(R.string.category_infrastructure_desc), "🚧"),
        EditReportCategory("Mascotas y Animales", stringResource(R.string.category_pets), stringResource(R.string.category_pets_desc), "🐾"),
        EditReportCategory("Comunidad y Entorno", stringResource(R.string.category_community), stringResource(R.string.category_community_desc), "♧"),
        EditReportCategory("Alumbrado y Servicios", stringResource(R.string.category_lighting), stringResource(R.string.category_lighting_desc), "💡"),
    )
}

@Composable
fun ReportEditRoute(
    report: Report,
    onBack: () -> Unit,
    onUpdate: (title: String, description: String, category: String, photos: List<String>, done: (Boolean, String?) -> Unit) -> Unit
) {
    var title by rememberSaveable(report.id) { mutableStateOf(report.title) }
    var description by rememberSaveable(report.id) { mutableStateOf(report.description) }
    var category by rememberSaveable(report.id) { mutableStateOf(report.category.ifBlank { "Seguridad" }) }
    var photos by rememberSaveable(report.id) {
        mutableStateOf(report.imageUrl?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList())
    }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var categoryMenu by remember { mutableStateOf(false) }

    val editReportCategories = getEditReportCategories()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && photos.size < 2) {
            photos = photos + uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = VozNavy, modifier = Modifier.size(19.dp))
            }
            Box(
                modifier = Modifier.size(29.dp).clip(CircleShape).background(VozNavy),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.edit_and_correct_report), color = VozNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.adjust_details_review), color = VozMuted, fontSize = 10.sp)
            }
        }

        // Rejection Reason Banner
        if (!report.rejectionReason.isNullOrBlank() || !report.rejectionExplanation.isNullOrBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.rejection_reason),
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    if (!report.rejectionReason.isNullOrBlank()) {
                        Text(
                            text = "${stringResource(R.string.rejection_reason_label)} ${report.rejectionReason}",
                            color = Color(0xFF7F1D1D),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                    if (!report.rejectionExplanation.isNullOrBlank()) {
                        Text(
                            text = "${stringResource(R.string.rejection_detail)}${report.rejectionExplanation}",
                            color = Color(0xFF7F1D1D),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Section 1: Category
        Section(
            number = "1",
            title = stringResource(R.string.select_category),
            subtitle = stringResource(R.string.choose_most_suitable),
            badgeText = stringResource(R.string.required),
            badgeColor = Color(0xFFFFEDD5),
            badgeTextColor = Color(0xFFC2410C)
        ) {
            Box {
                OutlinedButton(
                    onClick = { categoryMenu = true },
                    modifier = Modifier.fillMaxWidth().height(53.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    val current = editReportCategories.firstOrNull { it.id == category } ?: editReportCategories[0]
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(current.icon, fontSize = 17.sp)
                        Spacer(Modifier.width(9.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(current.name, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(current.description, color = VozMuted, fontSize = 10.sp, maxLines = 1)
                        }
                        Text("▾", color = VozNavy, fontSize = 16.sp)
                    }
                }
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    editReportCategories.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(option.icon, fontSize = 15.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(option.name, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(option.description, color = VozMuted, fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            },
                            onClick = { category = option.id; categoryMenu = false }
                        )
                    }
                }
            }
        }

        // Section 2: Photos
        Section(
            number = "2",
            title = stringResource(R.string.photographic_evidence),
            subtitle = stringResource(R.string.max_2_photos),
            badgeText = stringResource(R.string.optional),
            badgeColor = Color(0xFFD1FAE5),
            badgeTextColor = Color(0xFF047857)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.forEachIndexed { index, uri ->
                    Box(modifier = Modifier.size(72.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photos = photos.filterIndexed { i, _ -> i != index } },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                if (photos.size < 2) {
                    Column(
                        Modifier.size(72.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).clickable { picker.launch("image/*") },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = VozMuted, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.upload), color = VozMuted, fontSize = 9.sp)
                    }
                }
            }
        }

        // Section 3: Title & Description
        Section(
            number = "3",
            title = stringResource(R.string.incident_description),
            subtitle = stringResource(R.string.corrected_title_desc),
            badgeText = stringResource(R.string.required),
            badgeColor = Color(0xFFFFEDD5),
            badgeTextColor = Color(0xFFC2410C)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    label = { Text(stringResource(R.string.report_title_label)) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    shape = RoundedCornerShape(10.dp),
                    label = { Text(stringResource(R.string.report_description_label)) }
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(errorMessage!!, color = Color(0xFFDC2626), fontSize = 12.sp)
        }

        val errorCompleteMsg = stringResource(R.string.error_complete_title_desc)
        val errorUpdateMsg = stringResource(R.string.report_updated_error)

        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    errorMessage = errorCompleteMsg
                    return@Button
                }
                isUploading = true
                errorMessage = null
                onUpdate(title, description, category, photos) { success, err ->
                    isUploading = false
                    if (success) {
                        onBack()
                    } else {
                        errorMessage = err ?: errorUpdateMsg
                    }
                }
            },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.update_and_resend_report), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun Section(
    number: String,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFFFEDD5),
    badgeTextColor: Color = Color(0xFFC2410C),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(VozNavy), contentAlignment = Alignment.Center) {
                    Text(number, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = VozMuted, fontSize = 9.sp)
                }
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            color = badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            content()
        }
    }
}

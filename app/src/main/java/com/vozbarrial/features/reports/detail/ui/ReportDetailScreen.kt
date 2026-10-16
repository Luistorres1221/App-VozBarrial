package com.vozbarrial.features.reports.detail.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vozbarrial.domain.model.Report
import com.vozbarrial.features.reports.detail.presentation.ReportDetailViewModel
import com.vozbarrial.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportDetailRoute(
    report: Report,
    userName: String,
    userPhoto: String?,
    onBack: () -> Unit,
    viewModel: ReportDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(report) {
        viewModel.setReport(report)
    }

    ReportDetailScreen(
        uiState = uiState,
        userName = userName,
        userPhoto = userPhoto,
        onBack = onBack,
        onContributionChange = viewModel::onNewContributionChanged,
        onAddPhoto = viewModel::addContributionPhoto,
        onRemovePhoto = viewModel::removeContributionPhoto,
        onSubmitContribution = { viewModel.addContribution(context, userName) }
    )
}

@Composable
private fun ReportDetailScreen(
    uiState: com.vozbarrial.features.reports.detail.presentation.ReportDetailUiState,
    userName: String,
    userPhoto: String?,
    onBack: () -> Unit,
    onContributionChange: (String) -> Unit,
    onAddPhoto: (String?) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onSubmitContribution: () -> Unit
) {
    val report = uiState.report
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        onAddPhoto(uri?.toString())
    }

    val formattedDate = remember(report.timestamp) {
        try {
            val sdf = SimpleDateFormat("dd 'de' MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))
            sdf.format(Date(report.timestamp))
        } catch (_: Exception) {
            "Hoy, 14:32 hrs"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = VozNavy)
                }
                Spacer(Modifier.width(8.dp))
                Text("Detalle Incidencia", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Surface(shape = CircleShape, color = Color(0xFFDCEBE7), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (!userPhoto.isNullOrBlank()) {
                        AsyncImage(model = userPhoto, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Text(userName.take(1).uppercase(), color = VozGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFEAF1F8)) {
                                Text("#REP-${report.id.take(8).uppercase()}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = VozNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFFEE2E2)) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Alerta Activa", color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir", tint = VozNavy, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Verified Official Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Verificado Oficialmente", color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Relevance Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0F6FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Nivel de Relevancia: Alta", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(Modifier.weight(1f))
                                Text("Severidad 4/5", color = VozMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(4) { Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(50)).background(Color(0xFFF59E0B))) }
                                Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(50)).background(Color(0xFFE2E8F0)))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Prioridad calculada basada en ${uiState.supportCount} respaldos comunitarios y riesgo reportado.", color = VozMuted, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = report.title.ifBlank { "Incidencia vecinal reportada" },
                        color = VozNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // Image Gallery Card
            val imageUrls = remember(report.imageUrl) {
                report.imageUrl?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
            }

            if (imageUrls.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { imageUrls.size })
                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = imageUrls[page],
                                contentDescription = "Evidencia ${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${imageUrls.size} fotos",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Description Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(VozGreen))
                        Spacer(Modifier.width(8.dp))
                        Text("Descripción del Incidente", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = report.description.ifBlank { "Sin descripción detallada proporcionada para este reporte." },
                        color = VozNavy,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Author Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val authorName = report.authorName.ifBlank { report.authorEmail.substringBefore("@").ifBlank { "Vecino" } }
                    Surface(shape = CircleShape, color = Color(0xFFDCEBE7), modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(authorName.take(1).uppercase(), color = VozGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(authorName, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Guardián (1,450 pts)", color = VozGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Text("Publicado $formattedDate", color = VozMuted, fontSize = 10.sp)
                    }
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFEAF1F8)) {
                        Text("Autor del Reporte", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = VozNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Citizen Contributions Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Aportes Ciudadanos (${uiState.contributions.size})", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("En vivo", color = VozGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Input box
                    OutlinedTextField(
                        value = uiState.newContributionText,
                        onValueChange = onContributionChange,
                        placeholder = { Text("Aporta datos, testigos o fotos adicionales...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VozGreen,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Selected photos preview (max 2)
                    if (uiState.contributionPhotoUris.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            uiState.contributionPhotoUris.forEachIndexed { index, uriStr ->
                                Box(modifier = Modifier.size(64.dp)) {
                                    AsyncImage(
                                        model = uriStr,
                                        contentDescription = "Foto aporte",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { onRemovePhoto(index) },
                                        modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (uiState.contributionPhotoUris.size < 2) {
                                        photoPickerLauncher.launch("image/*")
                                    }
                                },
                                enabled = uiState.contributionPhotoUris.size < 2 && !uiState.isUploading
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Adjuntar foto", tint = if (uiState.contributionPhotoUris.size < 2) VozNavy else VozMuted)
                            }
                            Text(
                                text = "Fotos (${uiState.contributionPhotoUris.size}/2 opcional)",
                                color = VozMuted,
                                fontSize = 10.sp
                            )
                        }

                        Button(
                            onClick = onSubmitContribution,
                            enabled = (!uiState.newContributionText.isBlank() || uiState.contributionPhotoUris.isNotEmpty()) && !uiState.isUploading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF071C2D))
                        ) {
                            if (uiState.isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("Enviando...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Enviar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Contributions list
                    uiState.contributions.forEach { contrib ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(contrib.authorName, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(50), color = if (contrib.isOfficial) Color(0xFFD1FAE5) else Color(0xFFEAF1F8)) {
                                        Text(contrib.authorRole, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = if (contrib.isOfficial) Color(0xFF059669) else VozNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text(contrib.timeAgo, color = VozMuted, fontSize = 10.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                if (contrib.content.isNotBlank()) {
                                    Text("\"${contrib.content}\"", color = VozNavy, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    Spacer(Modifier.height(6.dp))
                                }
                                if (contrib.imageUrls.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        contrib.imageUrls.forEach { imgUrl ->
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = "Foto aporte",
                                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

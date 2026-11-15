package com.vozbarrial.features.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vozbarrial.R
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.ReportStatus
import com.vozbarrial.features.map.MapFilter
import com.vozbarrial.features.map.formatTimeAgo
import com.vozbarrial.features.map.getLocalizedCategoryName
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.ui.theme.*

@Composable
fun AdminResolutionScreen(
    name: String,
    profilePhoto: String? = null,
    equippedFrame: String = "",
    reports: List<Report> = emptyList(),
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onResolveReport: (String, (Result<Unit>) -> Unit) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("TODOS") }
    val resolvingReportIds = remember { mutableStateSetOf<String>() }

    val reportSnapshot = reports.toList()
    val baseReports = remember(reportSnapshot) {
        reportSnapshot.filter { r ->
            val status = r.status.trim().uppercase()
            status == "VERIFICADO" || ReportStatus.fromString(r.status) == ReportStatus.VERIFICADO
        }
    }
    val filteredReports = remember(baseReports, selectedCategory) {
        baseReports.filter { r ->
            selectedCategory == "TODOS" || when (selectedCategory) {
                "Seguridad" -> r.category.contains("seguridad", true)
                "Emergencias Médicas" -> r.category.contains("emergencia", true)
                "Infraestructura y Vías", "Infraestructura" -> r.category.contains("infraestructura", true) || r.category.contains("vías", true)
                "Mascotas y Animales" -> r.category.contains("mascota", true) || r.category.contains("animal", true)
                "Comunidad y Entorno" -> r.category.contains("comunidad", true) || r.category.contains("entorno", true)
                "Alumbrado y Servicios" -> r.category.contains("alumbrado", true) || r.category.contains("servicio", true)
                else -> r.category.equals(selectedCategory, true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = VozNavy, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(VozNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.reports_resolution_title), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(stringResource(R.string.app_name), color = VozMuted, fontSize = 10.sp)
                }
                UserAvatarWithFrame(
                    profilePhoto = profilePhoto,
                    name = name,
                    frameName = equippedFrame,
                    size = 35.dp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MapFilter(
                text = stringResource(R.string.filter_all),
                count = baseReports.size,
                selected = selectedCategory == "TODOS",
                iconVector = Icons.Default.Map,
                onClick = { selectedCategory = "TODOS" }
            )
            MapFilter(
                text = stringResource(R.string.filter_security),
                count = baseReports.count { it.category.contains("seguridad", true) },
                selected = selectedCategory == "Seguridad",
                iconRes = R.drawable.ic_pin_security,
                onClick = {
                    selectedCategory = if (selectedCategory == "Seguridad") "TODOS" else "Seguridad"
                }
            )
            MapFilter(
                text = stringResource(R.string.filter_emergencies),
                count = baseReports.count { it.category.contains("emergencia", true) },
                selected = selectedCategory == "Emergencias Médicas",
                iconRes = R.drawable.ic_pin_emergencias,
                onClick = {
                    selectedCategory = if (selectedCategory == "Emergencias Médicas") "TODOS" else "Emergencias Médicas"
                }
            )
            MapFilter(
                text = stringResource(R.string.filter_infrastructure),
                count = baseReports.count {
                    it.category.contains(
                        "infraestructura",
                        true
                    ) || it.category.contains("vías", true)
                },
                selected = selectedCategory == "Infraestructura y Vías" || selectedCategory == "Infraestructura",
                iconRes = R.drawable.ic_pin_infraestructura,
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Infraestructura y Vías") "TODOS" else "Infraestructura y Vías"
                }
            )
            MapFilter(
                text = stringResource(R.string.filter_pets),
                count = baseReports.count {
                    it.category.contains("mascota", true) || it.category.contains("animal", true)
                },
                selected = selectedCategory == "Mascotas y Animales",
                iconRes = R.drawable.ic_pin_mascotas,
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Mascotas y Animales") "TODOS" else "Mascotas y Animales"
                }
            )
            MapFilter(
                text = stringResource(R.string.filter_community),
                count = baseReports.count {
                    it.category.contains("comunidad", true) || it.category.contains("entorno", true)
                },
                selected = selectedCategory == "Comunidad y Entorno",
                iconRes = R.drawable.ic_pin_comunidad,
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Comunidad y Entorno") "TODOS" else "Comunidad y Entorno"
                }
            )
            MapFilter(
                text = stringResource(R.string.filter_lighting),
                count = baseReports.count {
                    it.category.contains("alumbrado", true) || it.category.contains("servicio", true)
                },
                selected = selectedCategory == "Alumbrado y Servicios",
                iconRes = R.drawable.ic_pin_alumbrado,
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Alumbrado y Servicios") "TODOS" else "Alumbrado y Servicios"
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Reports List
        if (filteredReports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_reports_section), color = VozMuted, fontSize = 12.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredReports.forEach { report ->
                    val status = ReportStatus.fromString(report.status)
                    val canResolve = status == ReportStatus.POR_VERIFICAR || status == ReportStatus.VERIFICADO
                    ResolutionReportCard(
                        report = report,
                        onViewInfo = { onNavigate("reportDetail/${report.id}") },
                        onMap = { onBack() },
                        showResolve = canResolve,
                        isResolving = report.id in resolvingReportIds,
                        onResolve = {
                            if (canResolve && resolvingReportIds.add(report.id)) {
                                onResolveReport(report.id) { resolvingReportIds.remove(report.id) }
                            }
                        }
                    )
                }
            }
        }
    }
}



@Composable
private fun ResolutionReportCard(
    report: Report,
    onViewInfo: () -> Unit,
    onMap: () -> Unit,
    showResolve: Boolean,
    isResolving: Boolean,
    onResolve: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Category & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = getLocalizedCategoryName(report.category),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = Color(0xFF0369A1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "#REP-${report.id.takeLast(6).uppercase()}",
                        color = VozMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = VozMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(formatTimeAgo(report.timestamp), color = VozMuted, fontSize = 10.sp)
                }
            }

Spacer(Modifier.height(8.dp))

            // Title & Place
            Text(
                text = report.title,
                color = VozNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE34652), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    text = report.place.ifBlank { stringResource(R.string.admin_community_location) },
                    color = VozMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            // Image & Description Preview Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val firstImg = report.imageUrl?.split(",")?.firstOrNull()?.trim()
                    if (!firstImg.isNullOrBlank()) {
                        AsyncImage(
                            model = firstImg,
                            contentDescription = stringResource(R.string.admin_evidence),
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.description,
                            color = VozMuted,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Author row & Map button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(report.authorName.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VozNavy)
                    }
                    Column {
                        Text(report.authorName.ifBlank { stringResource(R.string.admin_default_citizen) }, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VozNavy, maxLines = 1)
                        Text(stringResource(R.string.active_collaborator), fontSize = 9.sp, color = VozMuted)
                    }
                }

                OutlinedButton(
                    onClick = onMap,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Map, null, tint = VozNavy, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.map_label), fontSize = 10.sp, color = VozNavy)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewInfo,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.view_info), fontSize = 11.sp, color = VozNavy)
                }
                if (showResolve) {
                    Button(
                        onClick = onResolve,
                        enabled = !isResolving,
                        modifier = Modifier.weight(1.5f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.resolved), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

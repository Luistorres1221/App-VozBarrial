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
import androidx.compose.material.icons.filled.Check
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
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.ui.theme.*

@Composable
fun AdminModerationScreen(
    name: String,
    profilePhoto: String? = null,
    equippedFrame: String = "",
    reports: List<Report> = emptyList(),
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onUpdateStatus: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTab by rememberSaveable { mutableStateOf("Pendientes") }
    var selectedCategory by rememberSaveable { mutableStateOf("TODOS") }

    val filteredReports = remember(reports, selectedTab, selectedCategory) {
        reports.filter { r ->
            val statusEnum = ReportStatus.fromString(r.status)
            val matchesTab = when (selectedTab) {
                "Pendientes" -> statusEnum == ReportStatus.POR_VERIFICAR
                "Aprobados" -> statusEnum == ReportStatus.VERIFICADO || statusEnum == ReportStatus.RESUELTO
                "Rechazados" -> statusEnum == ReportStatus.RECHAZADO
                else -> true
            }
            val matchesCat = selectedCategory == "TODOS" || when (selectedCategory) {
                "Seguridad" -> r.category.contains("seguridad", true)
                "Infraestructura" -> r.category.contains("infraestructura", true) || r.category.contains("vías", true)
                else -> r.category.equals(selectedCategory, true)
            }
            matchesTab && matchesCat
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = VozNavy, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(VozNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Panel de Moderación", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("VozBarrial", color = VozMuted, fontSize = 10.sp)
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

        // Tabs Row (Pendientes, Aprobados, Rechazados)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pendingCount = reports.count { ReportStatus.fromString(it.status) == ReportStatus.POR_VERIFICAR }
            val approvedCount = reports.count { ReportStatus.fromString(it.status) == ReportStatus.VERIFICADO || ReportStatus.fromString(it.status) == ReportStatus.RESUELTO }
            val rejectedCount = reports.count { ReportStatus.fromString(it.status) == ReportStatus.RECHAZADO }

            ModerationTabButton(
                title = "Pendientes",
                count = pendingCount,
                selected = selectedTab == "Pendientes",
                onClick = { selectedTab = "Pendientes" },
                modifier = Modifier.weight(1f)
            )
            ModerationTabButton(
                title = "Aprobados",
                count = approvedCount,
                selected = selectedTab == "Aprobados",
                onClick = { selectedTab = "Aprobados" },
                modifier = Modifier.weight(1f)
            )
            ModerationTabButton(
                title = "Rechazos",
                count = rejectedCount,
                selected = selectedTab == "Rechazados",
                onClick = { selectedTab = "Rechazados" },
                modifier = Modifier.weight(1f)
            )
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
                text = "Todos",
                count = reports.size,
                selected = selectedCategory == "TODOS",
                iconVector = Icons.Default.Map,
                onClick = { selectedCategory = "TODOS" }
            )
            MapFilter(
                text = "Seguridad",
                count = reports.count { it.category.contains("seguridad", true) },
                selected = selectedCategory == "Seguridad",
                iconRes = R.drawable.ic_pin_security,
                onClick = {
                    selectedCategory = if (selectedCategory == "Seguridad") "TODOS" else "Seguridad"
                }
            )
            MapFilter(
                text = "Infraestructura",
                count = reports.count {
                    it.category.contains(
                        "infraestructura",
                        true
                    ) || it.category.contains("vías", true)
                },
                selected = selectedCategory == "Infraestructura",
                iconRes = R.drawable.ic_pin_infraestructura,
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Infraestructura") "TODOS" else "Infraestructura"
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
                Text("No hay reportes en esta sección.", color = VozMuted, fontSize = 12.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredReports.forEach { report ->
                    ModerationReportCard(
                        report = report,
                        onViewInfo = { onNavigate("reportDetail/${report.id}") },
                        onMap = { onBack() },
                        onReject = { onNavigate("adminRejectReason/${report.id}") },
                        onApprove = { onUpdateStatus(report.id, "VERIFICADO") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModerationTabButton(
    title: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(36.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF071C2D) else Color.White,
        shadowElevation = if (selected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$title $count",
                color = if (selected) Color.White else VozNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModerationReportCard(
    report: Report,
    onViewInfo: () -> Unit,
    onMap: () -> Unit,
    onReject: () -> Unit,
    onApprove: () -> Unit
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
                            text = report.category,
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
                    text = report.place.ifBlank { "Ubicación comunitaria" },
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
                            contentDescription = "Evidencia",
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Validaciones barriales activas", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(2.dp))
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
                        Text(report.authorName.ifBlank { "Ciudadano" }, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VozNavy, maxLines = 1)
                        Text("Colaborador Activo", fontSize = 9.sp, color = VozMuted)
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
                    Text("Mapa", fontSize = 10.sp, color = VozNavy)
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
                    Text("Ver Info", fontSize = 11.sp, color = VozNavy)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Text("Rechazar", fontSize = 11.sp)
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1.2f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aprobar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

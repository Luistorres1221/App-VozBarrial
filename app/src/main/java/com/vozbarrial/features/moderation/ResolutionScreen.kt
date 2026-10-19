package com.vozbarrial.features.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.ReportStatus
import java.util.concurrent.TimeUnit

private val ResolutionNavy = Color(0xFF09243A)
private val ResolutionBlue = Color(0xFFEAF1FF)
private val ResolutionGreen = Color(0xFF00845A)

@Composable
fun ResolutionScreen(
    reports: List<Report>,
    initialTab: String = "En Proceso",
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenReport: (String) -> Unit,
    onModerate: (String, String, (Boolean, String?) -> Unit) -> Unit,
) {
    var tab by remember(initialTab) { mutableStateOf(initialTab) }
    var category by remember { mutableStateOf("Todos") }
    var busyId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val pending = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.POR_VERIFICAR }
    val inProcess = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.VERIFICADO }
    val history = reports.filter {
        ReportStatus.fromString(it.status) in listOf(ReportStatus.RESUELTO, ReportStatus.RECHAZADO)
    }
    val currentReports = when (tab) {
        "Pendientes" -> pending
        "Historial" -> history
        else -> inProcess
    }
    val categories = listOf("Todos") + currentReports.map { it.category.ifBlank { "Otros" } }.distinct()
    val filtered = currentReports.filter { category == "Todos" || it.category.equals(category, true) }

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F7FB))) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = ResolutionNavy) }
            Column(Modifier.weight(1f)) {
                Text("Resolución de Reportes", color = ResolutionNavy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("VozBarrial · Administración", color = Color(0xFF73839A), fontSize = 10.sp)
            }
            TextButton(onClick = onOpenMap) {
                Icon(Icons.Default.Map, null, tint = ResolutionNavy, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(3.dp)); Text("Mapa", color = ResolutionNavy, fontSize = 12.sp)
            }
        }

        Row(Modifier.fillMaxWidth().background(Color.White), horizontalArrangement = Arrangement.SpaceEvenly) {
            ResolutionTab("Pendientes", pending.size, tab == "Pendientes") { tab = "Pendientes" }
            ResolutionTab("En Proceso", inProcess.size, tab == "En Proceso") { tab = "En Proceso" }
            ResolutionTab("Historial", history.size, tab == "Historial") { tab = "Historial" }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { item ->
                FilterChip(
                    selected = category == item,
                    onClick = { category = item },
                    label = { Text(item, maxLines = 1, fontSize = 10.sp) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ResolutionNavy, selectedLabelColor = Color.White)
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    when (tab) {
                        "Pendientes" -> "No hay reportes pendientes"
                        "Historial" -> "Aún no hay reportes en el historial"
                        else -> "No hay reportes en proceso"
                    },
                    color = Color(0xFF73839A)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 9.dp, end = 9.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { report ->
                    ResolutionReportCard(
                        report = report,
                        tab = tab,
                        loading = busyId == report.id,
                        onView = { onOpenReport(report.id) },
                        onMap = onOpenMap,
                        onApprove = {
                            busyId = report.id
                            onModerate(report.id, ReportStatus.VERIFICADO.value) { ok, error ->
                                busyId = null
                                message = if (ok) "Reporte aprobado y enviado a En Proceso" else error ?: "No se pudo aprobar el reporte"
                            }
                        },
                        onReject = {
                            busyId = report.id
                            onModerate(report.id, ReportStatus.RECHAZADO.value) { ok, error ->
                                busyId = null
                                message = if (ok) "Reporte rechazado y enviado al Historial" else error ?: "No se pudo rechazar el reporte"
                            }
                        },
                        onResolve = {
                            busyId = report.id
                            onModerate(report.id, ReportStatus.RESUELTO.value) { ok, error ->
                                busyId = null
                                message = if (ok) "Reporte marcado como resuelto" else error ?: "No se pudo resolver el reporte"
                            }
                        }
                    )
                }
            }
        }
    }
    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("Resolución de reportes") },
            text = { Text(text) },
            confirmButton = { TextButton(onClick = { message = null }) { Text("Aceptar") } }
        )
    }
}

@Composable
private fun RowScope.ResolutionTab(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clickable(onClick = onClick).padding(top = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$label  $count", color = if (selected) ResolutionNavy else Color(0xFF75849A), fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 5.dp).clip(CircleShape).background(if (selected) ResolutionNavy else Color.Transparent))
    }
}

@Composable
private fun ResolutionReportCard(
    report: Report,
    tab: String,
    loading: Boolean,
    onView: () -> Unit,
    onMap: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onResolve: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val catColor = if (report.category.contains("seguridad", true)) Color(0xFFD84242) else Color(0xFF315B9B)
                Text(report.category.ifBlank { "Comunidad" }.uppercase(), color = catColor, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (catColor == Color(0xFFD84242)) Color(0xFFFFE7E5) else ResolutionBlue).padding(horizontal = 7.dp, vertical = 4.dp))
                Spacer(Modifier.width(6.dp))
                Text("#REP-${report.id.takeLast(6).uppercase()}", color = Color(0xFF6D7F95), fontSize = 8.sp, modifier = Modifier.weight(1f))
                Text("◷ ${timeLabel(report.timestamp)}", color = Color(0xFF6D7F95), fontSize = 8.sp)
            }
            Text(report.title, color = ResolutionNavy, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF53729A), modifier = Modifier.size(13.dp))
                Text(report.place.ifBlank { "Ubicación no especificada" }, color = Color(0xFF49617D), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (!report.imageUrl.isNullOrBlank()) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Color(0xFFF0F4FB)).padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = report.imageUrl.substringBefore(","), contentDescription = "Evidencia del reporte", modifier = Modifier.size(48.dp).clip(RoundedCornerShape(7.dp)))
                    Spacer(Modifier.width(7.dp))
                    Column {
                        Text("👍 ${report.votes} validaciones barriales", color = ResolutionNavy, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                        Text(report.description, color = Color(0xFF405873), fontSize = 9.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else if (report.description.isNotBlank()) {
                Text(report.description, color = Color(0xFF405873), fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF0F4FB)).padding(horizontal = 7.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(22.dp).background(Color(0xFFDCE8F8), CircleShape), contentAlignment = Alignment.Center) {
                    Text(report.authorName.firstOrNull()?.uppercase() ?: "U", color = ResolutionNavy, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(report.authorName.ifBlank { report.authorEmail.substringBefore("@") }, color = ResolutionNavy, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    Text("☆ Colaborador Activo · ${report.votes} pts", color = Color(0xFF6E7F95), fontSize = 7.sp)
                }
                TextButton(onClick = onMap, contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp)) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(12.dp)); Spacer(Modifier.width(2.dp)); Text("Mapa", fontSize = 8.sp)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                OutlinedButton(onClick = onView, modifier = Modifier.weight(1f).height(34.dp), enabled = !loading, contentPadding = PaddingValues(horizontal = 3.dp)) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(13.dp)); Spacer(Modifier.width(3.dp)); Text("Ver info", fontSize = 9.sp, maxLines = 1)
                }
                when (tab) {
                    "Pendientes" -> {
                        OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(34.dp), enabled = !loading, contentPadding = PaddingValues(horizontal = 2.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFFD84242), modifier = Modifier.size(13.dp)); Text("Rechazar", color = Color(0xFFD84242), fontSize = 8.sp, maxLines = 1)
                        }
                        Button(onClick = onApprove, modifier = Modifier.weight(1f).height(34.dp), enabled = !loading, contentPadding = PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = ResolutionGreen)) {
                            if (loading) CircularProgressIndicator(Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Default.Check, null, modifier = Modifier.size(13.dp))
                            Text("Aprobar", fontSize = 8.sp, maxLines = 1)
                        }
                    }
                    "En Proceso" -> Button(onClick = onResolve, modifier = Modifier.weight(1f).height(34.dp), enabled = !loading, contentPadding = PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = ResolutionGreen)) {
                        if (loading) CircularProgressIndicator(Modifier.size(13.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Default.Check, null, modifier = Modifier.size(13.dp))
                        Text("Resuelto", fontSize = 9.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

private fun timeLabel(timestamp: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes((System.currentTimeMillis() - timestamp).coerceAtLeast(0))
    return if (minutes < 60) "$minutes min" else "${minutes / 60} h"
}

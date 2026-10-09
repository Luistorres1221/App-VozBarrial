package com.vozbarrial.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vozbarrial.domain.Reporte
import com.vozbarrial.ui.theme.*

@Composable
fun CommunityPage(
    page: String,
    name: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onSaveName: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    profilePhoto: String?,
    points: Int,
    reports: List<Reporte>,
    onEditReport: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onDeleteReport: (String, (Boolean, String?) -> Unit) -> Unit,
) {
    var showNameEditor by rememberSaveable { mutableStateOf(false) }
    var showAccountDelete by rememberSaveable { mutableStateOf(false) }
    var reportToEdit by remember { mutableStateOf<Reporte?>(null) }
    var reportToDelete by remember { mutableStateOf<Reporte?>(null) }
    var nameDraft by remember(name) { mutableStateOf(name) }
    var titleDraft by remember(reportToEdit) { mutableStateOf(reportToEdit?.title.orEmpty()) }
    var descriptionDraft by remember(reportToEdit) { mutableStateOf(reportToEdit?.description.orEmpty()) }
    var operationMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var activityFilter by rememberSaveable { mutableStateOf("Todas") }
    val sortedReports = remember(reports) { reports.sortedByDescending { it.timestamp } }
    val verifiedCount = reports.count { it.status.equals("RESUELTO", true) || it.status.equals("VERIFICADO", true) }

    Column(Modifier.fillMaxSize().background(VozBackground)) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = VozNavy) }
            Text(
                when (page) { "profile" -> "Perfil ciudadano"; "activity" -> "Actividad"; else -> "Insignias y logros" },
                Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            )
            IconButton(onClick = { onNavigate("activity") }) {
                Icon(Icons.Default.Notifications, contentDescription = "Actividad", tint = VozNavy)
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (page) {
                "profile" -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = profilePhoto,
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier.size(62.dp).clip(CircleShape).background(VozBlueSurface),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(name.ifBlank { "Vecino" }, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text("Cuenta ciudadana", color = VozMuted, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { nameDraft = name; showNameEditor = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar nombre", tint = VozNavy)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryTile("Puntos", points.toString(), Modifier.weight(1f))
                                    SummaryTile("Reportes", reports.size.toString(), Modifier.weight(1f))
                                    SummaryTile("Resueltos", verifiedCount.toString(), Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onNavigate("frames") }, modifier = Modifier.weight(1f)) { Text("Mis marcos") }
                                    Button(onClick = { onNavigate("store") }, modifier = Modifier.weight(1f)) { Text("Tienda") }
                                }
                            }
                        }
                    }
                    item { Text("Mis publicaciones · ${reports.size}", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    if (sortedReports.isEmpty()) {
                        item { EmptyState("Todavía no has publicado reportes.") }
                    } else {
                        items(sortedReports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onEdit = { reportToEdit = report },
                                onDelete = { reportToDelete = report },
                            )
                        }
                    }
                    item {
                        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { showAccountDelete = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Eliminar cuenta permanentemente", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                "activity" -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Todas", "Activas", "Resueltas").forEach { filter ->
                                FilterChip(
                                    selected = activityFilter == filter,
                                    onClick = { activityFilter = filter },
                                    label = { Text(filter) },
                                )
                            }
                        }
                    }
                    val activityReports = sortedReports.filter { report ->
                        when (activityFilter) {
                            "Activas" -> report.status.equals("ACTIVO", true)
                            "Resueltas" -> report.status.equals("RESUELTO", true) || report.status.equals("VERIFICADO", true)
                            else -> true
                        }
                    }
                    if (activityReports.isEmpty()) item { EmptyState("No hay actividad en este filtro.") }
                    items(activityReports, key = { "activity:${it.id}" }) { report -> ReportCard(report) }
                }
                else -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = VozNavy)) {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Participación comunitaria", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("$points puntos · ${reports.size} reportes · $verifiedCount resueltos", color = Color.White)
                                LinearProgressIndicator(
                                    progress = { (reports.size / 10f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF55E0B0),
                                )
                            }
                        }
                    }
                    val milestones = listOf(
                        "Primer reporte" to (reports.size >= 1),
                        "Cinco reportes" to (reports.size >= 5),
                        "Primer caso resuelto" to (verifiedCount >= 1),
                        "Diez reportes" to (reports.size >= 10),
                    )
                    items(milestones) { (title, unlocked) ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (unlocked) "🏅" else "🔒", fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(title, color = VozNavy, fontWeight = FontWeight.Bold)
                                    Text(if (unlocked) "Desbloqueada" else "Sigue participando para desbloquearla", color = VozMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        NavigationBar(containerColor = Color.White) {
            listOf("profile" to "Perfil", "activity" to "Actividad", "badges" to "Insignias").forEach { (route, label) ->
                NavigationBarItem(
                    selected = page == route || (route == "badges" && page !in setOf("profile", "activity")),
                    onClick = { onNavigate(route) },
                    icon = { Text(when (route) { "profile" -> "●"; "activity" -> "◷"; else -> "★" }) },
                    label = { Text(label) },
                )
            }
        }
    }

    if (showNameEditor) AlertDialog(
        onDismissRequest = { showNameEditor = false },
        title = { Text("Editar nombre") },
        text = { OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, label = { Text("Nombre completo") }, singleLine = true) },
        confirmButton = {
            TextButton(onClick = {
                if (nameDraft.trim().isNotBlank()) onSaveName(nameDraft.trim())
                showNameEditor = false
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = { showNameEditor = false }) { Text("Cancelar") } },
    )

    reportToEdit?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToEdit = null },
            title = { Text("Editar reporte") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(titleDraft, { titleDraft = it.take(100) }, label = { Text("Título") }, singleLine = true)
                    OutlinedTextField(descriptionDraft, { descriptionDraft = it.take(1000) }, label = { Text("Descripción") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onEditReport(report.id, titleDraft, descriptionDraft) { success, error ->
                        operationMessage = if (success) "Reporte actualizado." else error ?: "No se pudo actualizar el reporte."
                        if (success) reportToEdit = null
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { reportToEdit = null }) { Text("Cancelar") } },
        )
    }

    reportToDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text("Eliminar reporte") },
            text = { Text("Se eliminará el reporte y su evidencia. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteReport(report.id) { success, error ->
                        operationMessage = if (success) "Reporte eliminado." else error ?: "No se pudo eliminar el reporte."
                        if (success) reportToDelete = null
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { reportToDelete = null }) { Text("Cancelar") } },
        )
    }

    if (showAccountDelete) AlertDialog(
        onDismissRequest = { showAccountDelete = false },
        title = { Text("Eliminar cuenta") },
        text = { Text("Tu cuenta y tu perfil de VozBarrial se eliminarán permanentemente.") },
        confirmButton = {
            TextButton(onClick = { showAccountDelete = false; onDeleteAccount() }) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { showAccountDelete = false }) { Text("Cancelar") } },
    )

    operationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { operationMessage = null },
            title = { Text("VozBarrial") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { operationMessage = null }) { Text("Aceptar") } },
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = VozBlueSurface, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = VozMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ReportCard(report: Reporte, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.title, Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold)
                Text(
                    report.status,
                    Modifier.background(VozBlueSurface, RoundedCornerShape(50.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    color = VozNavy,
                    fontSize = 10.sp,
                )
            }
            Text(report.category, color = VozGreen, fontSize = 12.sp)
            Text(report.description, color = VozMuted, fontSize = 13.sp)
            Text(report.place, color = VozMuted, fontSize = 11.sp)
            if (onEdit != null || onDelete != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (onEdit != null) TextButton(onClick = onEdit) { Text("Editar") }
                    if (onDelete != null) TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(12.dp)) {
        Text(message, Modifier.padding(18.dp), color = VozMuted)
    }
}

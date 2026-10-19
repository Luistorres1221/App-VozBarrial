package com.vozbarrial.features.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import java.util.concurrent.TimeUnit

private val RejectNavy = Color(0xFF09243A)
private val RejectBlue = Color(0xFFEAF1FF)
private val RejectRed = Color(0xFFC81924)

private val rejectionCriteria = listOf(
    "Información insuficiente o foto borrosa" to "No es legible el daño ni la ubicación exacta.",
    "Reporte duplicado" to "Ya existe un ticket abierto idéntico.",
    "Fuera de jurisdicción comunitaria" to "Corresponde a propiedad privada o autoridad externa.",
    "Contenido ofensivo o inapropiado" to "Infracción a pautas de convivencia cívica.",
    "Otro motivo puntual" to "Especificación manual libre."
)

@Composable
fun RejectionScreen(
    report: Report,
    onBack: () -> Unit,
    onConfirm: (String, String, Boolean, Boolean, (Boolean, String?) -> Unit) -> Unit,
    onRejected: () -> Unit,
) {
    var selectedReason by remember(report.id) { mutableStateOf(rejectionCriteria.first().first) }
    var explanation by remember(report.id) {
        mutableStateOf("Hola ${report.authorName.ifBlank { "vecino" }}, gracias por tu compromiso. La información enviada no permite verificar el reporte. Por favor, revisa la sugerencia y vuelve a enviarlo.")
    }
    var allowResubmission by remember { mutableStateOf(true) }
    var notifyByEmail by remember { mutableStateOf(true) }
    var showCriteria by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize().background(Color(0xFFF5F7FB))) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !submitting) { Icon(Icons.Default.ArrowBack, "Volver", tint = RejectNavy) }
            Column(Modifier.weight(1f)) {
                Text("Moderación", color = RejectNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("VozBarrial · Administración", color = Color(0xFF718198), fontSize = 14.sp)
            }
            Text("Perfil", color = Color(0xFF718198), fontSize = 14.sp)
        }

        Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Column {
                Text("Motivo del Rechazo", color = RejectNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Revisión de veracidad y normativas cívicas", color = Color(0xFF718198), fontSize = 14.sp)
            }
            RejectionReportSummary(report)

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("▤ Criterio de Inadmisibilidad *", color = RejectNavy, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Paso 1 de 2", color = RejectNavy, fontSize = 12.sp)
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showCriteria = !showCriteria },
                        color = RejectBlue, shape = RoundedCornerShape(9.dp)
                    ) {
                        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFD8E5FB)), contentAlignment = Alignment.Center) { Text("▧", color = RejectNavy, fontSize = 12.sp) }
                            Spacer(Modifier.width(7.dp))
                            Column(Modifier.weight(1f)) {
                                Text(selectedReason, color = RejectNavy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(rejectionCriteria.firstOrNull { it.first == selectedReason }?.second ?: "Describe el motivo del rechazo.", color = Color(0xFF536983), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text("Seleccionado", color = Color(0xFF45617F), fontSize = 11.sp)
                            Icon(Icons.Default.ExpandMore, null, tint = RejectNavy, modifier = Modifier.size(17.dp))
                        }
                    }
                    if (showCriteria) {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Color(0xFFF8FAFD)).padding(7.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("CRITERIOS DISPONIBLES", color = Color(0xFF718198), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            rejectionCriteria.forEach { (title, subtitle) ->
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(if (selectedReason == title) RejectBlue else Color.Transparent).clickable { selectedReason = title; showCriteria = false }.padding(7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (selectedReason == title) "◉" else "○", color = RejectNavy, fontSize = 12.sp)
                                    Spacer(Modifier.width(7.dp))
                                    Column {
                                        Text(title, color = RejectNavy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(subtitle, color = Color(0xFF718198), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp)) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ⓘ Explicación para el vecino *", color = RejectNavy, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${explanation.length} / 500", color = Color(0xFF718198), fontSize = 12.sp)
                    }
                    Text("Este texto queda asociado al rechazo como guía para corregir o encauzar el reporte.", color = Color(0xFF657991), fontSize = 12.sp, lineHeight = 16.sp)
                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { if (it.length <= 500) explanation = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 105.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 17.sp),
                        shape = RoundedCornerShape(9.dp)
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Sugerencias:", color = Color(0xFF657991), fontSize = 12.sp)
                        SuggestionChip("Pedir mejor foto") { explanation = (explanation + " Por favor, adjunta una foto más clara del problema.").take(500) }
                        SuggestionChip("Indicar canal municipal") { explanation = (explanation + " También puedes reportarlo por los canales oficiales del municipio.").take(500) }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(13.dp)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { allowResubmission = !allowResubmission }) {
                        Checkbox(checked = allowResubmission, onCheckedChange = { allowResubmission = it }, modifier = Modifier.size(30.dp))
                        Column(Modifier.padding(start = 5.dp)) {
                            Text("Permitir al ciudadano corregir y reenviar", color = RejectNavy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Se guardará la opción para permitir una corrección posterior.", color = Color(0xFF657991), fontSize = 12.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { notifyByEmail = !notifyByEmail }) {
                        Checkbox(checked = notifyByEmail, onCheckedChange = { notifyByEmail = it }, modifier = Modifier.size(30.dp))
                        Column(Modifier.padding(start = 5.dp)) {
                            Text("Marcar para notificación por correo electrónico", color = RejectNavy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("La preferencia queda guardada con el rechazo.", color = Color(0xFF657991), fontSize = 12.sp)
                        }
                    }
                }
            }
            error?.let { Text(it, color = RejectRed, fontSize = 14.sp) }
        }

        Button(
            onClick = {
                if (explanation.isBlank()) {
                    error = "Escribe una explicación para la persona que realizó el reporte."
                } else {
                    submitting = true
                    error = null
                    onConfirm(selectedReason, explanation.trim(), allowResubmission, notifyByEmail) { success, message ->
                        submitting = false
                        if (success) onRejected() else error = message ?: "No se pudo confirmar el rechazo."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp).height(46.dp),
            enabled = !submitting,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RejectRed)
        ) {
            if (submitting) CircularProgressIndicator(Modifier.size(17.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("⊘  Confirmar Rechazo", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RejectionReportSummary(report: Report) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!report.imageUrl.isNullOrBlank()) {
                AsyncImage(model = report.imageUrl.substringBefore(","), contentDescription = "Evidencia", modifier = Modifier.size(47.dp).clip(RoundedCornerShape(8.dp)))
            } else {
                Box(Modifier.size(47.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEAF1F8)), contentAlignment = Alignment.Center) { Text("📍", fontSize = 18.sp) }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▰ ${report.category.ifBlank { "Comunidad" }}", color = RejectNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(5.dp))
                    Text("${report.authorName.ifBlank { report.authorEmail.substringBefore("@") }}", color = Color(0xFF718198), fontSize = 12.sp)
                }
                Text(report.title, color = RejectNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF55759E), modifier = Modifier.size(11.dp))
                    Text(report.place, color = Color(0xFF657991), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("#REP-${report.id.takeLast(6).uppercase()}", color = RejectNavy, fontSize = 11.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(RejectBlue).padding(5.dp))
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color(0xFF718198), modifier = Modifier.size(10.dp))
                    Text(" ${elapsed(report.timestamp)}", color = Color(0xFF718198), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Text(label, color = Color(0xFF315B9B), fontSize = 11.sp, modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(RejectBlue).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 4.dp), maxLines = 1)
}

private fun elapsed(timestamp: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes((System.currentTimeMillis() - timestamp).coerceAtLeast(0))
    return if (minutes < 60) "$minutes min" else "${minutes / 60} h"
}

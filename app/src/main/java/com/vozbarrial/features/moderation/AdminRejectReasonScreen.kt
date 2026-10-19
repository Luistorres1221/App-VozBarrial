package com.vozbarrial.features.moderation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
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
import com.vozbarrial.domain.model.Report
import com.vozbarrial.features.map.formatTimeAgo
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.ui.theme.*

@Composable
fun AdminRejectReasonScreen(
    report: Report?,
    name: String,
    profilePhoto: String? = null,
    equippedFrame: String = "",
    onBack: () -> Unit,
    onConfirm: (reason: String, explanation: String, allowResubmit: Boolean) -> Unit
) {
    val criteriaOptions = listOf(
        "Información insuficiente o foto borrosa" to "No es legible el daño ni la ubicación exacta",
        "Reporte duplicado" to "Ya existe un ticket abierto idéntico",
        "Fuera de jurisdicción comunitaria" to "Corresponde a propiedad privada o autopista",
        "Contenido ofensivo o inapropiado" to "Infracción a pautas de convivencia cívica",
        "Otro motivo puntual" to "Especificación manual libre"
    )

    var selectedCriterion by rememberSaveable { mutableStateOf(criteriaOptions[0].first) }
    var expandedDropdown by rememberSaveable { mutableStateOf(false) }
    var explanation by rememberSaveable(report?.id) {
        mutableStateOf(
            if (report != null)
                "Hola ${report.authorName.substringBefore(' ')}, gracias por tu compromiso. La fotografía adjunta no permite verificar con claridad el detalle del incidente reportado. Por favor, toma una nueva foto con mayor visibilidad."
            else ""
        )
    }
    var allowResubmit by rememberSaveable { mutableStateOf(true) }

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
                    Text("Moderación", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

        Spacer(Modifier.height(12.dp))

        // Title Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Motivo del Rechazo", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Revisión de veracidad y normativas cívicas", color = VozMuted, fontSize = 11.sp)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE0F2FE)
            ) {
                Text(
                    text = if (report != null) "#REP-${report.id.takeLast(6).uppercase()}" else "#REP-2024-882",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color(0xFF0369A1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Report Summary Card
        if (report != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 1.dp
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
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFE0F2FE)) {
                                Text(report.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF0369A1), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("• ${report.authorName}", fontSize = 10.sp, color = VozMuted, maxLines = 1)
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(report.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VozNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = VozMuted, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Enviado ${formatTimeAgo(report.timestamp)}", fontSize = 10.sp, color = VozMuted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // Section 1: Criterio de Inadmisibilidad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Criterio de Inadmisibilidad *", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.width(4.dp))
                Text("Paso 1 de 2", color = VozMuted, fontSize = 10.sp)
            }

            // Selected criterion dropdown box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedDropdown = !expandedDropdown },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(selectedCriterion, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VozNavy)
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFE2E8F0)) {
                                Text("Seleccionado", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VozNavy)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        val desc = criteriaOptions.firstOrNull { it.first == selectedCriterion }?.second.orEmpty()
                        Text(desc, fontSize = 11.sp, color = VozMuted)
                    }
                    Icon(if (expandedDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = VozNavy)
                }
            }

            // Expanded Criteria List
            if (expandedDropdown) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("CRITERIOS DISPONIBLES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VozMuted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).align(Alignment.Start))
                        criteriaOptions.forEach { (title, subtitle) ->
                            val isSelected = selectedCriterion == title
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCriterion = title
                                        expandedDropdown = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFF1F5F9) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VozNavy)
                                        Text(subtitle, fontSize = 10.sp, color = VozMuted)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Section 2: Explicación para el vecino
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Explicación para el vecino *", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${explanation.length} / 500", color = VozMuted, fontSize = 10.sp)
            }
            Text("Este texto se enviará inmediatamente como mensaje orientativo para guiar al vecino sobre cómo corregir o encauzar su reporte.", color = VozMuted, fontSize = 10.sp, lineHeight = 14.sp)

            OutlinedTextField(
                value = explanation,
                onValueChange = { if (it.length <= 500) explanation = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = VozNavy),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFCBD5E1),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // Suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Sugerencias:", color = VozMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                SuggestionChip(
                    onClick = { explanation += " Por favor, toma una foto con mayor iluminación." },
                    label = { Text("+ Pedir mejor foto", fontSize = 10.sp) }
                )
                SuggestionChip(
                    onClick = { explanation += " Te recomendamos contactar al canal municipal correspondiente." },
                    label = { Text("+ Indicar canal municipal", fontSize = 10.sp) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Checkbox Option
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clickable { allowResubmit = !allowResubmit },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = allowResubmit,
                    onCheckedChange = { allowResubmit = it }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Permitir al ciudadano corregir y reenviar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VozNavy)
                    Text("El reporte pasará a estado 'Borrador editable' en su perfil vecinal.", fontSize = 10.sp, color = VozMuted)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onConfirm(selectedCriterion, explanation, allowResubmit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Icon(Icons.Default.Block, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirmar Rechazo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text("Cancelar y volver a la revisión", color = Color(0xFF45628A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

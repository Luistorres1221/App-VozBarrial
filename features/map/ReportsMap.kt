package com.vozbarrial.features.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MapNavy = Color(0xFF102B45)
private val MapGreen = Color(0xFF0B9B72)
private val MapMuted = Color(0xFF68788B)

private enum class ReportKind(val label: String, val color: Color) {
    SECURITY("Seguridad", Color(0xFFE34652)),
    ROAD("Vías y baches", Color(0xFFF08A28)),
    COMMUNITY("Comunidad", Color(0xFF159F78)),
}

private data class CommunityReport(
    val id: Int,
    val title: String,
    val description: String,
    val place: String,
    val kind: ReportKind,
    val x: Float,
    val y: Float,
)

@Composable
fun ReportsMap(name: String, onSignOut: () -> Unit, onNavigate: (String) -> Unit = {}, points: Int = 1450, submittedTitle: String? = null, submittedDescription: String? = null, submittedCategory: String? = null, submittedLatitude: Double? = null, submittedLongitude: Double? = null, onReportConsumed: () -> Unit = {}) {
    val reports = remember {
        mutableStateListOf(
            CommunityReport(1, "Alerta de seguridad", "Persona sospechosa reportada por vecinos.", "Parque Central", ReportKind.SECURITY, .43f, .27f),
            CommunityReport(2, "Bache en la vía", "Hueco profundo junto al cruce peatonal.", "Calle 12 con Carrera 8", ReportKind.ROAD, .68f, .43f),
            CommunityReport(3, "Luminaria apagada", "El poste lleva varias noches sin iluminación.", "Villa del Río", ReportKind.COMMUNITY, .27f, .53f),
            CommunityReport(4, "Daño en la calzada", "La vía está levantada y requiere mantenimiento.", "Parque Las Heras", ReportKind.ROAD, .57f, .66f),
            CommunityReport(5, "Jornada vecinal", "Punto de encuentro para la limpieza del parque.", "Plaza del Barrio", ReportKind.COMMUNITY, .34f, .79f),
        )
    }
    LaunchedEffect(submittedTitle, submittedLatitude, submittedLongitude) {
        if (!submittedTitle.isNullOrBlank()) {
            val kind = when { submittedCategory?.contains("seguridad", true) == true || submittedCategory?.contains("Emergencias", true) == true -> ReportKind.SECURITY; submittedCategory?.contains("vías", true) == true || submittedCategory?.contains("infraestructura", true) == true -> ReportKind.ROAD; else -> ReportKind.COMMUNITY }
            val id = (reports.maxOfOrNull { it.id } ?: 0) + 1
            val lat = submittedLatitude ?: 4.60971
            val lon = submittedLongitude ?: -74.08175
            val mapX = ((lon + 74.20) / .22).toFloat().coerceIn(.06f, .94f)
            val mapY = ((4.82 - lat) / .38).toFloat().coerceIn(.08f, .92f)
            val place = "Lat: " + "%.5f".format(java.util.Locale.US, lat) + ", Lon: " + "%.5f".format(java.util.Locale.US, lon)
            reports.add(CommunityReport(id, submittedTitle, submittedDescription.orEmpty(), place, kind, mapX, mapY))
            onReportConsumed()
        }
    }
    var selectedKind by rememberSaveable { mutableStateOf("TODOS") }
    var selectedReportId by rememberSaveable { mutableStateOf<Int?>(null) }
    var radarActive by rememberSaveable { mutableStateOf(true) }
    var distance by rememberSaveable { mutableStateOf("1.5 km") }
    var distanceMenu by remember { mutableStateOf(false) }
    var showCreateReport by rememberSaveable { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showPoints by rememberSaveable { mutableStateOf(false) }
    var zoom by remember { mutableStateOf(1f) }
    var mapPan by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        MapHeader(
            name = name,
            reportCount = reports.size,
            points = points,
            onPoints = { onNavigate("badges") },
            onNotifications = { onNavigate("activity") },
            onSignOut = onSignOut,
            onProfile = { onNavigate("profile") },
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Surface(
                    modifier = Modifier.clickable { distanceMenu = true },
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF0F3F8),
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MapNavy, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Distancia ($distance)⌄", fontSize = 10.sp, color = MapNavy, fontWeight = FontWeight.Medium)
                    }
                }
                DropdownMenu(expanded = distanceMenu, onDismissRequest = { distanceMenu = false }) {
                    listOf("500 m", "1.5 km", "3 km", "5 km").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { distance = option; distanceMenu = false },
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { radarActive = !radarActive }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (radarActive) MapGreen else Color.Gray))
                Spacer(Modifier.width(5.dp))
                Text(if (radarActive) "Radar Activo" else "Radar Pausado", color = if (radarActive) MapGreen else MapMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            InteractiveMap(
                reports = reports.toList(),
                selectedKind = selectedKind,
                selectedReportId = selectedReportId,
                zoom = zoom,
                mapPan = mapPan,
                onPanZoom = { newZoom, pan -> zoom = newZoom; mapPan = pan },
                onSelectReport = { selectedReportId = it },
                onRecenter = { zoom = 1f; mapPan = Offset.Zero },
            )

            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal = 9.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                MapFilter("Todos", reports.size, selectedKind == "TODOS", Modifier.weight(1f)) { selectedKind = "TODOS" }
                MapFilter("Seguridad", reports.count { it.kind == ReportKind.SECURITY }, selectedKind == "SECURITY", Modifier.weight(1f)) { selectedKind = "SECURITY" }
                MapFilter("Vías & Baches", reports.count { it.kind == ReportKind.ROAD }, selectedKind == "ROAD", Modifier.weight(1.2f)) { selectedKind = "ROAD" }
            }

            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 58.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MapControl(Icons.Default.Layers, "Capas", onClick = { selectedKind = "TODOS" })
                MapControl(Icons.Default.MyLocation, "Centrar mapa", onClick = { zoom = 1f; mapPan = Offset.Zero })
                MapControl(Icons.Default.Add, "Acercar", onClick = { zoom = (zoom + .2f).coerceAtMost(2f) })
                MapControl(Icons.Default.Remove, "Alejar", onClick = { zoom = (zoom - .2f).coerceAtLeast(.8f) })
            }

            selectedReportId?.let { id -> reports.firstOrNull { it.id == id }?.let { report ->
                SelectedReportCard(
                    report = report,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 76.dp),
                    onClose = { selectedReportId = null },
                )
            } }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp).size(58.dp).clickable { onNavigate("createReport") },
                shape = CircleShape,
                color = Color(0xFF071C2D),
                shadowElevation = 10.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Crear reporte", tint = Color.White, modifier = Modifier.size(29.dp))
                }
            }
        }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Notificaciones", color = MapNavy) },
            text = { Text("Tienes ${reports.count { it.kind == ReportKind.SECURITY }} alerta de seguridad en tu zona y ${reports.size} reportes comunitarios activos.") },
            confirmButton = { TextButton(onClick = { showNotifications = false }) { Text("Listo") } },
        )
    }
    if (showPoints) {
        AlertDialog(
            onDismissRequest = { showPoints = false },
            title = { Text("Mis puntos vecinales", color = MapNavy) },
            text = { Text("Tienes $points puntos. Puedes ganar más reportando situaciones y participando en tu comunidad.") },
            confirmButton = { TextButton(onClick = { showPoints = false }) { Text("Entendido") } },
        )
    }
}

@Composable
private fun MapHeader(
    name: String,
    reportCount: Int,
    points: Int,
    onPoints: () -> Unit,
    onNotifications: () -> Unit,
    onSignOut: () -> Unit,
    onProfile: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(start = 12.dp, end = 9.dp, top = 7.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(shape = CircleShape, color = Color(0xFFDCEBE7), modifier = Modifier.size(35.dp).clickable(onClick = onProfile)) {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase(), color = MapGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Column(modifier = Modifier.weight(1f).clickable(onClick = onProfile)) {
            Text("¡Hola, ${name.substringBefore('%').substringBefore(' ')}!", color = MapNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Nivel 3 · Guardián", color = MapGreen, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
        Surface(
            modifier = Modifier.clickable(onClick = onPoints),
            shape = RoundedCornerShape(50),
            color = Color(0xFFFFF2E7),
        ) {
            Text("🟠 $points", modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = Color(0xFF8A5723), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onNotifications, modifier = Modifier.size(34.dp)) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = MapNavy, modifier = Modifier.size(21.dp))
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFE35454)))
            }
        }
    }
}

@Composable
private fun MapFilter(text: String, count: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(32.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF08223A) else Color.White,
        shadowElevation = if (selected) 3.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 7.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (text == "Seguridad") Icon(Icons.Default.Warning, null, tint = if (selected) Color(0xFFFF6B66) else Color(0xFFE34C51), modifier = Modifier.size(12.dp))
            if (text == "Vías & Baches") Icon(Icons.Default.Build, null, tint = if (selected) Color(0xFFFFBD67) else Color(0xFFF19A42), modifier = Modifier.size(12.dp))
            if (text != "Todos") Spacer(Modifier.width(3.dp))
            Text("$text ($count)", color = if (selected) Color.White else MapNavy, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun InteractiveMap(
    reports: List<CommunityReport>,
    selectedKind: String,
    selectedReportId: Int?,
    zoom: Float,
    mapPan: Offset,
    onPanZoom: (Float, Offset) -> Unit,
    onSelectReport: (Int) -> Unit,
    onRecenter: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))) {
        val mapWidth = maxWidth
        val mapHeight = maxHeight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = zoom, scaleY = zoom, translationX = mapPan.x, translationY = mapPan.y)
                .pointerInput(zoom, mapPan) {
                    detectTransformGestures { _, pan, zoomChange, _ ->
                        onPanZoom((zoom * zoomChange).coerceIn(.8f, 2f), mapPan + pan)
                    }
                },
        ) {
            CityArtwork(Modifier.fillMaxSize())
            MapPlace("AEROPUERTO", .40f, .09f, mapWidth, mapHeight)
            MapPlace("Universidad", .11f, .21f, mapWidth, mapHeight, Color(0xFF718296))
            MapPlace("Parque Central", .12f, .40f, mapWidth, mapHeight, MapGreen)
            MapPlace("Easy Shopping", .57f, .37f, mapWidth, mapHeight, Color(0xFF3682C5))
            MapPlace("Parque Las Heras", .57f, .62f, mapWidth, mapHeight, MapGreen)
            MapPlace("Villa del Río", .13f, .69f, mapWidth, mapHeight, MapGreen)
            MapPlace("Centro", .57f, .84f, mapWidth, mapHeight, Color(0xFF718296))

            Box(
                modifier = Modifier.offset(x = mapWidth * .49f, y = mapHeight * .50f).size(22.dp).clip(CircleShape).background(Color.White).padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF3188E8)))
            }

            reports.filter { selectedKind == "TODOS" || it.kind.name == selectedKind }.forEach { report ->
                ReportPin(
                    report = report,
                    selected = selectedReportId == report.id,
                    modifier = Modifier.offset(x = mapWidth * report.x - 19.dp, y = mapHeight * report.y - 19.dp),
                    onClick = { onSelectReport(report.id) },
                )
            }
        }
    }
}

@Composable
private fun MapPlace(label: String, x: Float, y: Float, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, color: Color = Color(0xFF8A959F)) {
    Text(
        label,
        modifier = Modifier.offset(x = width * x, y = height * y),
        color = color,
        fontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
    )
}

@Composable
private fun CityArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color(0xFFE8EEF0))) {
        val w = size.width
        val h = size.height
        drawRect(Color(0xFFE8EEF0))
        for (row in 0..9) {
            for (column in 0..5) {
                val left = column * w / 5.5f + if (row % 2 == 0) 5f else 14f
                val top = row * h / 9.5f + 5f
                val blockWidth = w / 6.5f
                val blockHeight = h / 12f
                val color = if ((row + column) % 3 == 0) Color(0xFFE1E9E8) else Color(0xFFE6ECEB)
                drawRect(color, topLeft = Offset(left, top), size = androidx.compose.ui.geometry.Size(blockWidth, blockHeight))
            }
        }
        drawRect(Color(0xFFD2F0DA), topLeft = Offset(w * .04f, h * .37f), size = androidx.compose.ui.geometry.Size(w * .27f, h * .10f))
        drawRect(Color(0xFFD2F0DA), topLeft = Offset(w * .56f, h * .58f), size = androidx.compose.ui.geometry.Size(w * .24f, h * .11f))
        drawRect(Color(0xFFD7EFDE), topLeft = Offset(w * .17f, h * .72f), size = androidx.compose.ui.geometry.Size(w * .17f, h * .08f))
        val roadPaths = listOf(
            Path().apply { moveTo(-w * .1f, h * .12f); cubicTo(w * .20f, h * .22f, w * .26f, h * .40f, w * .55f, h * .47f); cubicTo(w * .78f, h * .53f, w * .82f, h * .71f, w * 1.1f, h * .78f) },
            Path().apply { moveTo(w * .53f, -h * .05f); cubicTo(w * .46f, h * .22f, w * .69f, h * .39f, w * .53f, h * .59f); cubicTo(w * .43f, h * .77f, w * .62f, h * .88f, w * .56f, h * 1.05f) },
            Path().apply { moveTo(-w * .05f, h * .34f); cubicTo(w * .30f, h * .31f, w * .66f, h * .39f, w * 1.05f, h * .30f) },
            Path().apply { moveTo(-w * .05f, h * .66f); cubicTo(w * .27f, h * .58f, w * .72f, h * .73f, w * 1.05f, h * .61f) },
            Path().apply { moveTo(-w * .05f, h * .87f); cubicTo(w * .32f, h * .92f, w * .66f, h * .80f, w * 1.05f, h * .89f) },
            Path().apply { moveTo(w * .14f, -h * .03f); cubicTo(w * .20f, h * .30f, w * .06f, h * .63f, w * .19f, h * 1.04f) },
            Path().apply { moveTo(w * .84f, -h * .04f); cubicTo(w * .78f, h * .28f, w * .98f, h * .51f, w * .81f, h * 1.04f) },
        )
        roadPaths.forEach { path ->
            drawPath(path, Color(0xFFFAFBFC), style = Stroke(width = 25f, cap = StrokeCap.Round))
            drawPath(path, Color(0xFFC6D0D5), style = Stroke(width = 17f, cap = StrokeCap.Round))
            drawPath(path, Color(0xFFE9EDEF), style = Stroke(width = 12f, cap = StrokeCap.Round))
        }
        drawLine(Color(0xFFB7E6DD), Offset(w * .05f, h * .52f), Offset(w * .94f, h * .48f), strokeWidth = 5f)
        for (index in 0..11) {
            val x = (index % 4) * w / 4f + w * .08f
            val y = (index / 4) * h / 3.5f + h * .14f
            drawCircle(Color(0xFFCCD8D7), radius = 2f, center = Offset(x, y))
        }
    }
}

@Composable
private fun ReportPin(report: CommunityReport, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val icon = when (report.kind) {
        ReportKind.SECURITY -> Icons.Default.Warning
        ReportKind.ROAD -> Icons.Default.Build
        ReportKind.COMMUNITY -> Icons.Default.Groups
    }
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(if (selected) 39.dp else 34.dp).border(2.dp, Color.White, CircleShape),
            shape = CircleShape,
            color = report.kind.color,
            shadowElevation = 5.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = report.title, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Surface(color = MapNavy.copy(alpha = .9f), shape = RoundedCornerShape(5.dp)) {
            Text(report.place, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), color = Color.White, fontSize = 7.sp, maxLines = 1)
        }
    }
}

@Composable
private fun MapControl(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(38.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = MapNavy, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun SelectedReportCard(report: CommunityReport, modifier: Modifier, onClose: () -> Unit) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 9.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(report.kind.color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(
                    when (report.kind) { ReportKind.SECURITY -> Icons.Default.Warning; ReportKind.ROAD -> Icons.Default.Build; ReportKind.COMMUNITY -> Icons.Default.Groups },
                    null,
                    tint = report.kind.color,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(report.title, color = MapNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${report.kind.label} · ${report.place}", color = MapMuted, fontSize = 9.sp)
                Text(report.description, color = MapNavy, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar detalle", tint = MapMuted, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun CreateReportDialog(onDismiss: () -> Unit, onCreate: (String, String, ReportKind) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf(ReportKind.SECURITY) }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear reporte", color = MapNavy, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Ayuda a tu comunidad compartiendo una situación cercana.", color = MapMuted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReportKind.values().forEach { option ->
                        FilterChip(
                            selected = kind == option,
                            onClick = { kind = option },
                            label = { Text(option.label, fontSize = 9.sp) },
                        )
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it; error = "" }, label = { Text("Título") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it; error = "" }, label = { Text("Descripción") }, minLines = 2)
                if (error.isNotBlank()) Text(error, color = Color(0xFFB42318), fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) error = "Completa el título y la descripción."
                    else onCreate(title.trim(), description.trim(), kind)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MapNavy),
            ) { Text("Publicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
package com.vozbarrial.features.moderation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.ReportStatus
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.concurrent.TimeUnit

private val Navy = Color(0xFF0B2740)
private val Pale = Color(0xFFF4F7FC)

@Composable
fun AdminMapScreen(
    name: String,
    reports: List<Report>,
    onVerification: () -> Unit,
    onResolution: () -> Unit,
    onOpenReport: (String) -> Unit,
) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("Todos") }
    var selectedReport by remember { mutableStateOf<Report?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        locationPermissionGranted = permissions.values.any { it }
    }
    val locationManager = remember { context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager }
    val categories = listOf("Todos", "Seguridad", "Vías & Baches")
    val visibleReports = reports.filter { report ->
        category == "Todos" || report.category.contains(if (category == "Vías & Baches") "infraestructura" else category, true)
    }
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", android.content.Context.MODE_PRIVATE))
        true
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    DisposableEffect(locationPermissionGranted) {
        if (!locationPermissionGranted) return@DisposableEffect onDispose { }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val point = GeoPoint(location.latitude, location.longitude)
                currentLocation = point
                mapView?.controller?.animateTo(point)
            }
            @Deprecated("Deprecated in Android")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        try {
            val providers = locationManager.getProviders(true)
            providers.forEach { provider ->
                @Suppress("MissingPermission")
                locationManager.getLastKnownLocation(provider)?.let { location ->
                    val point = GeoPoint(location.latitude, location.longitude)
                    if (currentLocation == null) currentLocation = point
                }
                @Suppress("MissingPermission")
                locationManager.requestLocationUpdates(provider, 10_000L, 25f, listener, context.mainLooper)
            }
            currentLocation?.let { mapView?.controller?.animateTo(it) }
        } catch (_: SecurityException) {
            locationPermissionGranted = false
        }
        onDispose { locationManager.removeUpdates(listener) }
    }
    LaunchedEffect(currentLocation) {
        currentLocation?.let { mapView?.controller?.animateTo(it) }
    }

    Box(Modifier.fillMaxSize().background(Pale)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(13.5)
                    controller.setCenter(currentLocation ?: GeoPoint(4.5339, -75.6811))
                }
            },
            update = { map ->
                map.overlays.removeAll { it is Marker }
                visibleReports.filter { it.latitude != 0.0 || it.longitude != 0.0 }.forEach { report ->
                    Marker(map).apply {
                        position = GeoPoint(report.latitude, report.longitude)
                        title = report.title
                        snippet = "${report.category} · ${ReportStatus.fromString(report.status).label}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { marker, _ ->
                            selectedReport = report
                            marker.showInfoWindow()
                            true
                        }
                    }.also(map.overlays::add)
                }
                currentLocation?.let { point ->
                    Marker(map).apply {
                        position = point
                        title = "Tu ubicación actual"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }.also(map.overlays::add)
                }
                map.invalidate()
            }
        )

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(37.dp).clip(CircleShape).background(Color(0xFFE8EFF8)), contentAlignment = Alignment.Center) {
                        Text(name.firstOrNull()?.uppercase() ?: "A", color = Navy, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("¡Hola, ${name.ifBlank { "Administrador" }}!", color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("● Moderador", color = Color(0xFF00845A), fontSize = 10.sp)
                    }
                    Text("● Radar activo", color = Color(0xFF00845A), fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 3.dp) {
                    Text("●  Distancia (1.5 km)  ⌄", color = Navy, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item, fontSize = 10.sp, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Navy, selectedLabelColor = Color.White),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        IconButton(
            onClick = {
                if (!locationPermissionGranted) {
                    locationPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                } else {
                    val point = currentLocation
                    if (point != null) mapView?.controller?.animateTo(point)
                    else mapView?.controller?.setCenter(GeoPoint(4.5339, -75.6811))
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).offset(y = (-12).dp).background(Color.White, CircleShape)
        ) { Icon(Icons.Default.MyLocation, "Centrar mapa", tint = Navy) }

        selectedReport?.let { report ->
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 150.dp).fillMaxWidth().clickable { onOpenReport(report.id) },
                shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(report.category.uppercase(), color = Color(0xFF45617F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(report.title, color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(report.place, color = Color(0xFF718198), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Toca para ver el reporte", color = Navy, fontSize = 10.sp)
                }
            }
        }

        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.White.copy(alpha = 0.97f)).padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pendingCount = reports.count { ReportStatus.fromString(it.status) == ReportStatus.POR_VERIFICAR }
            Button(onClick = onVerification, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5FB))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✿  Verificación", color = Navy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("$pendingCount pendientes", color = Color(0xFF00845A), fontSize = 9.sp)
                }
            }
            Button(onClick = onResolution, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("◉  Resolución", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Solución vecinal", color = Color(0xFFD5E5F4), fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun ModerationScreen(
    reports: List<Report>,
    initialTab: String = "Pendientes",
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenReport: (String) -> Unit,
    onModerate: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onStartRejection: (String) -> Unit = {},
    onApproved: () -> Unit = {},
) {
    var tab by remember(initialTab) { mutableStateOf(initialTab) }
    var category by remember { mutableStateOf("Todos") }
    var busyReport by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val pending = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.POR_VERIFICAR }
    val approved = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.VERIFICADO }
    val rejected = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.RECHAZADO }
    val resolved = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.RESUELTO }
    val tabReports = when (tab) {
        "Aprobados" -> approved
        "Rechazos" -> rejected
        "Resolución" -> resolved
        else -> pending
    }
    val categories = listOf("Todos") + tabReports.map { it.category.trim().ifBlank { "Otros" } }.distinct()
    val filtered = if (category == "Todos") tabReports else tabReports.filter { it.category.equals(category, true) }

    Column(Modifier.fillMaxSize().background(Pale)) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Navy) }
            Column(Modifier.weight(1f)) {
                Text("Moderación", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("VozBarrial · Administración", color = Color(0xFF718198), fontSize = 11.sp)
            }
            TextButton(onClick = onOpenMap) {
                Icon(Icons.Default.Map, null, tint = Navy, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp)); Text("Mapa", color = Navy)
            }
        }
        Row(Modifier.fillMaxWidth().background(Color.White), horizontalArrangement = Arrangement.SpaceEvenly) {
            ModerationTab("Pendientes", pending.size, tab == "Pendientes") { tab = "Pendientes" }
            ModerationTab("Aprobados", approved.size, tab == "Aprobados") { tab = "Aprobados" }
            ModerationTab("Rechazos", rejected.size, tab == "Rechazos") { tab = "Rechazos" }
            ModerationTab("Resolución", resolved.size, tab == "Resolución") { tab = "Resolución" }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { item ->
                FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item, maxLines = 1) })
            }
        }
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (tab == "Pendientes") "No hay reportes pendientes" else "No hay reportes en esta sección", color = Color(0xFF718198))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { report ->
                    ModerationReportCard(
                        report = report,
                        pending = tab == "Pendientes",
                        loading = busyReport == report.id,
                        onMap = onOpenMap,
                        onView = { onOpenReport(report.id) },
                        onApprove = {
                            busyReport = report.id
                            onModerate(report.id, ReportStatus.VERIFICADO.value) { ok, error ->
                                busyReport = null
                                message = if (ok) "Reporte aprobado" else error ?: "No se pudo aprobar el reporte"
                            if (ok) onApproved()
                            }
                        },
                        onReject = {
                            onStartRejection(report.id)
                        }
                    )
                }
            }
        }
    }
    message?.let { text ->
        AlertDialog(onDismissRequest = { message = null }, title = { Text("Moderación") }, text = { Text(text) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Aceptar") } })
    }
}

@Composable
fun ModerationMapScreen(reports: List<Report>, onBack: () -> Unit) {
    val context = LocalContext.current
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", android.content.Context.MODE_PRIVATE))
        true
    }
    Column(Modifier.fillMaxSize().background(Pale)) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Navy) }
            Text("Mapa de reportes", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(4.60971, -74.08175))
                }
            },
            update = { map ->
                map.overlays.removeAll { it is Marker }
                reports.filter { it.latitude != 0.0 || it.longitude != 0.0 }.forEach { report ->
                    Marker(map).apply {
                        position = GeoPoint(report.latitude, report.longitude)
                        title = report.title
                        snippet = "${report.category} · ${ReportStatus.fromString(report.status).label}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }.also(map.overlays::add)
                }
                map.invalidate()
            }
        )
    }
}

@Composable
private fun ModerationTab(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$label  $count", color = if (selected) Navy else Color(0xFF718198), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
        Spacer(Modifier.height(7.dp))
        Box(Modifier.width(76.dp).height(3.dp).clip(CircleShape).background(if (selected) Navy else Color.Transparent))
    }
}

@Composable
private fun ModerationReportCard(
    report: Report,
    pending: Boolean,
    loading: Boolean,
    onMap: () -> Unit,
    onView: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.category.ifBlank { "Comunidad" }.uppercase(), color = Navy, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFE8F0FF)).padding(horizontal = 8.dp, vertical = 4.dp))
                Spacer(Modifier.width(6.dp))
                Text("#${report.id.takeLast(8)}", color = Color(0xFF718198), fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text(timeAgo(report.timestamp), color = Color(0xFF718198), fontSize = 10.sp)
            }
            Text(report.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF55759E), modifier = Modifier.size(15.dp))
                Text(report.place.ifBlank { "Ubicación no especificada" }, color = Color(0xFF45617F), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (!report.imageUrl.isNullOrBlank()) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0F4FA)).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = report.imageUrl.substringBefore(","), contentDescription = "Evidencia del reporte", modifier = Modifier.size(54.dp).clip(RoundedCornerShape(7.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text(report.description, color = Color(0xFF36516D), fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Color(0xFFF0F4FA)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(27.dp).background(Color(0xFFDCE8F8), CircleShape), contentAlignment = Alignment.Center) { Text("${report.authorName.firstOrNull()?.uppercaseChar() ?: 'U'}", color = Navy, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(7.dp))
                Column(Modifier.weight(1f)) {
                    Text(report.authorName.ifBlank { report.authorEmail.substringBefore("@") }, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text("★ ${report.authorNivel} · ${report.votes} pts", color = Color(0xFF718198), fontSize = 9.sp)
                }
                TextButton(onClick = onMap, contentPadding = PaddingValues(horizontal = 6.dp)) { Icon(Icons.Default.Map, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(3.dp)); Text("Mapa", fontSize = 10.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onView, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp), enabled = !loading) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(3.dp)); Text("Ver info", fontSize = 10.sp, maxLines = 1)
                }
                if (pending) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp), enabled = !loading) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFFD83C45), modifier = Modifier.size(15.dp)); Spacer(Modifier.width(2.dp)); Text("Rechazar", color = Color(0xFFD83C45), fontSize = 10.sp, maxLines = 1)
                    }
                    Button(onClick = onApprove, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 4.dp), enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00845A))) {
                        if (loading) CircularProgressIndicator(Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Default.Check, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(2.dp)); Text("Aprobar", fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

private fun timeAgo(timestamp: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes((System.currentTimeMillis() - timestamp).coerceAtLeast(0))
    return if (minutes < 60) "$minutes min" else "${minutes / 60} h"
}

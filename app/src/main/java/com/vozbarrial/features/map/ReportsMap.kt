package com.vozbarrial.features.map

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import com.vozbarrial.ui.theme.*
import com.vozbarrial.domain.Reporte
import java.util.Locale

private enum class ReportKind(val label: String, val color: Color) {
    SECURITY("Seguridad", Color(0xFFE34652)),
    ROAD("Vías y baches", Color(0xFFF08A28)),
    COMMUNITY("Comunidad", Color(0xFF159F78)),
}

private data class CommunityReport(
    val id: String,
    val title: String,
    val description: String,
    val place: String,
    val kind: ReportKind,
    val latitude: Double,
    val longitude: Double,
)

@Composable
fun ReportsMap(name: String, profilePhoto: String? = null, onSignOut: () -> Unit, onNavigate: (String) -> Unit = {}, points: Int = 1450, communityReports: List<Reporte> = emptyList()) {
    val context = LocalContext.current
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        true
    }

    val reports = remember(communityReports) {
        communityReports.map { report ->
            val kind = when {
                report.category.contains("seguridad", true) || report.category.contains("Emergencias", true) -> ReportKind.SECURITY
                report.category.contains("vías", true) || report.category.contains("infraestructura", true) -> ReportKind.ROAD
                else -> ReportKind.COMMUNITY
            }
            CommunityReport(report.id, report.title, report.description, report.place, kind, report.latitude, report.longitude)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    var selectedKind by rememberSaveable { mutableStateOf("TODOS") }
    var selectedReportId by rememberSaveable { mutableStateOf<String?>(null) }
    var radarActive by rememberSaveable { mutableStateOf(true) }
    var distance by rememberSaveable { mutableStateOf("1.5 km") }
    var distanceMenu by remember { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showPoints by rememberSaveable { mutableStateOf(false) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(VozBackground)) {
        MapHeader(
            name = name,
            profilePhoto = profilePhoto,
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
                        Icon(Icons.Default.LocationOn, null, tint = VozNavy, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Distancia ($distance)⌄", fontSize = 10.sp, color = VozNavy, fontWeight = FontWeight.Medium)
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
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (radarActive) VozGreen else Color.Gray))
                Spacer(Modifier.width(5.dp))
                Text(if (radarActive) "Radar Activo" else "Radar Pausado", color = if (radarActive) VozGreen else VozMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(14.0)
                        controller.setCenter(GeoPoint(4.60971, -74.08175))

                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                            enableMyLocation()
                            enableFollowLocation()
                            val dotBitmap = android.graphics.Bitmap.createBitmap(48, 48, android.graphics.Bitmap.Config.ARGB_8888).apply {
                                val canvas = android.graphics.Canvas(this)
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#3188E8")
                                    isAntiAlias = true
                                }
                                canvas.drawCircle(24f, 24f, 22f, paint)
                                paint.color = android.graphics.Color.WHITE
                                canvas.drawCircle(24f, 24f, 10f, paint)
                            }
                            setPersonIcon(dotBitmap)
                            setDirectionIcon(dotBitmap)
                        }
                        overlays.add(locationOverlay)

                        mapViewInstance = this
                    }
                },
                update = { mapView ->
                    mapView.overlays.removeAll { it is Marker }
                    reports.filter { selectedKind == "TODOS" || it.kind.name == selectedKind }.forEach { report ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(report.latitude, report.longitude)
                            title = report.title
                            snippet = report.description
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { m, _ ->
                                selectedReportId = report.id
                                m.showInfoWindow()
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                }
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
                MapControl(Icons.Default.MyLocation, "Mi ubicación", onClick = {
                    val myLoc = (mapViewInstance?.overlays?.firstOrNull { it is MyLocationNewOverlay } as? MyLocationNewOverlay)?.myLocation
                    if (myLoc != null) {
                        mapViewInstance?.controller?.setCenter(myLoc)
                        mapViewInstance?.controller?.setZoom(16.0)
                    } else {
                        mapViewInstance?.controller?.setCenter(GeoPoint(4.60971, -74.08175))
                        mapViewInstance?.controller?.setZoom(14.0)
                    }
                })
                MapControl(Icons.Default.Add, "Acercar", onClick = {
                    mapViewInstance?.controller?.zoomIn()
                })
                MapControl(Icons.Default.Remove, "Alejar", onClick = {
                    mapViewInstance?.controller?.zoomOut()
                })
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
            title = { Text("Notificaciones", color = VozNavy) },
            text = { Text("Tienes ${reports.count { it.kind == ReportKind.SECURITY }} alerta de seguridad en tu zona y ${reports.size} reportes comunitarios activos.") },
            confirmButton = { TextButton(onClick = { showNotifications = false }) { Text("Listo") } },
        )
    }
    if (showPoints) {
        AlertDialog(
            onDismissRequest = { showPoints = false },
            title = { Text("Mis puntos vecinales", color = VozNavy) },
            text = { Text("Tienes $points puntos. Puedes ganar más reportando situaciones y participando en tu comunidad.") },
            confirmButton = { TextButton(onClick = { showPoints = false }) { Text("Entendido") } },
        )
    }
}

@Composable
private fun MapHeader(
    name: String,
    profilePhoto: String? = null,
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
                if (!profilePhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePhoto,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(name.take(1).uppercase(), color = VozGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        Column(modifier = Modifier.weight(1f).clickable(onClick = onProfile)) {
            Text("¡Hola, ${name.substringBefore('%').substringBefore(' ')}!", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Nivel 3 · Guardián", color = VozGreen, fontSize = 9.sp, fontWeight = FontWeight.Medium)
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
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = VozNavy, modifier = Modifier.size(21.dp))
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
            Text("$text ($count)", color = if (selected) Color.White else VozNavy, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun MapControl(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(38.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = VozNavy, modifier = Modifier.size(19.dp))
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
                Text(report.title, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${report.kind.label} · ${report.place}", color = VozMuted, fontSize = 9.sp)
                Text(report.description, color = VozNavy, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar detalle", tint = VozMuted, modifier = Modifier.size(17.dp))
            }
        }
    }
}

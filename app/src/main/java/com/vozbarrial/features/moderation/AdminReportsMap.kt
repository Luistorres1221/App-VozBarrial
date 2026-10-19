package com.vozbarrial.features.moderation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.vozbarrial.ui.components.UserAvatarWithFrame
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vozbarrial.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import com.vozbarrial.ui.theme.*
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.ReportStatus
import com.vozbarrial.features.map.CommunityReport
import com.vozbarrial.features.map.MapFilter
import com.vozbarrial.features.map.SelectedReportCard

@Composable
fun AdminReportsMap(
    name: String,
    profilePhoto: String? = null,
    equippedFrame: String = "",
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit = {},
    monedas: Int = 0,
    nivel: String = "novato",
    communityReports: List<Report> = emptyList(),
    onEarnRewards: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        true
    }

    val reports = remember(communityReports) {
        communityReports.map { report ->
            CommunityReport(
                report.id,
                report.title,
                report.description,
                report.place,
                report.category,
                report.imageUrl,
                report.timestamp,
                report.status,
                report.latitude,
                report.longitude,
                report.authorUid,
                report.authorEmail,
                report.authorNivel
            )
        }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
    }

    var selectedCategory by rememberSaveable { mutableStateOf("TODOS") }
    var selectedReportId by rememberSaveable { mutableStateOf<String?>(null) }
    var radarActive by rememberSaveable { mutableStateOf(true) }
    var distance by rememberSaveable { mutableStateOf("1.5 km") }
    var distanceMenu by remember { mutableStateOf(false) }
    var showPendingDialog by rememberSaveable { mutableStateOf(false) }
    var showResolutionDialog by rememberSaveable { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showPoints by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    var currentUserLocation by remember {
        mutableStateOf<GeoPoint?>(GeoPoint(4.60971, -74.08175))
    }

    val pendingCount = reports.count { ReportStatus.fromString(it.status) == ReportStatus.POR_VERIFICAR }

    val distanceFilteredReports = remember(reports, distance) {
        reports.filter { report ->
            val statusEnum = ReportStatus.fromString(report.status)
            if (statusEnum == ReportStatus.RECHAZADO || statusEnum == ReportStatus.ELIMINADO) return@filter false
            true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VozBackground)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(4.60971, -74.08175))

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this).apply {
                        enableMyLocation()
                        enableFollowLocation()
                    }
                    overlays.add(locationOverlay)
                    mapViewInstance = this
                }
            },
            update = { mapView ->
                val currentCategory = selectedCategory
                mapView.overlays.removeAll { it is Marker }

                distanceFilteredReports.filter { report ->
                    currentCategory == "TODOS" || when (currentCategory) {
                        "Seguridad" -> report.category.contains("seguridad", true)
                        "Emergencias Médicas" -> report.category.contains("emergencia", true)
                        "Infraestructura y Vías" -> report.category.contains("infraestructura", true) || report.category.contains("vías", true)
                        "Mascotas y Animales" -> report.category.contains("mascota", true) || report.category.contains("animal", true)
                        "Comunidad y Entorno" -> report.category.contains("comunidad", true) || report.category.contains("entorno", true)
                        "Alumbrado y Servicios" -> report.category.contains("alumbrado", true) || report.category.contains("servicio", true)
                        else -> report.category.equals(currentCategory, ignoreCase = true)
                    }
                }.forEach { report ->
                    val (iconRes, badgeColor) = when {
                        report.category.contains("alumbrado", true) || report.category.contains("servicios", true) -> R.drawable.ic_pin_alumbrado to android.graphics.Color.parseColor("#ca8a04")
                        report.category.contains("emergencia", true) -> R.drawable.ic_pin_emergencias to android.graphics.Color.parseColor("#dc2626")
                        report.category.contains("infraestructura", true) || report.category.contains("vías", true) -> R.drawable.ic_pin_infraestructura to android.graphics.Color.parseColor("#ea580c")
                        report.category.contains("mascota", true) -> R.drawable.ic_pin_mascotas to android.graphics.Color.parseColor("#9333ea")
                        report.category.contains("seguridad", true) -> R.drawable.ic_pin_security to android.graphics.Color.parseColor("#2563eb")
                        else -> R.drawable.ic_pin_comunidad to android.graphics.Color.parseColor("#16a34a")
                    }

                    val marker = Marker(mapView).apply {
                        position = GeoPoint(report.latitude, report.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        val size = 64
                        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)

                        val paint = Paint().apply {
                            color = badgeColor
                            isAntiAlias = true
                        }
                        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

                        val borderPaint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            style = Paint.Style.STROKE
                            strokeWidth = 3f
                            isAntiAlias = true
                        }
                        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)

                        val drawable = ContextCompat.getDrawable(mapView.context, iconRes)
                        if (drawable != null) {
                            val iconSize = 34
                            val left = (size - iconSize) / 2
                            val top = (size - iconSize) / 2
                            drawable.setBounds(left, top, left + iconSize, top + iconSize)
                            drawable.colorFilter =
                                PorterDuffColorFilter(android.graphics.Color.WHITE, PorterDuff.Mode.SRC_IN)
                            drawable.draw(canvas)
                        }

                        icon = BitmapDrawable(mapView.resources, bitmap)

                        setOnMarkerClickListener { _, _ ->
                            selectedReportId = report.id
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            }
        )

        // Top Header and Filters
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(2f)
        ) {
            AdminMapHeader(
                name = name,
                profilePhoto = profilePhoto,
                equippedFrame = equippedFrame,
                monedas = monedas,
                onPoints = { showPoints = true },
                onNotifications = { showNotifications = true },
                onSignOut = onSignOut,
                onProfile = { onNavigate("profile") }
            )
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 13.dp, vertical = 5.dp),
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
                            Text("Distancia ($distance)", fontSize = 10.sp, color = VozNavy, fontWeight = FontWeight.Medium)
                        }
                    }
                    DropdownMenu(expanded = distanceMenu, onDismissRequest = { distanceMenu = false }) {
                        listOf("500 m", "1.5 km", "3 km", "5 km", "Todos").forEach { option ->
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
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 118.dp, start = 9.dp, end = 9.dp)
                .horizontalScroll(rememberScrollState())
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            MapFilter(
                text = "Todos",
                count = distanceFilteredReports.size,
                selected = selectedCategory == "TODOS",
                iconVector = Icons.Default.Layers,
                iconTint = Color(0xFF3188E8),
                onClick = { selectedCategory = "TODOS" }
            )
            MapFilter(
                text = "Seguridad",
                count = distanceFilteredReports.count { it.category.contains("seguridad", true) },
                selected = selectedCategory == "Seguridad",
                iconRes = R.drawable.ic_pin_security,
                iconTint = Color(0xFF2563EB),
                onClick = {
                    selectedCategory = if (selectedCategory == "Seguridad") "TODOS" else "Seguridad"
                }
            )
            MapFilter(
                text = "Emergencias",
                count = distanceFilteredReports.count { it.category.contains("emergencia", true) },
                selected = selectedCategory == "Emergencias Médicas",
                iconRes = R.drawable.ic_pin_emergencias,
                iconTint = Color(0xFFDC2626),
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Emergencias Médicas") "TODOS" else "Emergencias Médicas"
                }
            )
            MapFilter(
                text = "Infraestructura",
                count = distanceFilteredReports.count {
                    it.category.contains(
                        "infraestructura",
                        true
                    ) || it.category.contains("vías", true)
                },
                selected = selectedCategory == "Infraestructura y Vías",
                iconRes = R.drawable.ic_pin_infraestructura,
                iconTint = Color(0xFFEA580C),
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Infraestructura y Vías") "TODOS" else "Infraestructura y Vías"
                }
            )
            MapFilter(
                text = "Mascotas",
                count = distanceFilteredReports.count {
                    it.category.contains(
                        "mascota",
                        true
                    ) || it.category.contains("animal", true)
                },
                selected = selectedCategory == "Mascotas y Animales",
                iconRes = R.drawable.ic_pin_mascotas,
                iconTint = Color(0xFF9333EA),
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Mascotas y Animales") "TODOS" else "Mascotas y Animales"
                }
            )
            MapFilter(
                text = "Comunidad",
                count = distanceFilteredReports.count {
                    it.category.contains(
                        "comunidad",
                        true
                    ) || it.category.contains("entorno", true)
                },
                selected = selectedCategory == "Comunidad y Entorno",
                iconRes = R.drawable.ic_pin_comunidad,
                iconTint = Color(0xFF16A34A),
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Comunidad y Entorno") "TODOS" else "Comunidad y Entorno"
                }
            )
            MapFilter(
                text = "Alumbrado",
                count = distanceFilteredReports.count {
                    it.category.contains(
                        "alumbrado",
                        true
                    ) || it.category.contains("servicio", true)
                },
                selected = selectedCategory == "Alumbrado y Servicios",
                iconRes = R.drawable.ic_pin_alumbrado,
                iconTint = Color(0xFFCA8A04),
                onClick = {
                    selectedCategory =
                        if (selectedCategory == "Alumbrado y Servicios") "TODOS" else "Alumbrado y Servicios"
                }
            )
        }

        // Selected Report Card if any
        selectedReportId?.let { id -> reports.firstOrNull { it.id == id }?.let { report ->
            SelectedReportCard(
                report = report,
                myLoc = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 90.dp)
                    .zIndex(2f),
                onClose = { selectedReportId = null },
                onViewReport = { onNavigate("reportDetail/${report.id}") },
                onEarnRewards = onEarnRewards
            )
        } }

        // Bottom Admin Action Bar (Verificación & Resolución cards matching screenshot)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(2f),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verificación Button Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { onNavigate("adminModeration") },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Verificación", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("$pendingCount pendientes", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Resolución Button Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { onNavigate("adminResolution") },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF071C2D),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF1D4ED8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Resolución", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Solución vecinal", color = Color(0xFF93C5FD), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    if (showPendingDialog) {
        AlertDialog(
            onDismissRequest = { showPendingDialog = false },
            title = { Text("Reportes Pendientes de Verificación", color = VozNavy, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pendingList = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.POR_VERIFICAR }
                    if (pendingList.isEmpty()) {
                        Text("No hay reportes pendientes de verificación.", color = VozMuted, fontSize = 12.sp)
                    } else {
                        Text("Hay ${pendingList.size} reportes por verificar en la comunidad:", fontSize = 11.sp, color = VozNavy)
                        pendingList.take(5).forEach { r ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    showPendingDialog = false
                                    onNavigate("reportDetail/${r.id}")
                                },
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(r.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VozNavy)
                                    Text(r.place, fontSize = 10.sp, color = VozMuted)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPendingDialog = false }) { Text("Cerrar") } }
        )
    }

    if (showResolutionDialog) {
        AlertDialog(
            onDismissRequest = { showResolutionDialog = false },
            title = { Text("Resolución y Soluciones Vecinales", color = VozNavy, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val resolvedList = reports.filter { ReportStatus.fromString(it.status) == ReportStatus.RESUELTO || ReportStatus.fromString(it.status) == ReportStatus.VERIFICADO }
                    Text("Gestión de soluciones vecinales y reportes resueltos (${resolvedList.size} totales).", fontSize = 11.sp, color = VozNavy)
                }
            },
            confirmButton = { TextButton(onClick = { showResolutionDialog = false }) { Text("Cerrar") } }
        )
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Notificaciones de Administrador", color = VozNavy) },
            text = { Text("Tienes $pendingCount reportes pendientes de revisión y verificación oficial.") },
            confirmButton = { TextButton(onClick = { showNotifications = false }) { Text("Entendido") } }
        )
    }

    if (showPoints) {
        AlertDialog(
            onDismissRequest = { showPoints = false },
            title = { Text("Panel de Moderación", color = VozNavy) },
            text = { Text("Como Administrador / Moderador, supervisas la seguridad y veracidad de los reportes en VozBarrial.") },
            confirmButton = { TextButton(onClick = { showPoints = false }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun AdminMapHeader(
    name: String,
    profilePhoto: String? = null,
    equippedFrame: String = "",
    monedas: Int,
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
        UserAvatarWithFrame(
            profilePhoto = profilePhoto,
            name = name,
            frameName = equippedFrame,
            size = 35.dp,
            modifier = Modifier.clickable(onClick = onProfile)
        )
        Column(modifier = Modifier.weight(1f).clickable(onClick = onProfile)) {
            Text("¡Hola, ${name.substringBefore('%').substringBefore(' ')}!", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFD6FAEA)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF087A55)))
                        Spacer(Modifier.width(3.dp))
                        Text("Moderador", color = Color(0xFF087A55), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.clickable(onClick = onPoints),
            shape = RoundedCornerShape(50),
            color = Color(0xFFFFF2E7),
        ) {
            Text("🟠 $monedas", modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = Color(0xFF8A5723), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onNotifications, modifier = Modifier.size(34.dp)) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = VozNavy, modifier = Modifier.size(21.dp))
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFE35454)))
            }
        }
    }
}

package com.vozbarrial.features.map

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.vozbarrial.domain.model.Report
import java.util.Locale

private data class CommunityReport(
    val id: String,
    val title: String,
    val description: String,
    val place: String,
    val category: String,
    val imageUrl: String?,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
)

@Composable
fun ReportsMap(name: String, profilePhoto: String? = null, onSignOut: () -> Unit, onNavigate: (String) -> Unit = {}, points: Int = 1450, communityReports: List<Report> = emptyList()) {
    val context = LocalContext.current
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        true
    }

    val reports = remember(communityReports) {
        communityReports.map { report ->
            CommunityReport(report.id, report.title, report.description, report.place, report.category, report.imageUrl, report.timestamp, report.latitude, report.longitude)
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

    var selectedCategory by rememberSaveable { mutableStateOf("TODOS") }
    var selectedReportId by rememberSaveable { mutableStateOf<String?>(null) }
    var radarActive by rememberSaveable { mutableStateOf(true) }
    var distance by rememberSaveable { mutableStateOf("1.5 km") }
    var distanceMenu by remember { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showPoints by rememberSaveable { mutableStateOf(false) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var currentUserLocation by remember { mutableStateOf<GeoPoint?>(null) }

    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        val listener = android.location.LocationListener { location ->
            currentUserLocation = GeoPoint(location.latitude, location.longitude)
        }
        try {
            locationManager?.requestLocationUpdates(
                android.location.LocationManager.GPS_PROVIDER,
                2000L,
                2f,
                listener
            )
            locationManager?.requestLocationUpdates(
                android.location.LocationManager.NETWORK_PROVIDER,
                2000L,
                2f,
                listener
            )
        } catch (_: SecurityException) {}

        onDispose {
            try {
                locationManager?.removeUpdates(listener)
            } catch (_: SecurityException) {}
        }
    }

    val myLoc = currentUserLocation ?: (mapViewInstance?.overlays?.firstOrNull { it is MyLocationNewOverlay } as? MyLocationNewOverlay)?.myLocation
    val maxMeters = when (distance) {
        "500 m" -> 500.0
        "1.5 km" -> 1500.0
        "3 km" -> 3000.0
        "5 km" -> 5000.0
        else -> Double.MAX_VALUE
    }

    val distanceFilteredReports = remember(reports, distance, currentUserLocation, myLoc) {
        reports.filter { report ->
            if (myLoc != null && maxMeters < Double.MAX_VALUE) {
                val reportPoint = GeoPoint(report.latitude, report.longitude)
                val distanceMeters = myLoc.distanceToAsDouble(reportPoint)
                distanceMeters <= maxMeters
            } else {
                true
            }
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

                    val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                    val lastKnown = try {
                        locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    } catch (_: SecurityException) {
                        null
                    } catch (_: Exception) {
                        null
                    }

                    if (lastKnown != null) {
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(lastKnown.latitude, lastKnown.longitude))
                    } else {
                        controller.setZoom(14.0)
                        controller.setCenter(GeoPoint(4.60971, -74.08175))
                    }

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
                        report.category.contains("alumbrado", true) || report.category.contains("servicios", true) -> com.vozbarrial.R.drawable.ic_pin_alumbrado to android.graphics.Color.parseColor("#ca8a04")
                        report.category.contains("emergencia", true) -> com.vozbarrial.R.drawable.ic_pin_emergencias to android.graphics.Color.parseColor("#dc2626")
                        report.category.contains("infraestructura", true) || report.category.contains("vías", true) -> com.vozbarrial.R.drawable.ic_pin_infraestructura to android.graphics.Color.parseColor("#ea580c")
                        report.category.contains("mascota", true) -> com.vozbarrial.R.drawable.ic_pin_mascotas to android.graphics.Color.parseColor("#9333ea")
                        report.category.contains("seguridad", true) -> com.vozbarrial.R.drawable.ic_pin_security to android.graphics.Color.parseColor("#2563eb")
                        else -> com.vozbarrial.R.drawable.ic_pin_comunidad to android.graphics.Color.parseColor("#16a34a")
                    }

                    val marker = Marker(mapView).apply {
                        position = GeoPoint(report.latitude, report.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        val size = 64
                        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)

                        val paint = android.graphics.Paint().apply {
                            color = badgeColor
                            isAntiAlias = true
                        }
                        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

                        val borderPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 3f
                            isAntiAlias = true
                        }
                        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)

                        val drawable = androidx.core.content.ContextCompat.getDrawable(mapView.context, iconRes)
                        if (drawable != null) {
                            val iconSize = 34
                            val left = (size - iconSize) / 2
                            val top = (size - iconSize) / 2
                            drawable.setBounds(left, top, left + iconSize, top + iconSize)
                            drawable.colorFilter = android.graphics.PorterDuffColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
                            drawable.draw(canvas)
                        }

                        icon = android.graphics.drawable.BitmapDrawable(mapView.resources, bitmap)

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

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).zIndex(2f)
        ) {
            MapHeader(
                name = name,
                profilePhoto = profilePhoto,
                reportCount = distanceFilteredReports.size,
                points = points,
                onPoints = { onNavigate("badges") },
                onNotifications = { onNavigate("activity") },
                onSignOut = onSignOut,
                onProfile = { onNavigate("profile") },
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
                iconRes = com.vozbarrial.R.drawable.ic_pin_security,
                iconTint = Color(0xFF2563EB),
                onClick = { selectedCategory = if (selectedCategory == "Seguridad") "TODOS" else "Seguridad" }
            )
            MapFilter(
                text = "Emergencias",
                count = distanceFilteredReports.count { it.category.contains("emergencia", true) },
                selected = selectedCategory == "Emergencias Médicas",
                iconRes = com.vozbarrial.R.drawable.ic_pin_emergencias,
                iconTint = Color(0xFFDC2626),
                onClick = { selectedCategory = if (selectedCategory == "Emergencias Médicas") "TODOS" else "Emergencias Médicas" }
            )
            MapFilter(
                text = "Infraestructura",
                count = distanceFilteredReports.count { it.category.contains("infraestructura", true) || it.category.contains("vías", true) },
                selected = selectedCategory == "Infraestructura y Vías",
                iconRes = com.vozbarrial.R.drawable.ic_pin_infraestructura,
                iconTint = Color(0xFFEA580C),
                onClick = { selectedCategory = if (selectedCategory == "Infraestructura y Vías") "TODOS" else "Infraestructura y Vías" }
            )
            MapFilter(
                text = "Mascotas",
                count = distanceFilteredReports.count { it.category.contains("mascota", true) || it.category.contains("animal", true) },
                selected = selectedCategory == "Mascotas y Animales",
                iconRes = com.vozbarrial.R.drawable.ic_pin_mascotas,
                iconTint = Color(0xFF9333EA),
                onClick = { selectedCategory = if (selectedCategory == "Mascotas y Animales") "TODOS" else "Mascotas y Animales" }
            )
            MapFilter(
                text = "Comunidad",
                count = distanceFilteredReports.count { it.category.contains("comunidad", true) || it.category.contains("entorno", true) },
                selected = selectedCategory == "Comunidad y Entorno",
                iconRes = com.vozbarrial.R.drawable.ic_pin_comunidad,
                iconTint = Color(0xFF16A34A),
                onClick = { selectedCategory = if (selectedCategory == "Comunidad y Entorno") "TODOS" else "Comunidad y Entorno" }
            )
            MapFilter(
                text = "Alumbrado",
                count = distanceFilteredReports.count { it.category.contains("alumbrado", true) || it.category.contains("servicio", true) },
                selected = selectedCategory == "Alumbrado y Servicios",
                iconRes = com.vozbarrial.R.drawable.ic_pin_alumbrado,
                iconTint = Color(0xFFCA8A04),
                onClick = { selectedCategory = if (selectedCategory == "Alumbrado y Servicios") "TODOS" else "Alumbrado y Servicios" }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 165.dp, end = 10.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
        }

        selectedReportId?.let { id -> reports.firstOrNull { it.id == id }?.let { report ->
            SelectedReportCard(
                report = report,
                myLoc = myLoc,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, end = 12.dp, bottom = 76.dp)
                    .zIndex(2f),
                onClose = { selectedReportId = null },
                onViewReport = { onNavigate("reportDetail/${report.id}") },
                onSupport = { selectedReportId = null },
            )
        } }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 13.dp)
                .size(58.dp)
                .clickable { onNavigate("createReport") }
                .zIndex(2f),
            shape = CircleShape,
            color = Color(0xFF071C2D),
            shadowElevation = 10.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = "Crear reporte", tint = Color.White, modifier = Modifier.size(29.dp))
            }
        }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Notificaciones", color = VozNavy) },
            text = { Text("Tienes ${distanceFilteredReports.count { it.category.contains("seguridad", true) }} alerta de seguridad en tu zona y ${distanceFilteredReports.size} reportes comunitarios activos.") },
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
private fun MapFilter(
    text: String,
    count: Int,
    selected: Boolean,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    iconTint: Color = VozNavy,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.height(32.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF08223A) else Color.White,
        shadowElevation = if (selected) 3.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val resolvedTint = if (selected) Color.White else iconTint
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    tint = resolvedTint,
                    modifier = Modifier.size(14.dp)
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = text,
                    tint = resolvedTint,
                    modifier = Modifier.size(14.dp)
                )
            }
            if (text != "Todos") Spacer(Modifier.width(4.dp))
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
private fun SelectedReportCard(
    report: CommunityReport,
    myLoc: GeoPoint?,
    modifier: Modifier,
    onClose: () -> Unit,
    onViewReport: () -> Unit,
    onSupport: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val badgeColor = when {
                    report.category.contains("alumbrado", true) || report.category.contains("servicios", true) -> Color(0xFFCA8A04)
                    report.category.contains("emergencia", true) -> Color(0xFFDC2626)
                    report.category.contains("infraestructura", true) || report.category.contains("vías", true) -> Color(0xFFEA580C)
                    report.category.contains("mascota", true) -> Color(0xFF9333EA)
                    report.category.contains("seguridad", true) -> Color(0xFFE34652)
                    else -> Color(0xFF159F78)
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(badgeColor))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = report.category,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (myLoc != null) {
                        val reportPoint = GeoPoint(report.latitude, report.longitude)
                        val distMeters = myLoc.distanceToAsDouble(reportPoint)
                        val distText = if (distMeters < 1000) "${distMeters.toInt()} m" else String.format(Locale.US, "%.1f km", distMeters / 1000.0)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFFE34652), modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("A $distText", color = Color(0xFFE34652), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = VozNavy, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title,
                        color = VozNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = report.description,
                        color = VozMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!report.imageUrl.isNullOrBlank()) {
                    Spacer(Modifier.width(12.dp))
                    val firstImage = report.imageUrl.split(",").firstOrNull()?.trim()
                    AsyncImage(
                        model = firstImage,
                        contentDescription = "Foto del reporte",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEAEFF5), thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, null, tint = VozMuted, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                val timeAgo = "Hace 15 min"
                Text(timeAgo, color = VozMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onViewReport,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF1F8))
                ) {
                    Icon(Icons.Default.Visibility, null, tint = VozNavy, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver reporte", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSupport,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF071C2D))
                ) {
                    Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("¡Apoyado! ✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

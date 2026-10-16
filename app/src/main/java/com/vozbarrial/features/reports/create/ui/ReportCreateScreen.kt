package com.vozbarrial.features.reports.create.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vozbarrial.features.reports.create.presentation.ReportCreateUiState
import com.vozbarrial.features.reports.create.presentation.ReportCreateViewModel
import com.vozbarrial.ui.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

private data class ReportCategory(val name: String, val description: String, val icon: String)

private val reportCategories = listOf(
    ReportCategory("Seguridad", "Robos, vandalismo, actividades sospechosas", "🛡"),
    ReportCategory("Emergencias Médicas", "Accidentes viales, primeros auxilios", "✚"),
    ReportCategory("Infraestructura y Vías", "Baches, daños en calles, pavimentos rotos", "🚧"),
    ReportCategory("Mascotas y Animales", "Animales perdidos, encontrados o en peligro", "🐾"),
    ReportCategory("Comunidad y Entorno", "Basuras, polución o ruido excesivo", "♧"),
    ReportCategory("Alumbrado y Servicios", "Postes sin luz, fugas de agua o gas", "💡"),
)

@Composable
fun ReportCreateRoute(
    name: String,
    onBack: () -> Unit,
    onPublish: (String, String, String, Double, Double, List<Uri>, (Boolean, String?) -> Unit) -> Unit,
    viewModel: ReportCreateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.addPhoto(uri?.toString())
    }

    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) {
            requestReportLocation(context) { loc ->
                viewModel.updateGpsLocation(loc.latitude, loc.longitude)
            }
        } else {
            viewModel.denyLocationPermission()
        }
    }

    ReportCreateContent(
        uiState = uiState,
        onBack = {
            viewModel.reset()
            onBack()
        },
        onSelectCategory = viewModel::selectCategory,
        onUpdateTitle = viewModel::updateTitle,
        onUpdateDescription = viewModel::updateDescription,
        onPickPhoto = { picker.launch("image/*") },
        onRemovePhoto = viewModel::removePhoto,
        onConfirmMapLocation = viewModel::confirmMapLocation,
        onRequestGps = {
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fine || coarse) {
                requestReportLocation(context) { loc ->
                    viewModel.updateGpsLocation(loc.latitude, loc.longitude)
                }
            } else {
                locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
            }
        },
        onPublish = { viewModel.publish(onPublish) }
    )
}

@Composable
fun ReportCreateContent(
    uiState: ReportCreateUiState,
    onBack: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onConfirmMapLocation: (Double, Double) -> Unit,
    onRequestGps: () -> Unit,
    onPublish: () -> Unit,
) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var showMapPicker by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(VozBackground)) {
        Row(
            Modifier.fillMaxWidth().height(47.dp).background(Color.White).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = VozNavy)
            }
            Text("VozBarrial", Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Reportar Incidente", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFD1FAE5)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, null, tint = Color(0xFF047857), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Red de Vigilancia Activa", color = Color(0xFF047857), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Crear Reporte\nCiudadano", color = VozNavy, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                        Text("Ayuda a mejorar la seguridad y convivencia de tu ciudad para bienestar de todos.", color = VozMuted, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(VozNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Section(
                number = "1",
                title = "Selecciona la Categoría",
                subtitle = "Elige la más adecuada",
                badgeText = "Requerido",
                badgeColor = Color(0xFFFFEDD5),
                badgeTextColor = Color(0xFFC2410C)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { menu = true },
                        modifier = Modifier.fillMaxWidth().height(53.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        val current = reportCategories.firstOrNull { it.name == uiState.category } ?: reportCategories[0]
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(current.icon, fontSize = 17.sp)
                            Spacer(Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(current.name, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(current.description, color = VozMuted, fontSize = 10.sp, maxLines = 1)
                            }
                            Text("▾", color = VozNavy, fontSize = 16.sp)
                        }
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        reportCategories.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.icon, fontSize = 15.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(option.name, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(option.description, color = VozMuted, fontSize = 10.sp, maxLines = 1)
                                        }
                                    }
                                },
                                onClick = { onSelectCategory(option.name); menu = false }
                            )
                        }
                    }
                }
            }

            Section(
                number = "2",
                title = "Evidencia Fotográfica",
                subtitle = "Mín. 1, Máx. 3 fotos",
                badgeText = "Mín. 1 foto",
                badgeColor = Color(0xFFD1FAE5),
                badgeTextColor = Color(0xFF047857)
            ) {
                if (uiState.photoUris.size < 3) {
                    Column(
                        Modifier.fillMaxWidth().background(VozBlueSurface, RoundedCornerShape(10.dp)).clickable(onClick = onPickPhoto).padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFD8E5FF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, null, tint = VozNavy)
                        }
                        Text(
                            "Subir fotografía (${uiState.photoUris.size}/3)",
                            color = VozNavy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Captura directa con cámara o galería (JPG, PNG)",
                            color = VozMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                        Text("Almacenamiento seguro y cifrado", color = Color(0xFFE56722), fontSize = 9.sp)
                    }
                }

                if (uiState.photoUris.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.photoUris.forEachIndexed { index, uriStr ->
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE2EEDD))
                            ) {
                                AsyncImage(
                                    model = uriStr,
                                    contentDescription = "Evidencia ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onRemovePhoto(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Section(
                number = "3",
                title = "Ubicación del Incidente",
                subtitle = if (uiState.gpsActive) "GPS Activo" else "Ubicación",
                badgeText = if (uiState.gpsActive) "GPS Activo" else "Ubicación",
                badgeColor = Color(0xFFFFEDD5),
                badgeTextColor = Color(0xFFC2410C)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE2EEDD)).clickable {
                            requestReportLocation(context) { loc ->
                                onConfirmMapLocation(loc.latitude, loc.longitude)
                            }
                            showMapPicker = true
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⌁    ⌁      ⌁\n   ⌁     ⌁    ⌁", color = Color(0xFF91AFC0), fontSize = 26.sp, textAlign = TextAlign.Center)
                        Text("Toca para elegir punto en el mapa", Modifier.align(Alignment.TopCenter).padding(top = 7.dp).background(VozNavy, RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 5.dp), color = Color.White, fontSize = 9.sp)
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFFF7629), modifier = Modifier.align(Alignment.Center).size(30.dp))
                        Text("Abrir mapa  ↗", Modifier.align(Alignment.BottomEnd).padding(7.dp).background(Color.White, RoundedCornerShape(20.dp)).padding(6.dp), color = VozNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        OutlinedButton(
                            onClick = {
                                requestReportLocation(context) { loc ->
                                    onConfirmMapLocation(loc.latitude, loc.longitude)
                                }
                                showMapPicker = true
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⌖  Elegir en mapa", fontSize = 10.sp, color = VozNavy)
                        }
                        Button(
                            onClick = onRequestGps,
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
                        ) {
                            Text("◎  Usar mi GPS", fontSize = 10.sp)
                        }
                    }
                    if (uiState.locationMessage.isNotBlank()) {
                        Text(uiState.locationMessage, color = if (uiState.locationSelected) VozGreen else VozMuted, fontSize = 10.sp)
                    }
                }
            }

            Section(
                number = "4",
                title = "Información Detallada",
                subtitle = "Título y descripción"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Título del reporte", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${uiState.title.length}/60", color = VozMuted, fontSize = 10.sp)
                    }
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = onUpdateTitle,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ej: Luminarias rotas en esquina de parque", fontSize = 12.sp, color = VozMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = VozNavy),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF4F7FB),
                            unfocusedContainerColor = Color(0xFFF4F7FB),
                            focusedBorderColor = Color(0xFFC5D3E8),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Descripción de lo sucedido", color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${uiState.description.length}/280", color = VozMuted, fontSize = 10.sp)
                    }
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onUpdateDescription,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Describe detalles relevantes: hora aproximada, puntos de referencia clave o si hay personas vulnerables en riesgo...", fontSize = 11.sp, color = VozMuted) },
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = VozNavy),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF4F7FB),
                            unfocusedContainerColor = Color(0xFFF4F7FB),
                            focusedBorderColor = Color(0xFFC5D3E8),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (uiState.error != null) {
                Text(uiState.error, color = Color(0xFFB42318), fontSize = 11.sp)
            }

            Button(
                onClick = onPublish,
                enabled = !uiState.publishing,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
            ) {
                if (uiState.publishing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, null)
                }
                Spacer(Modifier.width(7.dp))
                Text(if (uiState.publishing) "Publicando…" else "Publicar Reporte Ciudadano", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (showMapPicker) {
            ReportMapPicker(
                initialLat = uiState.latitude,
                initialLon = uiState.longitude,
                onConfirm = { lat, lon ->
                    onConfirmMapLocation(lat, lon)
                    showMapPicker = false
                },
                onDismiss = { showMapPicker = false }
            )
        }
    }
}

@Composable
private fun Section(
    number: String,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0xFFFFEDD5),
    badgeTextColor: Color = Color(0xFFC2410C),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(VozNavy), contentAlignment = Alignment.Center) {
                    Text(number, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = VozMuted, fontSize = 9.sp)
                }
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            color = badgeTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun ReportMapPicker(initialLat: Double, initialLon: Double, onConfirm: (Double, Double) -> Unit, onDismiss: () -> Unit) {
    var selectedLat by remember(initialLat) { mutableDoubleStateOf(initialLat) }
    var selectedLon by remember(initialLon) { mutableDoubleStateOf(initialLon) }
    var hasMoved by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Elige la ubicación", color = VozNavy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Arrastra el pin hasta el lugar exacto del incidente.", color = VozMuted, fontSize = 11.sp)
                AndroidView(
                    factory = { ctx ->
                        org.osmdroid.config.Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            setBuiltInZoomControls(false)
                            controller.setZoom(16.0)
                            controller.setCenter(GeoPoint(initialLat, initialLon))

                            val marker = Marker(this).apply {
                                position = GeoPoint(initialLat, initialLon)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                isDraggable = true
                                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                    override fun onMarkerDrag(marker: Marker) {}
                                    override fun onMarkerDragStart(marker: Marker) {}
                                    override fun onMarkerDragEnd(marker: Marker) {
                                        selectedLat = marker.position.latitude
                                        selectedLon = marker.position.longitude
                                        hasMoved = true
                                    }
                                })
                            }
                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(390.dp).clip(RoundedCornerShape(12.dp))
                )
                Text(
                    "Lat: ${String.format(Locale.US, "%.5f", selectedLat)} · Lon: ${String.format(Locale.US, "%.5f", selectedLon)}",
                    color = VozMuted,
                    fontSize = 10.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(
                        onClick = { onConfirm(selectedLat, selectedLon) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
                    ) {
                        Text(if (hasMoved) "Usar este punto" else "Confirmar punto")
                    }
                }
            }
        }
    }
}

private fun requestReportLocation(context: Context, onLocation: (Location) -> Unit) {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val providers = manager.getProviders(true).filter { it != "gps" || fine }
    val cached = providers.mapNotNull { provider -> try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } }.maxByOrNull { it.time }
    if (cached != null && System.currentTimeMillis() - cached.time < 30_000L) {
        onLocation(cached)
        return
    }
    var delivered = false
    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!delivered) {
                delivered = true
                try { manager.removeUpdates(this) } catch (_: SecurityException) {}
                onLocation(location)
            }
        }
        @Deprecated("Deprecated by Android")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    providers.forEach { provider ->
        try {
            manager.requestLocationUpdates(provider, 1000L, 0f, listener, Looper.getMainLooper())
        } catch (_: SecurityException) {}
    }
    Handler(Looper.getMainLooper()).postDelayed({
        if (!delivered) {
            delivered = true
            try { manager.removeUpdates(listener) } catch (_: SecurityException) {}
            cached?.let(onLocation)
        }
    }, 4000L)
}

package com.vozbarrial.features.reports.ui

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
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
import com.vozbarrial.features.reports.presentation.ReportCreateUiState
import com.vozbarrial.features.reports.presentation.ReportCreateViewModel
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
    ReportCategory("Infraestructura y Vías", "Baches, fallas de alumbrado, alcantarillado", "⚒"),
    ReportCategory("Mascotas y Animales", "Animales perdidos, encontrados o en peligro", "🐾"),
    ReportCategory("Comunidad y Entorno", "Basuras, polución o ruido excesivo", "♧"),
    ReportCategory("Alumbrado y Servicios", "Postes sin luz, fugas de agua o gas", "♧"),
)

/**
 * Punto de entrada Stateful para crear un reporte.
 */
@Composable
fun ReportCreateRoute(
    name: String,
    onBack: () -> Unit,
    onPublish: (String, String, String, Double, Double, Uri, (Boolean, String?) -> Unit) -> Unit,
    viewModel: ReportCreateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.setPhoto(uri?.toString())
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

/**
 * Vista UI Stateless pura para crear reportes.
 */
@Composable
fun ReportCreateContent(
    uiState: ReportCreateUiState,
    onBack: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onPickPhoto: () -> Unit,
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
            Text("▧ VozBarrial", Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Reportar Incidente", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "✿  Red de Vigilancia Activa",
                    Modifier.background(Color(0xFFDDF8EF), RoundedCornerShape(30.dp)).padding(horizontal = 9.dp, vertical = 4.dp),
                    color = VozGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Crear Reporte Ciudadano", color = VozNavy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Ayuda a mejorar la seguridad y convivencia de tu barrio.", color = VozMuted, fontSize = 11.sp)
                Text("● Paso 1: Categoría     Paso 2: Evidencia     Paso 3: Detalles", color = VozMuted, fontSize = 9.sp)
                LinearProgressIndicator(
                    progress = { .38f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFFFF8138),
                    trackColor = VozBlueSurface
                )
            }

            Section("1", "Selecciona la Categoría", "Requerido") {
                Box {
                    val selected = reportCategories.first { it.name == uiState.category }
                    Column {
                        Row(
                            Modifier.fillMaxWidth().background(VozBlueSurface, RoundedCornerShape(10.dp)).clickable { menu = true }.padding(9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(31.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE1E1)), contentAlignment = Alignment.Center) {
                                Text(selected.icon, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(selected.name, color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Seleccionada",
                                        Modifier.background(Color(0xFFDDF8EF), RoundedCornerShape(20.dp)).padding(horizontal = 5.dp, vertical = 2.dp),
                                        color = VozGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(selected.description, color = VozMuted, fontSize = 9.sp, maxLines = 1)
                            }
                            Text("⌃", color = VozNavy, fontSize = 16.sp)
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, modifier = Modifier.width(300.dp).background(Color.White)) {
                            Text("CATEGORÍAS DISPONIBLES", Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 8.dp), color = VozNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            reportCategories.forEach { option ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (option.name == uiState.category) Color(0xFFE8F1FF) else Color.White)
                                        .clickable { onSelectCategory(option.name); menu = false }
                                        .padding(horizontal = 11.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(29.dp).clip(RoundedCornerShape(7.dp)).background(if (option.name == "Seguridad") Color(0xFFFFE1E1) else Color(0xFFDDEAFF)), contentAlignment = Alignment.Center) {
                                        Text(option.icon, fontSize = 15.sp)
                                    }
                                    Spacer(Modifier.width(9.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(option.name, color = VozNavy, fontSize = 11.sp, fontWeight = if (option.name == uiState.category) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                                        Text(option.description, color = VozMuted, fontSize = 8.sp, maxLines = 1)
                                    }
                                    if (option.name == uiState.category) Text("✓", color = VozNavy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Section("2", "Evidencia Fotográfica", "Mín. 1 foto") {
                Column(
                    Modifier.fillMaxWidth().background(VozBlueSurface, RoundedCornerShape(10.dp)).clickable(onClick = onPickPhoto).padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFD8E5FF)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CameraAlt, null, tint = VozNavy)
                    }
                    Text(
                        if (uiState.photoUri == null) "Subir fotografía del incidente" else "Fotografía lista para adjuntar",
                        color = VozNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (uiState.photoUri == null) "Captura directa con cámara o galería (JPG, PNG)" else "Toca para cambiar la fotografía seleccionada",
                        color = VozMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                    Text("♧ Almacenamiento seguro y cifrado", color = Color(0xFFE56722), fontSize = 9.sp)
                }
                if (uiState.photoUri != null) {
                    Row(Modifier.fillMaxWidth().background(Color(0xFFDDF8EF), RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = VozGreen)
                        Spacer(Modifier.width(6.dp))
                        Text("Evidencia adjunta", color = VozGreen, fontSize = 10.sp)
                    }
                }
            }

            Section("3", "Ubicación del Incidente", if (uiState.gpsActive) "GPS Activo" else "Ubicación") {
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
                    Row(Modifier.fillMaxWidth().background(VozBlueSurface, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = VozNavy, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (uiState.gpsActive) "Ubicación GPS actual" else if (uiState.locationSelected) "Punto elegido en el mapa" else "Selecciona la ubicación del incidente",
                                color = VozNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Lat: ${String.format(Locale.US, "%.5f", uiState.latitude)}, Lon: ${String.format(Locale.US, "%.5f", uiState.longitude)}",
                                color = VozMuted,
                                fontSize = 9.sp
                            )
                        }
                        if (uiState.gpsActive) Icon(Icons.Default.CheckCircle, null, tint = VozGreen, modifier = Modifier.size(17.dp))
                    }
                    if (uiState.locationMessage.isNotBlank()) {
                        Text(uiState.locationMessage, color = if (uiState.gpsActive) VozGreen else Color(0xFFB42318), fontSize = 9.sp)
                    }
                }
            }

            Section("4", "Información Detallada", null) {
                Text("Título del reporte", color = VozNavy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    uiState.title,
                    onUpdateTitle,
                    Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej. Luminaria rota en esquina de parque", fontSize = 10.sp) },
                    supportingText = { Text("${uiState.title.length}/60", Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                    singleLine = true,
                    shape = RoundedCornerShape(9.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                Text("Descripción de lo sucedido", color = VozNavy, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    uiState.description,
                    onUpdateDescription,
                    Modifier.fillMaxWidth().height(104.dp),
                    placeholder = { Text("Describe detalles relevantes: hora aproximada, puntos de referencia clave o si hay personas vulnerables en riesgo...", fontSize = 10.sp, lineHeight = 13.sp) },
                    supportingText = { Text("${uiState.description.length}/280", Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                    shape = RoundedCornerShape(9.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
            }
            if (!uiState.error.isNullOrBlank()) {
                Text(uiState.error.orEmpty(), color = Color(0xFFB42318), fontSize = 11.sp)
            }
        }

        Button(
            onClick = onPublish,
            enabled = !uiState.publishing,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp).height(49.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
        ) {
            if (uiState.publishing) {
                CircularProgressIndicator(Modifier.size(19.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Send, null)
            }
            Spacer(Modifier.width(7.dp))
            Text(if (uiState.publishing) "Publicando…" else "Publicar Reporte Ciudadano", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
        if (!delivered) try { manager.removeUpdates(listener) } catch (_: SecurityException) {}
    }, 12_000L)
}

@Composable
private fun Section(number: String, title: String, badge: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(20.dp).clip(CircleShape).background(VozNavy), contentAlignment = Alignment.Center) {
                Text(number, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(7.dp))
            Text(title, Modifier.weight(1f), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (badge != null) {
                Text(
                    badge,
                    Modifier.background(if (badge.contains("GPS")) Color(0xFFFFE4D8) else Color(0xFFDDF8EF), RoundedCornerShape(30.dp)).padding(horizontal = 6.dp, vertical = 3.dp),
                    color = if (badge.contains("GPS")) Color(0xFFE56722) else VozGreen,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        content()
    }
}
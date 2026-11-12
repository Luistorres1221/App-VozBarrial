package com.vozbarrial.features.reports.create.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.vozbarrial.R
import com.vozbarrial.features.reports.create.presentation.ReportCreateUiState
import com.vozbarrial.features.reports.create.presentation.ReportCreateViewModel
import com.vozbarrial.ui.theme.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

private data class ReportCategory(val id: String, val name: String, val description: String, val icon: String)

@Composable
private fun getReportCategories(): List<ReportCategory> {
    return listOf(
        ReportCategory("Seguridad", stringResource(R.string.category_security), stringResource(R.string.category_security_desc), "🛡"),
        ReportCategory("Emergencias Médicas", stringResource(R.string.category_emergencies), stringResource(R.string.category_emergencies_desc), "✚"),
        ReportCategory("Infraestructura y Vías", stringResource(R.string.category_infrastructure), stringResource(R.string.category_infrastructure_desc), "🚧"),
        ReportCategory("Mascotas y Animales", stringResource(R.string.category_pets), stringResource(R.string.category_pets_desc), "🐾"),
        ReportCategory("Comunidad y Entorno", stringResource(R.string.category_community), stringResource(R.string.category_community_desc), "♧"),
        ReportCategory("Alumbrado y Servicios", stringResource(R.string.category_lighting), stringResource(R.string.category_lighting_desc), "💡"),
    )
}

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
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {}
            viewModel.addPhoto(uri.toString())
        }
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
    val reportCategories = getReportCategories()

    val gpsActiveText = stringResource(R.string.gps_active)
    val locationText = stringResource(R.string.location)

    Column(Modifier.fillMaxSize().background(VozBackground)) {
        Row(
            Modifier.fillMaxWidth().height(47.dp).background(Color.White).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = VozNavy)
            }
            Text(stringResource(R.string.app_name), Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(stringResource(R.string.report_incident), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 10.sp)
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
                                Text(stringResource(R.string.active_vigilance_network), color = Color(0xFF047857), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(stringResource(R.string.create_citizen_report), color = VozNavy, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                        Text(stringResource(R.string.report_help_message), color = VozMuted, fontSize = 11.sp)
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
                title = stringResource(R.string.select_category),
                subtitle = stringResource(R.string.choose_most_suitable),
                badgeText = stringResource(R.string.required),
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
                        val current = reportCategories.firstOrNull { it.id == uiState.category } ?: reportCategories[0]
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
                                onClick = { onSelectCategory(option.id); menu = false }
                            )
                        }
                    }
                }
            }

            Section(
                number = "2",
                title = stringResource(R.string.photographic_evidence),
                subtitle = stringResource(R.string.min_1_max_3_photos),
                badgeText = stringResource(R.string.min_1_photo),
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
                            stringResource(R.string.upload_photo_format, uiState.photoUris.size),
                            color = VozNavy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.upload_photo_instructions),
                            color = VozMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(stringResource(R.string.secure_encrypted_storage), color = Color(0xFFE56722), fontSize = 9.sp)
                    }
                }

                if (uiState.photoUris.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.photoUris.forEachIndexed { index, uriStr ->
                            val bitmap = remember<androidx.compose.ui.graphics.ImageBitmap?>(uriStr) {
                                runCatching {
                                    context.contentResolver.openInputStream(Uri.parse(uriStr))
                                        ?.use(BitmapFactory::decodeStream)
                                        ?.asImageBitmap()
                                }.getOrNull()
                            }
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE2EEDD))
                            ) {
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap,
                                        contentDescription = "Evidencia ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = uriStr,
                                        contentDescription = "Evidencia ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                IconButton(
                                    onClick = { onRemovePhoto(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete), tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            Section(
                number = "3",
                title = stringResource(R.string.incident_location),
                subtitle = if (uiState.gpsActive) gpsActiveText else locationText,
                badgeText = if (uiState.gpsActive) gpsActiveText else locationText,
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
                        Text(stringResource(R.string.tap_to_choose_map_point), Modifier.align(Alignment.TopCenter).padding(top = 7.dp).background(VozNavy, RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 5.dp), color = Color.White, fontSize = 9.sp)
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFFFF7629), modifier = Modifier.align(Alignment.Center).size(30.dp))
                        Text(stringResource(R.string.open_map), Modifier.align(Alignment.BottomEnd).padding(7.dp).background(Color.White, RoundedCornerShape(20.dp)).padding(6.dp), color = VozNavy, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                            Text(stringResource(R.string.choose_on_map), fontSize = 10.sp, color = VozNavy)
                        }
                        Button(
                            onClick = onRequestGps,
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
                        ) {
                            Text(stringResource(R.string.use_my_gps), fontSize = 10.sp)
                        }
                    }
                    if (uiState.locationMessage.isNotBlank()) {
                        Text(uiState.locationMessage, color = if (uiState.locationSelected) VozGreen else VozMuted, fontSize = 10.sp)
                    }
                }
            }

            Section(
                number = "4",
                title = stringResource(R.string.detailed_information),
                subtitle = stringResource(R.string.title_and_description)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.report_title_label), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${uiState.title.length}/60", color = VozMuted, fontSize = 10.sp)
                    }
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = onUpdateTitle,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.report_title_placeholder), fontSize = 12.sp, color = VozMuted) },
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
                        Text(stringResource(R.string.report_description_label), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${uiState.description.length}/280", color = VozMuted, fontSize = 10.sp)
                    }
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onUpdateDescription,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text(stringResource(R.string.report_description_placeholder), fontSize = 11.sp, color = VozMuted) },
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
                Text(if (uiState.publishing) stringResource(R.string.sending) else stringResource(R.string.send), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showMapPicker) {
        Dialog(onDismissRequest = { showMapPicker = false }) {
            Surface(modifier = Modifier.fillMaxWidth().height(400.dp), shape = RoundedCornerShape(16.dp)) {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            org.osmdroid.config.Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(16.0)
                                controller.setCenter(GeoPoint(uiState.latitude, uiState.longitude))
                                val marker = Marker(this).apply {
                                    position = GeoPoint(uiState.latitude, uiState.longitude)
                                    isDraggable = true
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                overlays.add(marker)
                                setOnTouchListener { v, _ ->
                                    v.parent?.requestDisallowInterceptTouchEvent(true)
                                    false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Button(
                        onClick = { showMapPicker = false },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VozNavy)
                    ) {
                        Text(stringResource(R.string.dialog_understood))
                    }
                }
            }
        }
    }
}

private fun requestReportLocation(context: Context, onLocation: (Location) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    try {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return

        val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val best = listOfNotNull(lastGps, lastNet).maxByOrNull { it.time }
        if (best != null && System.currentTimeMillis() - best.time < 120000) {
            onLocation(best)
            return
        }

        val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        locationManager.requestSingleUpdate(provider, object : LocationListener {
            override fun onLocationChanged(loc: Location) { onLocation(loc) }
            override fun onProviderEnabled(p0: String) {}
            override fun onProviderDisabled(p0: String) {}
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        }, Looper.getMainLooper())
    } catch (_: SecurityException) {}
}

@Composable
private fun Section(number: String, title: String, subtitle: String, badgeText: String? = null, badgeColor: Color = Color.Transparent, badgeTextColor: Color = Color.Transparent, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(VozBlueSurface), contentAlignment = Alignment.Center) {
                        Text(number, color = VozNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(title, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(subtitle, color = VozMuted, fontSize = 10.sp)
                    }
                }
                if (badgeText != null) {
                    Surface(shape = RoundedCornerShape(50), color = badgeColor) {
                        Text(badgeText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), color = badgeTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            content()
        }
    }
}

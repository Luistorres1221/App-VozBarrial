package com.vozbarrial.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import com.google.gson.JsonPrimitive
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.ComposeMapInitOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.OnScaleListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.vozbarrial.domain.model.GeoCoordinate
import kotlinx.coroutines.awaitCancellation

/** Marcador a dibujar en [MapBox]. El [id] se devuelve en `onMarkerClick`. */
data class MapMarker(val id: String, val position: GeoCoordinate, val icon: Bitmap)

private const val FOLLOW_USER_ZOOM = 16.0

private fun GeoCoordinate.toPoint(): Point = Point.fromLngLat(longitude, latitude)

/**
 * Mapa Mapbox reutilizable.
 *
 * @param center centro inicial de la cámara (solo se aplica en la primera composición).
 * @param zoom zoom inicial de la cámara.
 * @param markers marcadores a dibujar; al tocar uno se llama a [onMarkerClick] con su id.
 * @param showUserLocation muestra el punto de ubicación y empieza a seguir al usuario cuando pasa a true
 *   (requiere permiso de ubicación).
 * @param userLocationIcon ícono del punto de ubicación; si es null se usa el puck por defecto de Mapbox.
 * @param followUserRequest contador: cada incremento centra la cámara en el usuario con zoom 16.
 * @param userLocationFallback si no se muestra la ubicación, [followUserRequest] centra aquí con zoom 16.
 * @param recenterTo cada vez que cambia, la cámara se centra en ese punto (sin cambiar el zoom).
 * @param interactive si es false, el mapa no responde a gestos.
 * @param textureView dibuja el mapa en un TextureView para que respete el recorte de su contenedor
 *   (por ejemplo, las esquinas redondeadas de un diálogo).
 * @param onCameraCenterChanged informa el centro actual de la cámara cada vez que cambia.
 * @param onUserGesture se llama cuando el usuario empieza a mover o hacer zoom en el mapa.
 */
@Composable
fun MapBox(
    center: GeoCoordinate,
    zoom: Double,
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    onMarkerClick: (String) -> Unit = {},
    showUserLocation: Boolean = false,
    userLocationIcon: Bitmap? = null,
    followUserRequest: Int = 0,
    userLocationFallback: GeoCoordinate? = null,
    recenterTo: GeoCoordinate? = null,
    interactive: Boolean = true,
    textureView: Boolean = false,
    onCameraCenterChanged: ((GeoCoordinate) -> Unit)? = null,
    onUserGesture: () -> Unit = {},
) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(center.toPoint())
            zoom(zoom)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnUserGesture by rememberUpdatedState(onUserGesture)
    val currentOnCameraCenterChanged by rememberUpdatedState(onCameraCenterChanged)
    val currentFallback by rememberUpdatedState(userLocationFallback)

    // Informa el centro de la cámara.
    LaunchedEffect(mapViewportState) {
        snapshotFlow { mapViewportState.cameraState?.center }.collect { point ->
            if (point != null) {
                currentOnCameraCenterChanged?.invoke(GeoCoordinate(point.latitude(), point.longitude()))
            }
        }
    }

    // Recentrar cuando cambia recenterTo.
    LaunchedEffect(recenterTo) {
        recenterTo?.let { target ->
            mapViewportState.setCameraOptions { center(target.toPoint()) }
        }
    }

    // Seguir al usuario al activar la ubicación, sin cambiar el zoom.
    LaunchedEffect(showUserLocation) {
        if (showUserLocation) {
            mapViewportState.transitionToFollowPuckState(followOptions(zoom = null))
        }
    }

    // Botón "Mi ubicación" de la pantalla.
    LaunchedEffect(followUserRequest) {
        if (followUserRequest > 0) {
            if (showUserLocation) {
                mapViewportState.transitionToFollowPuckState(followOptions(zoom = FOLLOW_USER_ZOOM))
            } else {
                currentFallback?.let { target ->
                    mapViewportState.setCameraOptions {
                        center(target.toPoint())
                        zoom(FOLLOW_USER_ZOOM)
                    }
                }
            }
        }
    }

    val annotations = remember(markers) {
        markers.map { marker ->
            PointAnnotationOptions()
                .withPoint(marker.position.toPoint())
                .withIconImage(marker.icon)
                .withIconAnchor(IconAnchor.BOTTOM)
                .withData(JsonPrimitive(marker.id))
        }
    }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            composeMapInitOptions = ComposeMapInitOptions(
                pixelRatio = LocalDensity.current.density,
                textureView = textureView,
            ),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = Style.MAPBOX_STREETS) },
        ) {
            MapEffect(showUserLocation, userLocationIcon) { mapView ->
                mapView.location.updateSettings {
                    enabled = showUserLocation
                    locationPuck = userLocationIcon?.let { LocationPuck2D(topImage = ImageHolder.from(it)) }
                        ?: createDefault2DPuck()
                }
            }

            MapEffect(Unit) { mapView ->
                val moveListener = object : OnMoveListener {
                    override fun onMoveBegin(detector: MoveGestureDetector) = currentOnUserGesture()
                    override fun onMove(detector: MoveGestureDetector): Boolean = false
                    override fun onMoveEnd(detector: MoveGestureDetector) {}
                }
                val scaleListener = object : OnScaleListener {
                    override fun onScaleBegin(detector: StandardScaleGestureDetector) = currentOnUserGesture()
                    override fun onScale(detector: StandardScaleGestureDetector) {}
                    override fun onScaleEnd(detector: StandardScaleGestureDetector) {}
                }
                mapView.gestures.addOnMoveListener(moveListener)
                mapView.gestures.addOnScaleListener(scaleListener)
                try {
                    awaitCancellation()
                } finally {
                    mapView.gestures.removeOnMoveListener(moveListener)
                    mapView.gestures.removeOnScaleListener(scaleListener)
                }
            }

            if (annotations.isNotEmpty()) {
                PointAnnotationGroup(annotations = annotations) {
                    interactionsState.onClicked { annotation ->
                        annotation.getData()?.asString?.let(currentOnMarkerClick)
                        true
                    }
                }
            }
        }

        if (!interactive) {
            Box(Modifier.matchParentSize().pointerInput(Unit) {})
        }
    }
}

private fun followOptions(zoom: Double?): FollowPuckViewportStateOptions =
    FollowPuckViewportStateOptions.Builder()
        .zoom(zoom)
        .pitch(0.0)
        .bearing(FollowPuckViewportStateBearing.Constant(0.0))
        .build()

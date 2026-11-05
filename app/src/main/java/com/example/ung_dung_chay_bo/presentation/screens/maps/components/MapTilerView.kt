package com.example.ung_dung_chay_bo.presentation.screens.maps.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

enum class MapTilerLayerStyle(val title: String, val url: String) {
    OUTDOOR("Thể thao (Outdoor)", Constants.MAPTILER_STYLE_OUTDOOR),
    STREETS("Đường phố (Streets)", Constants.MAPTILER_STYLE_STREETS),
    SATELLITE("Vệ tinh (Satellite)", Constants.MAPTILER_STYLE_SATELLITE)
}

@Composable
fun MapTilerView(
    routes: List<SuggestedRoute>,
    selectedRoute: SuggestedRoute?,
    userLocation: Pair<Double, Double>,
    layerStyle: MapTilerLayerStyle = MapTilerLayerStyle.OUTDOOR,
    is3D: Boolean = false,
    onSelectRoute: (SuggestedRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }
    val markerRouteMap = remember { mutableStateMapOf<Marker, SuggestedRoute>() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(layerStyle) {
        mapLibreMap?.setStyle(Style.Builder().fromUri(layerStyle.url))
    }

    LaunchedEffect(is3D) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val tilt = if (is3D) 55.0 else 0.0
        val currentCam = map.cameraPosition
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder(currentCam)
                    .tilt(tilt)
                    .build()
            ),
            600
        )
    }

    LaunchedEffect(selectedRoute, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect

        currentPolyline?.let {
            map.removePolyline(it)
            currentPolyline = null
        }

        selectedRoute?.let { route ->
            if (route.routePoints.size >= 2) {
                val latLngs = route.routePoints.map { LatLng(it.first, it.second) }
                val polylineOptions = PolylineOptions()
                    .addAll(latLngs)
                    .color(AndroidColor.parseColor("#FC4C02"))
                    .width(6f)

                currentPolyline = map.addPolyline(polylineOptions)

                val targetLatLng = LatLng(route.startPoint.first, route.startPoint.second)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(targetLatLng, 13.5),
                    800
                )
            }
        }
    }

    LaunchedEffect(routes, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect

        map.clear()
        markerRouteMap.clear()

        selectedRoute?.let { route ->
            if (route.routePoints.size >= 2) {
                val latLngs = route.routePoints.map { LatLng(it.first, it.second) }
                currentPolyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(latLngs)
                        .color(AndroidColor.parseColor("#FC4C02"))
                        .width(6f)
                )
            }
        }

        map.addMarker(
            MarkerOptions()
                .position(LatLng(userLocation.first, userLocation.second))
                .title("Vị trí của bạn")
                .snippet("Khương Trung, Thanh Xuân, Hà Nội")
        )

        routes.forEach { route ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(route.startPoint.first, route.startPoint.second))
                    .title(route.title)
                    .snippet("${route.areaName} • ${route.distanceKm} km • ${route.durationFormatted}")
            )
            markerRouteMap[marker] = route
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    mapLibreMap = map

                    map.setStyle(Style.Builder().fromUri(layerStyle.url)) {
                        val initialPosition = CameraPosition.Builder()
                            .target(LatLng(userLocation.first, userLocation.second))
                            .zoom(12.8)
                            .tilt(if (is3D) 55.0 else 0.0)
                            .build()
                        map.cameraPosition = initialPosition

                        map.uiSettings.isRotateGesturesEnabled = true
                        map.uiSettings.isTiltGesturesEnabled = true
                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true
                        map.uiSettings.isCompassEnabled = false
                    }

                    map.setOnMarkerClickListener { clickedMarker ->
                        val matchedRoute = markerRouteMap[clickedMarker]
                        if (matchedRoute != null) {
                            onSelectRoute(matchedRoute)
                            true
                        } else {
                            false
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

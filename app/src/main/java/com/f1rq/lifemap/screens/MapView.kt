package com.f1rq.lifemap.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.f1rq.lifemap.components.AddEvent
import com.f1rq.lifemap.components.AddEventCard
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import com.google.android.gms.location.LocationServices
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.MapView as OSMMapView

@Composable
fun MapView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = koinViewModel()
) {
    var showSheet by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<OSMMapView?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        hasLocationPermission = permission[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permission[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && mapView != null) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLocation = GeoPoint(it.latitude, it.longitude)
                        mapView?.controller?.animateTo(userLocation)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission error
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                OSMMapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    val mapController = controller
                    mapController.setZoom(15.0)
                    mapController.setCenter(GeoPoint(48.8566, 2.3522)) // Default center

                    if (hasLocationPermission) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.enableMyLocation()
                        locationOverlay.enableFollowLocation()
                        overlays.add(locationOverlay)
                    }

                    mapView = this
                }
            },
            update = { view ->
                if (hasLocationPermission && view.overlays.isEmpty()) {
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), view)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    view.overlays.add(locationOverlay)
                    view.invalidate()
                }
            }
        )

        AddEventCard(
            onCreateEventClick = { showSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        if (showSheet) {
            AddEvent(
                onDismiss = { showSheet = false },
                viewModel = viewModel
            )
        }
    }
}
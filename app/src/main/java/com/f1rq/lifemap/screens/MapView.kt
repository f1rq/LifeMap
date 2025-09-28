package com.f1rq.lifemap.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.f1rq.lifemap.R
import com.f1rq.lifemap.components.AddEventCard
import com.f1rq.lifemap.components.AddEvent
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import com.google.android.gms.location.LocationServices
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.MapView as OSMMapView
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.overlay.Marker
import java.io.File
import androidx.core.graphics.drawable.DrawableCompat

@SuppressLint("UseKtx")
@Composable
fun MapView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<OSMMapView?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentUserLocation by remember { mutableStateOf<GeoPoint?>(null)}

    val context = LocalContext.current

    val positronTileSource = XYTileSource (
        "CartoDB_Positron",
        1, 19, 256, ".png",
        arrayOf("https://a.basemaps.cartocdn.com/light_all/")
    )

    // Function to center map on user location
    fun centerOnUserLocation() {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLocation = GeoPoint(it.latitude, it.longitude)
                        currentUserLocation = userLocation
                        mapView?.controller?.animateTo(userLocation)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission error
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName

        // Performance optimizations
        Configuration.getInstance().cacheMapTileCount = 12
        Configuration.getInstance().cacheMapTileOvershoot = 2

        val osmDir = File(context.cacheDir, "osmdroid")
        osmDir.mkdirs()
        Configuration.getInstance().osmdroidBasePath = osmDir
        Configuration.getInstance().osmdroidTileCache = File(osmDir, "tiles")

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
                        currentUserLocation = userLocation
                        mapView?.controller?.animateTo(userLocation)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission error
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Map implementation
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OSMMapView(ctx).apply {
                    setTileSource(positronTileSource)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true

                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                    val mapController = controller
                    mapController.setZoom(15.0)
                    mapController.setCenter(GeoPoint(48.8566, 2.3522))

                    // Initialize location overlay once during factory
                    if (hasLocationPermission) {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.setPersonIcon(null)
                        locationOverlay.setDirectionIcon(null)
                        locationOverlay.enableMyLocation()
                        locationOverlay.enableFollowLocation()
                        overlays.add(locationOverlay)
                    }

                    post { invalidate() }
                    mapView = this
                }
            },
            update = { view ->
                view.overlays.removeAll { it is Marker }

                uiState.events.forEach { event ->
                    if (event.latitude != null && event.longitude != null) {
                        val marker = Marker(view)
                        marker.position = GeoPoint(event.latitude, event.longitude)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = event.name
                        marker.snippet = event.description

                        val drawable = ContextCompat.getDrawable(context, R.drawable.location_on_36px)?.mutate()
                        drawable?.let {
                            val categoryColor = viewModel.getCategoryColor(event.category)
                            DrawableCompat.setTint(it, categoryColor.toArgb())
                            marker.icon = it
                        }

                        view.overlays.add(marker)
                    }
                }
                view.invalidate()
            }
        )

        // Floating action buttons
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Map zoom in button
            FloatingActionButton(
                onClick = {
                    mapView?.controller?.zoomIn()
                },
                modifier = Modifier.size(48.dp),
                containerColor = MainBG
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = MainTextColor
                )
            }

            // Map zoom out button
            FloatingActionButton(
                onClick = {
                    mapView?.controller?.zoomOut()
                },
                modifier = Modifier.size(48.dp),
                containerColor = MainBG
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = MainTextColor
                )
            }

            // Center on user location button
            FloatingActionButton(
                onClick = {
                    centerOnUserLocation()
                },
                modifier = Modifier.size(48.dp),
                containerColor = MainBG
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = MainTextColor
                )
            }
        }

        // AddEventCard
        AddEventCard(
            onCreateEventClick = { showSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Sheet for adding events
        if (showSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1000f)
            ) {
                AddEvent(
                    onDismiss = { showSheet = false },
                    viewModel = viewModel,
                    navController = navController,
                    currentLocation = currentUserLocation
                )
            }
        }
    }
}
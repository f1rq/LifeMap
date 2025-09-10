package com.f1rq.lifemap.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.f1rq.lifemap.components.AddEvent
import com.f1rq.lifemap.components.AddEventCard
import com.f1rq.lifemap.ui.screens.NominatimAPI
import com.f1rq.lifemap.ui.screens.PlaceSuggestion
import com.f1rq.lifemap.ui.screens.PlaceCategory
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.osmdroid.views.MapView as OSMMapView
import java.io.File

@Composable
fun MapView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = koinViewModel(),
    isLocationPickerMode: Boolean = false,
    onLocationPicked: ((GeoPoint) -> Unit)? = null,
    onCancelLocationPicker: (() -> Unit)? = null,
    initialPickerLocation: GeoPoint? = null
) {
    var showSheet by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<OSMMapView?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentUserLocation by remember { mutableStateOf<GeoPoint?>(null)}
    var searchQuery by remember { mutableStateOf("") }
    var placeSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedPickerLocation by remember { mutableStateOf(initialPickerLocation) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize Nominatim API for location picker mode
    val nominatimAPI = remember {
        if (isLocationPickerMode) {
            Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NominatimAPI::class.java)
        } else null
    }

    // Function to search places (only for location picker mode)
    fun searchPlaces(query: String) {
        if (!isLocationPickerMode || nominatimAPI == null) return

        if (query.length < 2) {
            placeSuggestions = emptyList()
            showSuggestions = false
            isSearching = false
            return
        }

        isSearching = true
        coroutineScope.launch {
            try {
                val suggestions = withContext(Dispatchers.IO) {
                    searchWithNominatim(nominatimAPI, query, currentUserLocation)
                }
                placeSuggestions = suggestions
                showSuggestions = suggestions.isNotEmpty()
                isSearching = false
            } catch (e: Exception) {
                placeSuggestions = emptyList()
                showSuggestions = false
                isSearching = false
            }
        }
    }

    // Function to center map on user location
    fun centerOnUserLocation() {
        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLocation = GeoPoint(it.latitude, it.longitude)
                        currentUserLocation = userLocation
                        if (isLocationPickerMode) {
                            selectedPickerLocation = userLocation
                            updateMapMarker(mapView, userLocation, context, true)
                        } else {
                            mapView?.controller?.animateTo(userLocation)
                            mapView?.controller?.setZoom(15.0)
                        }
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
                        if (!isLocationPickerMode) {
                            mapView?.controller?.animateTo(userLocation)
                            mapView?.controller?.setZoom(15.0)
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission error
            }
        }
    }

    LaunchedEffect(initialPickerLocation) {
        selectedPickerLocation = initialPickerLocation
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Search bar (only in picker mode)
        if (isLocationPickerMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .zIndex(9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MainBG)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchPlaces(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Search places, addresses...") },
                    leadingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, "Search")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    placeSuggestions = emptyList()
                                    showSuggestions = false
                                }
                            ) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }

        // Map implementation
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                OSMMapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true

                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                    val mapController = controller
                    mapController.setZoom(15.0)

                    val initialLocation = if (isLocationPickerMode) {
                        selectedPickerLocation ?: GeoPoint(48.8566, 2.3522)
                    } else {
                        GeoPoint(48.8566, 2.3522)
                    }
                    mapController.setCenter(initialLocation)

                    if (isLocationPickerMode) {
                        // Add initial marker for picker mode
                        selectedPickerLocation?.let { location ->
                            val marker = Marker(this).apply {
                                position = location
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Selected Location"
                            }
                            overlays.add(marker)
                        }

                        // Add tap listener for picker mode
                        setOnTouchListener { view, event ->
                            if (event.action == MotionEvent.ACTION_UP) {
                                view.performClick()
                                val projection = projection
                                val geoPoint = projection.fromPixels(
                                    event.x.toInt(),
                                    event.y.toInt()
                                ) as GeoPoint

                                selectedPickerLocation = geoPoint
                                updateMapMarker(this, geoPoint, context, true)
                                showSuggestions = false
                            }
                            false
                        }
                    }

                    post { invalidate() }
                    mapView = this
                }
            },
            update = { view ->
                if (hasLocationPermission && !isLocationPickerMode) {
                    view.overlays.removeAll { it is MyLocationNewOverlay }

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), view)
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    view.overlays.add(locationOverlay)
                    view.invalidate()
                }
            }
        )

        // Search suggestions (only in picker mode)
        if (isLocationPickerMode && showSuggestions && placeSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 140.dp)
                    .heightIn(max = 400.dp)
                    .zIndex(8f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                LazyColumn {
                    items(placeSuggestions) { suggestion ->
                        PlaceSuggestionItem(
                            suggestion = suggestion,
                            onClick = {
                                selectedPickerLocation = GeoPoint(suggestion.latitude, suggestion.longitude)
                                updateMapMarker(mapView, selectedPickerLocation!!, context, true)
                                showSuggestions = false
                                searchQuery = suggestion.name
                            }
                        )
                    }
                }
            }
        }

        // FLOATING ACTION BUTTONS - Same for both modes
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = if (isLocationPickerMode) 100.dp else 16.dp), // Adjust top padding for picker mode
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
                    painter = androidx.compose.ui.res.painterResource(id = com.f1rq.lifemap.R.drawable.add_24px),
                    contentDescription = "Zoom In"
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
                    painter = androidx.compose.ui.res.painterResource(id = com.f1rq.lifemap.R.drawable.remove_24px),
                    contentDescription = "Zoom Out"
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
                    painter = androidx.compose.ui.res.painterResource(id = com.f1rq.lifemap.R.drawable.location_searching_24px),
                    contentDescription = "My Location"
                )
            }
        }

        // Bottom bar (only in picker mode)
        if (isLocationPickerMode) {
            selectedPickerLocation?.let { location ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .zIndex(7f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MainBG)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back button
                        IconButton(onClick = { onCancelLocationPicker?.invoke() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MainTextColor
                            )
                        }

                        // Location info (centered)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Selected Location",
                                style = MaterialTheme.typography.titleMedium,
                                color = MainTextColor
                            )
                            Text(
                                text = "Lat: ${String.format("%.6f", location.latitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MainTextColor
                            )
                            Text(
                                text = "Lng: ${String.format("%.6f", location.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MainTextColor
                            )
                        }

                        // Done button
                        TextButton(
                            onClick = {
                                selectedPickerLocation?.let { onLocationPicked?.invoke(it) }
                            }
                        ) {
                            Text(
                                text = "Done",
                                color = MainBG,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        } else {
            // AddEventCard (only in normal mode)
            AddEventCard(
                onCreateEventClick = { showSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Sheet for adding events (only in normal mode)
        if (!isLocationPickerMode && showSheet) {
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

@Composable
fun PlaceSuggestionItem(
    suggestion: PlaceSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (suggestion.category) {
                PlaceCategory.RESTAURANT -> Icons.Default.Search
                PlaceCategory.FAST_FOOD -> Icons.Default.Search
                PlaceCategory.CAFE -> Icons.Default.Search
                PlaceCategory.SHOP -> Icons.Default.Search
                PlaceCategory.GAS_STATION -> Icons.Default.Search
                PlaceCategory.HOSPITAL -> Icons.Default.Search
                PlaceCategory.SCHOOL -> Icons.Default.Search
                PlaceCategory.HOTEL -> Icons.Default.Search
                PlaceCategory.BANK -> Icons.Default.Search
                PlaceCategory.UNKNOWN -> Icons.Default.Search
            },
            contentDescription = null,
            modifier = Modifier.padding(end = 12.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = suggestion.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun updateMapMarker(mapView: OSMMapView?, location: GeoPoint, context: Context, isPickerMode: Boolean = false) {
    mapView?.let { map ->
        if (isPickerMode) {
            map.overlays.clear()
            val marker = Marker(map).apply {
                position = location
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Selected Location"
            }
            map.overlays.add(marker)
            map.controller.animateTo(location)
            map.invalidate()
        }
    }
}

private suspend fun searchWithNominatim(
    nominatimAPI: NominatimAPI,
    query: String,
    userLocation: GeoPoint?
): List<PlaceSuggestion> {
    return try {
        val results = nominatimAPI.searchPlaces(
            query = query,
            lat = userLocation?.latitude,
            lon = userLocation?.longitude
        )

        results.map { place ->
            PlaceSuggestion(
                name = place.display_name.split(",").first().trim(),
                address = place.display_name,
                latitude = place.lat.toDouble(),
                longitude = place.lon.toDouble(),
                category = PlaceCategory.UNKNOWN
            )
        }.distinctBy { "${it.name}-${it.latitude}-${it.longitude}" }
    } catch (e: Exception) {
        emptyList()
    }
}
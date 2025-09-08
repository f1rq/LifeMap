package com.f1rq.lifemap.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.f1rq.lifemap.ui.theme.MainBG
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.text.toInt

// Nominatim API interface
interface NominatimAPI {
    @GET("search")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 15,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius") radius: Int = 25000,
        @Query("bounded") bounded: Int = 1
    ): List<NominatimPlace>
}

// Data classes for Nominatim response
data class NominatimPlace(
    val display_name: String,
    val lat: String,
    val lon: String,
    val type: String?,
    val `class`: String?,
    val address: NominatimAddress?,
    val icon: String?
)

data class NominatimAddress(
    val amenity: String?,
    val shop: String?,
    val restaurant: String?,
    val fast_food: String?,
    val cafe: String?,
    val fuel: String?,
    val hospital: String?,
    val school: String?,
    val house_number: String?,
    val road: String?,
    val neighbourhood: String?,
    val suburb: String?,
    val city: String?,
    val town: String?,
    val village: String?,
    val state: String?,
    val country: String?,
    val postcode: String?
)

// Enhanced PlaceSuggestion data class
data class PlaceSuggestion(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val types: List<String> = emptyList(),
    val category: PlaceCategory = PlaceCategory.UNKNOWN
)

// Place categories for better icons
enum class PlaceCategory {
    RESTAURANT, FAST_FOOD, CAFE, SHOP, GAS_STATION, HOSPITAL, SCHOOL, HOTEL, BANK, UNKNOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    onLocationPicked: (GeoPoint) -> Unit,
    onCancel: () -> Unit,
    initialLocation: GeoPoint? = null
) {
    var selectedLocation by remember {
        mutableStateOf(initialLocation ?: GeoPoint(48.8566, 2.3522))
    }
    var searchQuery by remember { mutableStateOf("") }
    var placeSuggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize Nominatim API
    val nominatimAPI = remember {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimAPI::class.java)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context, fusedLocationClient) { location ->
                userLocation = location
                if (initialLocation == null) {
                    selectedLocation = location
                    updateMapMarker(mapView, location, context)
                }
            }
        }
    }

    // Check location permission on start
    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation(context, fusedLocationClient) { location ->
                    userLocation = location
                    if (initialLocation == null) {
                        selectedLocation = location
                        updateMapMarker(mapView, location, context)
                    }
                }
            }
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Function to search places using Nominatim API
    fun searchPlaces(query: String) {
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
                    searchWithNominatim(nominatimAPI, query, userLocation)
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

    // Fullscreen location picker that covers everything including system bars
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom Top Bar (replaces TopAppBar)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(), // Handle status bar padding
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Pick Location",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = { onLocationPicked(selectedLocation) }
                    ) {
                        Text("Done")
                    }
                }
            }

            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        Row {
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
                            userLocation?.let {
                                IconButton(
                                    onClick = {
                                        selectedLocation = it
                                        updateMapMarker(mapView, it, context)
                                    }
                                ) {
                                    Icon(Icons.Default.GpsFixed, "My Location")
                                }
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding() // Handle navigation bar padding
            ) {
                // Map (always visible)
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(selectedLocation)

                            // Add initial marker
                            val marker = Marker(this).apply {
                                position = selectedLocation
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Selected Location"
                            }
                            overlays.add(marker)

                            // Add tap listener
                            setOnTouchListener { view, event ->
                                if (event.action == MotionEvent.ACTION_UP) {
                                    view.performClick()
                                    val projection = projection
                                    val geoPoint = projection.fromPixels(
                                        event.x.toInt(),
                                        event.y.toInt()
                                    ) as GeoPoint

                                    selectedLocation = geoPoint
                                    updateMapMarker(this, geoPoint, context)
                                    showSuggestions = false
                                }
                                false
                            }

                            mapView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Search Suggestions Overlay (on top of map)
                if (showSuggestions && placeSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .heightIn(max = 400.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        LazyColumn {
                            items(placeSuggestions) { suggestion ->
                                PlaceSuggestionItem(
                                    suggestion = suggestion,
                                    onClick = {
                                        selectedLocation = GeoPoint(suggestion.latitude, suggestion.longitude)
                                        updateMapMarker(mapView, selectedLocation, context)
                                        showSuggestions = false
                                        searchQuery = suggestion.name
                                    }
                                )
                            }
                        }
                    }
                }

                // Selected Location Info (Bottom)
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selected Location",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Latitude: ${String.format("%.6f", selectedLocation.latitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Longitude: ${String.format("%.6f", selectedLocation.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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
                PlaceCategory.RESTAURANT -> Icons.Default.Restaurant
                PlaceCategory.FAST_FOOD -> Icons.Default.Fastfood
                PlaceCategory.CAFE -> Icons.Default.LocalCafe
                PlaceCategory.SHOP -> Icons.Default.ShoppingCart
                PlaceCategory.GAS_STATION -> Icons.Default.LocalGasStation
                PlaceCategory.HOSPITAL -> Icons.Default.LocalHospital
                PlaceCategory.SCHOOL -> Icons.Default.School
                PlaceCategory.HOTEL -> Icons.Default.Hotel
                PlaceCategory.BANK -> Icons.Default.AccountBalance
                PlaceCategory.UNKNOWN -> Icons.Default.Place
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

// Function to update map marker
private fun updateMapMarker(mapView: MapView?, location: GeoPoint, context: Context) {
    mapView?.let { map ->
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

// Nominatim search function
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
            val address = place.address
            val name = extractPlaceName(place, address)
            val fullAddress = buildFullAddress(address, place.display_name)
            val category = determineCategory(place, address)

            PlaceSuggestion(
                name = name,
                address = fullAddress,
                latitude = place.lat.toDouble(),
                longitude = place.lon.toDouble(),
                types = listOfNotNull(place.type, place.`class`),
                category = category
            )
        }.distinctBy { "${it.name}-${it.latitude}-${it.longitude}" }
    } catch (e: Exception) {
        emptyList()
    }
}

// Helper function to extract meaningful place name
private fun extractPlaceName(place: NominatimPlace, address: NominatimAddress?): String {
    return address?.amenity
        ?: address?.shop
        ?: address?.restaurant
        ?: address?.fast_food
        ?: address?.cafe
        ?: address?.fuel
        ?: address?.hospital
        ?: address?.school
        ?: place.display_name.split(",").first().trim()
}

// Helper function to build readable address
private fun buildFullAddress(address: NominatimAddress?, fallbackDisplayName: String): String {
    return if (address != null) {
        listOfNotNull(
            address.house_number,
            address.road,
            address.neighbourhood ?: address.suburb,
            address.city ?: address.town ?: address.village,
            address.state,
            address.country
        ).joinToString(", ")
    } else {
        fallbackDisplayName
    }
}

// Helper function to determine place category
private fun determineCategory(place: NominatimPlace, address: NominatimAddress?): PlaceCategory {
    return when {
        address?.restaurant != null -> PlaceCategory.RESTAURANT
        address?.fast_food != null -> PlaceCategory.FAST_FOOD
        address?.cafe != null -> PlaceCategory.CAFE
        address?.shop != null -> PlaceCategory.SHOP
        address?.fuel != null -> PlaceCategory.GAS_STATION
        address?.hospital != null -> PlaceCategory.HOSPITAL
        address?.school != null -> PlaceCategory.SCHOOL
        address?.amenity == "hotel" -> PlaceCategory.HOTEL
        address?.amenity == "bank" -> PlaceCategory.BANK
        place.`class` == "amenity" && place.type == "restaurant" -> PlaceCategory.RESTAURANT
        place.`class` == "amenity" && place.type == "fast_food" -> PlaceCategory.FAST_FOOD
        place.`class` == "amenity" && place.type == "cafe" -> PlaceCategory.CAFE
        place.`class` == "shop" -> PlaceCategory.SHOP
        place.`class` == "amenity" && place.type == "fuel" -> PlaceCategory.GAS_STATION
        place.`class` == "amenity" && place.type == "hospital" -> PlaceCategory.HOSPITAL
        place.`class` == "amenity" && place.type == "school" -> PlaceCategory.SCHOOL
        else -> PlaceCategory.UNKNOWN
    }
}

// Helper function to get current location
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (GeoPoint) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(GeoPoint(it.latitude, it.longitude))
            }
        }
    }
}
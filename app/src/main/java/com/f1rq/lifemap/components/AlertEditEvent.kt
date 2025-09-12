package com.f1rq.lifemap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.f1rq.lifemap.api.NominatimAPI
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.screens.MapView
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun AlertEditEvent(
    onDismissRequest: () -> Unit,
    event: Event,
    onConfirmation: (Event) -> Unit,
    navController: NavController,
    viewModel: EventViewModel = koinViewModel()
) {
    var editedEvent by remember { mutableStateOf(event) }
    var showLocationPicker by remember { mutableStateOf(false) }
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Initialize the viewModel's selectedLocation with the event's location
    LaunchedEffect(event) {
        val eventLocation = if (event.latitude != null && event.longitude != null) {
            GeoPoint(event.latitude, event.longitude)
        } else null
        viewModel.setSelectedLocation(eventLocation)
    }

    // Fullscreen location picker using MapView (EXACT copy from AddEvent)
    if (showLocationPicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .zIndex(Float.MAX_VALUE)
        ) {
            MapView(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                isLocationPickerMode = true,
                onLocationPicked = { location ->
                    viewModel.setSelectedLocation(location)
                    // Fetch location name
                    coroutineScope.launch {
                        try {
                            val nominatimAPI = Retrofit.Builder()
                                .baseUrl("https://nominatim.openstreetmap.org/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                                .create(NominatimAPI::class.java)

                            val locationName = withContext(Dispatchers.IO) {
                                val result = nominatimAPI.reverseGeocode(location.latitude, location.longitude)
                                extractPlaceName(result.display_name)
                            }
                            viewModel.updateLocationName(locationName)
                        } catch (e: Exception) {
                            viewModel.updateLocationName(null)
                        }
                    }
                    showLocationPicker = false
                },
                onCancelLocationPicker = {
                    showLocationPicker = false
                },
                initialPickerLocation = selectedLocation
            )
        }
    }

    // Show dialog only when location picker is not active
    if (!showLocationPicker) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setSelectedLocation(null)
                viewModel.updateLocationName(null)
                onDismissRequest()
            },
            title = { Text("Edit Event") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextInputRow(
                        value = editedEvent.name,
                        onValueChange = { editedEvent = editedEvent.copy(name = it) },
                        label = "Event Name",
                        maxLength = 30,
                        required = true
                    )

                    DateSelectRow(
                        selectedDate = editedEvent.date,
                        onDateSelected = { editedEvent = editedEvent.copy(date = it) }
                    )

                    TextInputRow(
                        value = editedEvent.description,
                        onValueChange = { editedEvent = editedEvent.copy(description = it) },
                        label = "Description",
                        maxLength = 100,
                        required = false
                    )

                    LocationSelectRow(
                        event = editedEvent,
                        selectedLocation = selectedLocation,
                        onLocationSelected = { newLocation ->
                            viewModel.setSelectedLocation(newLocation)
                        },
                        onPickLocationClick = {
                            showLocationPicker = true
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Store selectedLocation in a local variable to avoid smart cast issues
                            val currentSelectedLocation = selectedLocation

                            val locationName = if (currentSelectedLocation != null) {
                                try {
                                    val nominatimAPI = Retrofit.Builder()
                                        .baseUrl("https://nominatim.openstreetmap.org/")
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()
                                        .create(NominatimAPI::class.java)

                                    withContext(Dispatchers.IO) {
                                        val result = nominatimAPI.reverseGeocode(
                                            currentSelectedLocation.latitude,
                                            currentSelectedLocation.longitude
                                        )
                                        extractPlaceName(result.display_name)
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            } else null

                            val updatedEvent = editedEvent.copy(
                                latitude = currentSelectedLocation?.latitude,
                                longitude = currentSelectedLocation?.longitude,
                                locationName = locationName
                            )
                            onConfirmation(updatedEvent)
                            viewModel.setSelectedLocation(null)
                            viewModel.updateLocationName(null)
                        }
                    },
                    enabled = editedEvent.name.isNotBlank()
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.setSelectedLocation(null)
                        viewModel.updateLocationName(null)
                        onDismissRequest()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun extractPlaceName(displayName: String): String {
    val parts = displayName.split(",")
    return when {
        parts.size >= 2 -> "${parts[0].trim()}, ${parts[1].trim()}"
        else -> parts[0].trim()
    }.take(50)
}
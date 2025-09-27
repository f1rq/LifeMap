package com.f1rq.lifemap.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import kotlinx.coroutines.delay
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

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NominatimSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedLocation by remember {
        mutableStateOf(
            if (event.latitude != null && event.longitude != null) {
                GeoPoint(event.latitude, event.longitude)
            } else null
        )
    }
    var selectedLocationName by remember { mutableStateOf(event.locationName) }

    val nominatimAPI = remember {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "LifeMapApp/1.0 (your-email@example.com)")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimAPI::class.java)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            isSearching = true
            try {
                delay(500) // Debounce
                val results = nominatimAPI.searchLocation(searchQuery)
                searchResults = results
            } catch (e: Exception) {
                searchResults = emptyList()
            } finally {
                isSearching = false
            }
        } else {
            searchResults = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Edit Event") },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Location Search
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = selectedLocationName ?: searchQuery,
                                onValueChange = {
                                    if (selectedLocationName != null) {
                                        selectedLocation = null
                                        selectedLocationName = null
                                        searchQuery = it

                                        editedEvent = editedEvent.copy(
                                            latitude = null,
                                            longitude = null,
                                            locationName = null
                                        )
                                    } else {
                                        searchQuery = it
                                    }
                                },
                                label = { Text("Search Location") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty() || selectedLocationName != null) {
                                        IconButton(
                                            onClick = {
                                                searchQuery = ""
                                                selectedLocation = null
                                                selectedLocationName = null

                                                editedEvent = editedEvent.copy(
                                                    latitude = null,
                                                    longitude = null,
                                                    locationName = null
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                readOnly = selectedLocationName != null
                            )
                        }
                    }

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
                }

                // Search results overlay
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty() && selectedLocation == null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 70.dp)
                        .zIndex(1f)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        if (isSearching) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (searchResults.isEmpty() && searchQuery.length >= 3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No results found",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn {
                                items(searchResults) { result ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedLocation = GeoPoint(
                                                    result.lat.toDouble(),
                                                    result.lon.toDouble()
                                                )
                                                selectedLocationName = result.display_name
                                                searchQuery = ""
                                                searchResults = emptyList()
                                                editedEvent = editedEvent.copy(
                                                    latitude = result.lat.toDouble(),
                                                    longitude = result.lon.toDouble(),
                                                    locationName = result.display_name
                                                )
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "Location"
                                        )
                                        Text(
                                            text = result.display_name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmation(editedEvent)
                },
                enabled = editedEvent.name.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
@file:OptIn(ExperimentalMaterial3Api::class)

package com.f1rq.lifemap.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.ui.screens.LocationPickerScreen
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import kotlinx.coroutines.delay
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.f1rq.lifemap.screens.MapView
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@Composable
fun AddEvent(
    onDismiss: () -> Unit,
    viewModel: EventViewModel,
    navController: NavController,
    currentLocation: GeoPoint? = null
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    var showLocationPicker by remember { mutableStateOf(false) }

    // Fullscreen location picker using existing MapView
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
                    showLocationPicker = false
                },
                onCancelLocationPicker = {
                    showLocationPicker = false
                },
                initialPickerLocation = selectedLocation ?: currentLocation
            )
        }
    }

    // Bottom sheet (only show when location picker is not active)
    if (!showLocationPicker) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            AddEventSheetContent(
                onDismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                },
                viewModel = viewModel,
                currentLocation = selectedLocation ?: currentLocation,
                onShowLocationPicker = {
                    showLocationPicker = true
                },
                onLocationChanged = { location ->
                    viewModel.setSelectedLocation(location)
                }
            )
        }
    }
}

@Composable
fun AddEventSheetContent(
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    viewModel: EventViewModel,
    currentLocation: GeoPoint? = null,
    onShowLocationPicker: () -> Unit,
    onLocationChanged: (GeoPoint?) -> Unit
) {
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventDesc by remember { mutableStateOf("") }

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val eventLocation = selectedLocation ?: currentLocation

    val uiState by viewModel.uiState.collectAsState()

    val isFormValid = eventName.isNotBlank()

    LaunchedEffect(uiState.addEventSuccess) {
        if (uiState.addEventSuccess) {
            onDismiss()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            delay(3000)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Add New Event",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Event Name (Required)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextInputRow(
                value = eventName,
                onValueChange = { eventName = it },
                label = "Event Name",
                maxLength = 30,
                modifier = Modifier.weight(1f),
                required = true
            )
        }

        // Date Selection
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateSelectRow(
                selectedDate = eventDate,
                onDateSelected = { eventDate = it },
                modifier = Modifier.weight(1f)
            )
        }

        // Description (Optional)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextInputRow(
                value = eventDesc,
                onValueChange = { eventDesc = it },
                label = "Description",
                maxLength = 100,
                modifier = Modifier.weight(1f),
                required = false
            )
        }

        // Location Selection
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LocationSelectRow(
                selectedLocation = eventLocation,
                onLocationSelected = { newLocation ->
                    viewModel.setSelectedLocation(newLocation)
                },
                onPickLocationClick = onShowLocationPicker
            )
        }

        // Save Button
        Button(
            onClick = {
                val event = Event(
                    name = eventName,
                    date = eventDate,
                    description = eventDesc,
                    latitude = eventLocation?.latitude,
                    longitude = eventLocation?.longitude
                )
                viewModel.addEvent(event)
            },
            enabled = isFormValid && !uiState.isAddingEvent,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (uiState.isAddingEvent) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Add Event")
            }
        }

        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        // Error Message Card (only show errors in the sheet)
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
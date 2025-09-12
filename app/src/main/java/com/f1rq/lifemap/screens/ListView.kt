package com.f1rq.lifemap.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.components.AlertConfirmation
import androidx.compose.ui.text.SpanStyle
import androidx.navigation.NavController
import com.f1rq.lifemap.R
import com.f1rq.lifemap.components.AlertEditEvent
import com.f1rq.lifemap.components.EventInfoSheet

@Composable
fun ListView(
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = koinViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showEventInfo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text (
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            uiState.events.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No events yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MainTextColor
                        )
                        Text(
                            text = "Add your first event to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MainTextColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = 8.dp
                    )
                ) {
                    items(uiState.events) { event ->
                        EventCard(
                            event = event,
                            onDeleteClick = { viewModel.deleteEvent(event) },
                            onEventClick = {
                                selectedEvent = event
                                showEventInfo = true
                            },
                            onEditClick = { updatedEvent ->
                                viewModel.updateEvent(updatedEvent)
                            },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
    if (showEventInfo && selectedEvent != null) {
        EventInfoSheet(
            event = selectedEvent!!,
            onDismiss = {
                showEventInfo = false
                selectedEvent = null
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun EventCard(
    event: Event,
    onEventClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: (Event) -> Unit,
    navController: NavController
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEventClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MainBG,
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MainTextColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
                // Add this after the description text block in EventCard
                if (event.latitude != null && event.longitude != null) {
                    Text(
                        text = formatLocationDisplay(event),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Column {
                Row {
                    IconButton(
                        onClick = { showEditDialog = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit_icon),
                            contentDescription = "Edit Event",
                            tint = MainTextColor
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete_icon),
                            contentDescription = "Delete Event",
                            tint = MainTextColor
                        )
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertConfirmation(
            onDismissRequest = { showDeleteDialog = false },
            onConfirmation = {
                onDeleteClick()
                showDeleteDialog = false
            },
            dialogTitle = "Delete Event",
            dialogText = buildAnnotatedString {
                append("Are you sure you want to delete the event ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(event.name)
                }
                append("?")
            },
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            iconRes = R.drawable.delete_icon,
        )
    }

    if (showEditDialog) {
        AlertEditEvent(
            onDismissRequest = { showEditDialog = false },
            event = event,
            navController = navController,
            onConfirmation = { updatedEvent ->
                onEditClick(updatedEvent)
                showEditDialog = false
            }
        )
    }
}

fun formatLocationDisplay(event: Event): String {
    return when {
        !event.locationName.isNullOrBlank() -> event.locationName
        event.latitude != null && event.longitude != null ->
            "Lat: ${String.format("%.4f", event.latitude)}, Lng: ${String.format("%.4f", event.longitude)}"
        else -> "No location"
    }
}
package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.data.entity.Event
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertEditEvent(
    onDismissRequest: () -> Unit,
    event: Event,
    onConfirmation: (Event) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        EditEventSheetContent(
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            },
            event = event,
            onConfirmation = { updatedEvent ->
                onConfirmation(updatedEvent)
                coroutineScope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            }
        )
    }
}

@Composable
fun EditEventSheetContent(
    onDismiss: () -> Unit,
    event: Event,
    onConfirmation: (Event) -> Unit
) {
    var eventName by remember { mutableStateOf(event.name) }
    var eventDesc by remember { mutableStateOf(event.description) }
    var eventDate by remember { mutableStateOf(event.date) }

    val isFormValid = eventName.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Edit Event",
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        // Event Name
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

        // Description
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
        Button(
            onClick = {
                val updatedEvent = event.copy(
                    name = eventName,
                    description = eventDesc,
                    date = eventDate
                )
                onConfirmation(updatedEvent)
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Save Changes")
        }

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}
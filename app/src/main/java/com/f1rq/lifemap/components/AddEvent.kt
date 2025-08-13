@file:OptIn(ExperimentalMaterial3Api::class)

package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.ui.theme.LifeMapTheme
import com.f1rq.lifemap.ui.theme.PrimaryColor
import com.f1rq.lifemap.data.entity.Event
import kotlinx.coroutines.launch

@Composable
fun AddEvent(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AddEventSheetContent(onDismiss)
    }
}

@Composable
fun AddEventSheetContent(onDismiss: () -> Unit) {
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventDesc by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }

    val isFormValid = eventName.isNotBlank() && eventDate.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
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

        // Date Selection (Required)
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

        // Save Button
        Button(
            onClick = {
                val event = Event(
                    name = eventName,
                    date = eventDate,
                    description = eventDesc
                )
                saveEvent(event, context) { success ->
                    isSaving = false
                    if (success) {
                        saveMessage = "Event saved successfully!"
                        eventName = ""
                        eventDate = ""
                        eventDesc = ""

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(1500)
                            onDismiss()
                        }
                    } else {
                        saveMessage = "Failed to save event. Please try again."
                    }
                }
                isSaving = true
            },
            enabled = isFormValid && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Add Event")
            }
        }

        // Cancel Button
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        // Success/Error Message
        if (saveMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (saveMessage.contains("success"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = saveMessage,
                    modifier = Modifier.padding(12.dp),
                    color = if (saveMessage.contains("success"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

private fun saveEvent(event: Event, context: android.content.Context, onResult: (Boolean) -> Unit) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val database = com.f1rq.lifemap.data.database.AppDatabase.getDatabase(context)
            val repository = com.f1rq.lifemap.data.repository.EventRepository(database.eventDao())

            val eventId = repository.insertEvent(event)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResult(eventId > 0)
            }
        } catch (e: Exception) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResult(false)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventPreview() {
    LifeMapTheme {
        AddEventSheetContent(onDismiss = {})
    }
}
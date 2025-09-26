package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlertEditEvent(
    onDismissRequest: () -> Unit,
    event: Event,
    onConfirmation: (Event) -> Unit,
    navController: NavController,
    viewModel: EventViewModel = koinViewModel()
) {
    var editedEvent by remember { mutableStateOf(event) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
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
package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.screens.formatLocationDisplay
import com.f1rq.lifemap.ui.theme.MainTextColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInfoSheet(
    event: Event,
    onDismiss: () -> Unit,
    viewModel: EventViewModel
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        EventInfoSheetContent(
            event = event,
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
        )
    }
}

@Composable
private fun EventInfoSheetContent(
    event: Event,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Event Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Name",
                style = MaterialTheme.typography.labelMedium,
                color = MainTextColor
            )
            Text(
                text = event.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold

            )
        }

        if (!event.date.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MainTextColor
                )
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }


        if (!event.description.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium,
                    color = MainTextColor
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (!event.category.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MainTextColor
                )
                Text(
                    text = event.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (event.latitude != null && event.longitude != null) {
            Column(
                verticalArrangement =  Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.labelMedium,
                    color = MainTextColor
                )
                Text (
                    text = formatLocationDisplay(event),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold

                )
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
    }
}
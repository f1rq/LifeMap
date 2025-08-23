package com.f1rq.lifemap.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.f1rq.lifemap.components.AddEventCard
import androidx.compose.runtime.*
import com.f1rq.lifemap.components.AddEvent
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun MapView(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = koinViewModel()
) {
    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Text(
            text = "Map view",
            modifier = Modifier.align(Alignment.Center)
        )
        AddEventCard(
            onCreateEventClick = { showSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showSheet) {
            AddEvent(
                onDismiss = { showSheet = false },
                viewModel = viewModel
            )
        }
    }
}
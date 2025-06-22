package com.f1rq.lifemap.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.f1rq.lifemap.components.AddEventCard


@Composable
fun MapView(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Text(
            text = "Map view",
            modifier = Modifier.align(Alignment.Center)
        )
        AddEventCard(
            onCreateEventClick = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Preview
@Composable
fun MapViewPreview() {
    MapView()
}
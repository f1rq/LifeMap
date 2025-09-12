package com.f1rq.lifemap.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.data.entity.Event
import com.f1rq.lifemap.screens.formatLocationDisplay
import com.f1rq.lifemap.ui.theme.MainTextColor
import org.osmdroid.util.GeoPoint

@SuppressLint("DefaultLocale")
@Composable
fun LocationSelectRow(
    event: Event,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint?) -> Unit,
    onPickLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPickLocationClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = com.f1rq.lifemap.R.drawable.location_searching_24px),
                    contentDescription = "Pick Location",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (selectedLocation != null) "Change Location" else "Pick Location",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            if (selectedLocation != null) {
                OutlinedButton(
                    onClick = { onLocationSelected(null) }
                ) {
                    Text("Clear")
                }
            }
        }

        selectedLocation?.let { location ->
            Text(
                text = formatLocationDisplay(event),
                style = MaterialTheme.typography.bodySmall,
                color = MainTextColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
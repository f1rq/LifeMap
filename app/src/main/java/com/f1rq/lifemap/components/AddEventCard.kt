package com.f1rq.lifemap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.theme.PrimaryColor
import com.f1rq.lifemap.R.drawable.add_event_button

@Composable
fun AddEventCard(
    modifier: Modifier = Modifier,
    onCreateEventClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MainBG,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "Add event",
                    style = MaterialTheme.typography.titleMedium,
                    color = MainTextColor
                )
                Text(
                    text = "With localization, date etc.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MainTextColor
                )
            }

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(12.dp),
                        clip = false
                    )
                    .background(
                        color = MainBG,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .size(56.dp)
            ) {
                IconButton(
                    onClick = onCreateEventClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = add_event_button),
                        contentDescription = "Edit",
                        tint = PrimaryColor
                    )
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEventCardPreview() {
    AddEventCard()
}

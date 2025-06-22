package com.f1rq.lifemap.components

import android.graphics.drawable.shapes.Shape
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.theme.PrimaryColor
import com.f1rq.lifemap.R.drawable.notifications_button
import com.f1rq.lifemap.R.drawable.settings_button

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    onNotificationsButtonClick: () -> Unit = {},
    onSettingsButtonClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
    shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MainBG,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 20.0.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    end = 20.0.dp,
                    bottom = 15.0.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = "LifeMap",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    fontWeight = FontWeight(700.0.toInt()),
                    color = MainTextColor,
                )
                Text(
                    text = "Personal life events",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                    color = MainTextColor
                )
            }
            Box(
                modifier = Modifier
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNotificationsButtonClick,
                    ) {
                        Icon(
                            painter = painterResource(id = notifications_button),
                            contentDescription = "Notifications button",
                            tint = PrimaryColor
                        )
                    }
                    IconButton(
                        onClick = onSettingsButtonClick,
                    ) {
                        Icon(
                            painter = painterResource(id = settings_button),
                            contentDescription = "Settings button",
                            tint = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}

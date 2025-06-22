package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.theme.PrimaryColor

@Composable
fun SettingsLabel(
    modifier: Modifier = Modifier,
    text: String,
    iconResource: ImageVector = Icons.Default.Edit,
    desc: String? = null,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MainBG,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconResource,
                contentDescription = desc,
                tint = PrimaryColor
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MainTextColor
            )
        }
    }
}

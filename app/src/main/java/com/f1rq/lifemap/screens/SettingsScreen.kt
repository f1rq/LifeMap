package com.f1rq.lifemap.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.f1rq.lifemap.components.SettingsLabel
import com.f1rq.lifemap.components.ScreenTitle

@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Settings")

        SettingsLabel(
            text = "Notifications",
            iconResource = Icons.Outlined.Notifications,
            desc = "Notifications",
            onClick = { navController.navigate("settings_notifications") }
        )

        SettingsLabel(
            text = "Contact",
            iconResource = Icons.Outlined.Email,
            desc = "Contact us"
        )

        SettingsLabel(
            text = "Report a bug",
            iconResource = Icons.Outlined.Email,
            desc = "Report a bug"
        )
    }
}
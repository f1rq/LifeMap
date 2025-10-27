package com.f1rq.lifemap.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.f1rq.lifemap.components.SettingsLabel
import com.f1rq.lifemap.components.ScreenTitle
import com.f1rq.lifemap.ui.theme.PrimaryColor

@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = { backDispatcher?.onBackPressed() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            ScreenTitle(
                title = "Settings",
                modifier = Modifier.padding(start = 0.dp)
            )
        }

        SettingsLabel(
            text = "Map Theme",
            iconResource = painterResource(com.f1rq.lifemap.R.drawable.map_24px),
            desc = "Select map theme",
            onClick = { navController.navigate("settings_map_theme") }
        )

        SettingsLabel(
            text = "Notifications",
            iconResource = painterResource(com.f1rq.lifemap.R.drawable.notifications_button),
            desc = "Notifications",
            onClick = { navController.navigate("settings_notifications") }
        )

        SettingsLabel(
            text = "Contact",
            iconResource = painterResource(com.f1rq.lifemap.R.drawable.mail_24px),
            desc = "Contact us"
        )

        SettingsLabel(
            text = "Report a bug",
            iconResource = painterResource(com.f1rq.lifemap.R.drawable.bug_report_24px),
            desc = "Report a bug"
        )
    }
}
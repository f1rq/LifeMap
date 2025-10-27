package com.f1rq.lifemap.screens.settingsScreens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.f1rq.lifemap.ui.theme.PrimaryColor
import com.f1rq.lifemap.components.ScreenTitle
import com.f1rq.lifemap.data.MapTheme
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsMapThemeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EventViewModel = koinViewModel()
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val currentTheme by viewModel.mapTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
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
                title = "Map theme",
                modifier = Modifier.padding(start = 0.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MapTheme.values().toList()) { theme ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.setMapTheme(theme) }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = theme == currentTheme,
                        onClick = { viewModel.setMapTheme(theme) }
                    )
                    Text(
                        text = theme.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}

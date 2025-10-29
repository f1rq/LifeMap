package com.f1rq.lifemap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.f1rq.lifemap.data.SortOption
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.MainTextColor
import com.f1rq.lifemap.ui.theme.SecondaryBG
import com.f1rq.lifemap.ui.viewmodel.EventViewModel

@Composable
fun EventListControls(viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.filterText.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    var search by remember { mutableStateOf(currentFilter) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.setFilterText(it)
                },
                placeholder = { Text("Search events") },
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(
                        painterResource(id = com.f1rq.lifemap.R.drawable.sort_24px),
                        contentDescription = "Sort events")
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier
                        .background(MainBG)
                ) {
                    DropdownMenuItem(
                        text = { Text("Newest", color = MainTextColor) },
                        onClick = { viewModel.setSortOption(SortOption.NEWEST); sortMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Oldest", color = MainTextColor) },
                        onClick = { viewModel.setSortOption(SortOption.OLDEST); sortMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Title A‑Z", color = MainTextColor) },
                        onClick = { viewModel.setSortOption(SortOption.TITLE_AZ); sortMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Title Z‑A", color = MainTextColor) },
                        onClick = { viewModel.setSortOption(SortOption.TITLE_ZA); sortMenuExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 8.dp))

        val categories = remember(uiState.events) {
            val fromEvents = uiState.events.mapNotNull { it.category }.distinct()
            fromEvents.ifEmpty { viewModel.eventCategories }
        }

        if (categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val selected = selectedCategories.contains(cat)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleCategory(cat) },
                        label = { Text(cat) },
                        modifier = Modifier
                            .wrapContentWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = viewModel.getCategoryColor(cat),
                            labelColor = MainTextColor
                        ),
                        leadingIcon = {
                            if (selected) {
                                Icon(
                                    painterResource(id = com.f1rq.lifemap.R.drawable.check_24px),
                                    contentDescription = "Selected"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
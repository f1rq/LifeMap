package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.f1rq.lifemap.ui.theme.MainBG
import com.f1rq.lifemap.ui.theme.InactiveNavColor
import com.f1rq.lifemap.ui.theme.ActiveNavColor
import com.f1rq.lifemap.ui.theme.PrimaryColor

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    onMapViewClicked: () -> Unit = {},
    onListViewClicked: () -> Unit = {},
    mapViewBackgroundColor: Color = ActiveNavColor,
    listViewBackgroundColor: Color = InactiveNavColor,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MainBG),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        top = 12.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 6.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onMapViewClicked,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = mapViewBackgroundColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = com.f1rq.lifemap.R.drawable.map_24px),
                            contentDescription = "Map View",
                            modifier = Modifier.requiredSize(30.dp),
                            tint = PrimaryColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onListViewClicked,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = listViewBackgroundColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = com.f1rq.lifemap.R.drawable.list_24px),
                            contentDescription = "List View",
                            modifier = Modifier.requiredSize(30.dp),
                            tint = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}


//@Composable
//fun NavBar(
//    modifier: Modifier = Modifier,
//    onMapViewClicked: () -> Unit = {},
//    onListViewClicked: () -> Unit = {},
//    mapViewBackgroundColor: Color = ActiveNavColor,
//    listViewBackgroundColor: Color = InactiveNavColor,
//) {
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//    ) {
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = MainBG),
//            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
//        ) {
//            Row(
//                modifier = Modifier
//                    .padding(
//                        top = 12.dp,
//                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 6.dp,
//                    ),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .weight(1f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    IconButton(
//                        onClick = onMapViewClicked,
//                        modifier = Modifier
//                            .size(48.dp)
//                            .background(
//                                color = mapViewBackgroundColor,
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                    ) {
//                        Icon(
//                            painter = painterResource(id = navbar_mapview_button),
//                            contentDescription = "Map View",
//                            modifier = Modifier.requiredSize(24.dp),
//                            tint = PrimaryColor
//                        )
//                    }
//                }
//
//                Box(
//                    modifier = Modifier
//                        .weight(1f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    IconButton(
//                        onClick = onListViewClicked,
//                        modifier = Modifier
//                            .size(48.dp)
//                            .background(
//                                color = listViewBackgroundColor,
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                    ) {
//                        Icon(
//                            painter = painterResource(id = navbar_listview_button),
//                            contentDescription = "List View",
//                            modifier = Modifier.requiredSize(24.dp),
//                            tint = PrimaryColor
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
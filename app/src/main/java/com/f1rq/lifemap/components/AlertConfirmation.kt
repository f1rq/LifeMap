package com.f1rq.lifemap.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import com.f1rq.lifemap.ui.theme.MainBG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertConfirmation(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: AnnotatedString,
    confirmButtonText: String,
    dismissButtonText: String,
    @DrawableRes iconRes : Int,
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Icon"
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(dismissButtonText)
            }
        },
        containerColor = MainBG
    )
}
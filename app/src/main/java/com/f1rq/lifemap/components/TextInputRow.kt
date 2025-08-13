package com.f1rq.lifemap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TextInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLength: Int = 20,
    required: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isError = required && value.isEmpty()

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        label = { Text(label + if (required) " *" else "") },
        supportingText = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        required && isError -> "This field is required"
                        required -> "Required"
                        else -> ""
                    },
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${value.length}/$maxLength",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        isError = isError,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun RequiredTextFieldPreview() {
    var text by remember { mutableStateOf("") }

    MaterialTheme {
        TextInputRow(
            value = text,
            onValueChange = { text = it },
            label = "Name",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
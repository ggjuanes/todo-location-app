package com.example.locationtodoappkotlin.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskForm(
    initialTitle: String = "",
    initialDescription: String = "",
    initialLocation: String = "",
    submitLabel: String = "Save",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSubmit: (title: String, description: String, location: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var location by remember(initialLocation) { mutableStateOf(initialLocation) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 100) title = it },
            label = { Text("Title *") },
            singleLine = true,
            supportingText = { Text("${title.length}/100") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 500) description = it },
            label = { Text("Description") },
            minLines = 3,
            maxLines = 5,
            supportingText = { Text("${description.length}/500") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { onSubmit(title, description, location) },
            enabled = !isLoading && title.isNotBlank() && location.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Saving..." else submitLabel)
        }
    }
}

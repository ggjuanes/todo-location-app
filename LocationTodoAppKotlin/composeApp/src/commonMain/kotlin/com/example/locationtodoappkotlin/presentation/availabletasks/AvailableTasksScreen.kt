package com.example.locationtodoappkotlin.presentation.availabletasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtodoappkotlin.presentation.components.EmptyState
import com.example.locationtodoappkotlin.presentation.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableTasksScreen(
    viewModel: AvailableTasksViewModel,
    onEditTask: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Available Tasks") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            // Location status card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!uiState.hasPermission) {
                        Text(
                            text = "Location permission required",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Grant location access to find tasks near you",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = { viewModel.requestPermission() }) {
                            Text("Grant Permission")
                        }
                    } else if (uiState.currentCity != null) {
                        Text(
                            text = "Current location: ${uiState.currentCity}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Button(onClick = { viewModel.fetchLocation() }) {
                            Text("Refresh Location")
                        }
                    } else if (uiState.isLoading) {
                        Text(
                            text = "Getting your location...",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.hasPermission && uiState.currentCity != null && uiState.tasks.isEmpty()) {
                EmptyState(
                    title = "No tasks here",
                    message = "No tasks found for ${uiState.currentCity}"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggleStatus = {},
                            onClick = { onEditTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

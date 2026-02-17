package com.example.locationtodoappkotlin.presentation.alltasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationtodoappkotlin.presentation.components.EmptyState
import com.example.locationtodoappkotlin.presentation.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasksScreen(
    viewModel: AllTasksViewModel,
    onCreateTask: () -> Unit,
    onEditTask: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Tasks") },
                actions = {
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Text(if (uiState.showCompleted) "Hide" else "Show")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, contentDescription = "Create task")
            }
        }
    ) { paddingValues ->
        if (uiState.tasks.isEmpty()) {
            EmptyState(
                title = "No tasks yet",
                message = "Tap the + button to create your first task",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggleStatus = { viewModel.toggleTaskStatus(task.id) },
                        onClick = { onEditTask(task.id) }
                    )
                }
            }
        }
    }
}

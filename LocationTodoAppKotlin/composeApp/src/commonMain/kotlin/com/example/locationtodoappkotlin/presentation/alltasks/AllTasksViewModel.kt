package com.example.locationtodoappkotlin.presentation.alltasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtodoappkotlin.data.repository.TaskRepository
import com.example.locationtodoappkotlin.domain.model.Task
import com.example.locationtodoappkotlin.domain.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AllTasksUiState(
    val tasks: List<Task> = emptyList(),
    val showCompleted: Boolean = true
)

class AllTasksViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _showCompleted = MutableStateFlow(true)
    private val _uiState = MutableStateFlow(AllTasksUiState())
    val uiState: StateFlow<AllTasksUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(repository.getAllTasks(), _showCompleted) { tasks, showCompleted ->
                val filtered = if (showCompleted) tasks
                else tasks.filter { it.status != TaskStatus.COMPLETED }
                AllTasksUiState(tasks = filtered, showCompleted = showCompleted)
            }.collect { _uiState.value = it }
        }
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    fun toggleTaskStatus(taskId: String) {
        viewModelScope.launch { repository.toggleTaskStatus(taskId) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { repository.deleteTask(taskId) }
    }
}

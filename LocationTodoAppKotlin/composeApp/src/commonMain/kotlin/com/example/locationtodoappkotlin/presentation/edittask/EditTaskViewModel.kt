package com.example.locationtodoappkotlin.presentation.edittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtodoappkotlin.data.repository.TaskRepository
import com.example.locationtodoappkotlin.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EditTaskUiState {
    data object Loading : EditTaskUiState()
    data class Editing(val task: Task) : EditTaskUiState()
    data object Success : EditTaskUiState()
    data object Deleted : EditTaskUiState()
    data class Error(val message: String) : EditTaskUiState()
}

class EditTaskViewModel(
    private val repository: TaskRepository,
    private val taskId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditTaskUiState>(EditTaskUiState.Loading)
    val uiState: StateFlow<EditTaskUiState> = _uiState

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            _uiState.value = if (task != null) {
                EditTaskUiState.Editing(task)
            } else {
                EditTaskUiState.Error("Task not found")
            }
        }
    }

    fun updateTask(title: String, description: String, location: String) {
        viewModelScope.launch {
            _uiState.value = EditTaskUiState.Loading
            repository.updateTask(taskId, title, description, location)
                .onSuccess { _uiState.value = EditTaskUiState.Success }
                .onFailure { _uiState.value = EditTaskUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _uiState.value = EditTaskUiState.Deleted
        }
    }
}

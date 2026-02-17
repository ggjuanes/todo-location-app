package com.example.locationtodoappkotlin.presentation.createtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtodoappkotlin.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CreateTaskUiState {
    data object Idle : CreateTaskUiState()
    data object Loading : CreateTaskUiState()
    data object Success : CreateTaskUiState()
    data class Error(val message: String) : CreateTaskUiState()
}

class CreateTaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateTaskUiState>(CreateTaskUiState.Idle)
    val uiState: StateFlow<CreateTaskUiState> = _uiState

    fun createTask(title: String, description: String, location: String) {
        viewModelScope.launch {
            _uiState.value = CreateTaskUiState.Loading
            repository.createTask(title, description, location)
                .onSuccess { _uiState.value = CreateTaskUiState.Success }
                .onFailure { _uiState.value = CreateTaskUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetState() {
        _uiState.value = CreateTaskUiState.Idle
    }
}

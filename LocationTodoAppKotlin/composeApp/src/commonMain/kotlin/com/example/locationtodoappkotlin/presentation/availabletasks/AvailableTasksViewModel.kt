package com.example.locationtodoappkotlin.presentation.availabletasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtodoappkotlin.data.remote.GeocodingApi
import com.example.locationtodoappkotlin.data.repository.TaskRepository
import com.example.locationtodoappkotlin.domain.model.Coordinates
import com.example.locationtodoappkotlin.domain.model.Task
import com.example.locationtodoappkotlin.platform.LocationResult
import com.example.locationtodoappkotlin.platform.LocationService
import com.example.locationtodoappkotlin.platform.PermissionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AvailableTasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val currentCity: String? = null,
    val coordinates: Coordinates? = null,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null
)

class AvailableTasksViewModel(
    private val repository: TaskRepository,
    private val locationService: LocationService,
    private val geocodingApi: GeocodingApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailableTasksUiState())
    val uiState: StateFlow<AvailableTasksUiState> = _uiState

    init {
        checkPermissionAndLoad()
    }

    fun checkPermissionAndLoad() {
        _uiState.value = _uiState.value.copy(hasPermission = locationService.hasPermission())
        if (locationService.hasPermission()) {
            fetchLocation()
        }
    }

    fun requestPermission() {
        viewModelScope.launch {
            val result = locationService.requestPermission()
            _uiState.value = _uiState.value.copy(
                hasPermission = result == PermissionResult.GRANTED,
                errorMessage = if (result == PermissionResult.PERMANENTLY_DENIED)
                    "Location permission permanently denied. Please enable in Settings."
                else null
            )
            if (result == PermissionResult.GRANTED) {
                fetchLocation()
            }
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val locationResult = locationService.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val coords = locationResult.coordinates
                    _uiState.value = _uiState.value.copy(coordinates = coords)

                    geocodingApi.reverseGeocode(coords.latitude, coords.longitude)
                        .onSuccess { cityName ->
                            _uiState.value = _uiState.value.copy(currentCity = cityName)
                            filterTasksByCity(cityName)
                        }
                        .onFailure {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Could not determine city name"
                            )
                        }
                }
                is LocationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = locationResult.message
                    )
                }
            }
        }
    }

    private suspend fun filterTasksByCity(cityName: String) {
        val allTasks = repository.getAllTasks().first()
        val filtered = allTasks.filter {
            it.location.contains(cityName, ignoreCase = true)
        }
        _uiState.value = _uiState.value.copy(
            tasks = filtered,
            isLoading = false
        )
    }
}

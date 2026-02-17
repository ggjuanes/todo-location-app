package com.example.locationtodoappkotlin.platform

import com.example.locationtodoappkotlin.domain.model.Coordinates

enum class PermissionResult {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

sealed class LocationResult {
    data class Success(val coordinates: Coordinates) : LocationResult()
    data class Error(val message: String) : LocationResult()
}

expect class LocationService {
    suspend fun requestPermission(): PermissionResult
    suspend fun getCurrentLocation(): LocationResult
    fun hasPermission(): Boolean
}

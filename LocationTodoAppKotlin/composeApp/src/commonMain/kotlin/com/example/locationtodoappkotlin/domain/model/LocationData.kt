package com.example.locationtodoappkotlin.domain.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class LocationData(
    val coordinates: Coordinates,
    val cityName: String
)

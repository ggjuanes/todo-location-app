package com.example.locationtodoappkotlin.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class GeocodeResponse(
    val address: GeocodeAddress? = null
)

@Serializable
data class GeocodeAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null
)

package com.example.locationtodoappkotlin.data.remote

import com.example.locationtodoappkotlin.BuildConfig
import com.example.locationtodoappkotlin.data.remote.model.GeocodeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class GeocodingApi {

    private val apiKey = BuildConfig.GEOCODE_API_KEY

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val cache = mutableMapOf<String, String>()
    private val rateLimitMutex = Mutex()
    private val timeSource = TimeSource.Monotonic
    private var lastRequestMark = timeSource.markNow()
    private var hasRequested = false

    suspend fun reverseGeocode(lat: Double, lon: Double): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("GEOCODE_API_KEY not configured"))
        }

        val key = "${round(lat * 1000) / 1000},${round(lon * 1000) / 1000}"
        cache[key]?.let { return Result.success(it) }

        return try {
            rateLimitMutex.withLock {
                if (hasRequested) {
                    val elapsed = lastRequestMark.elapsedNow()
                    if (elapsed < 1.seconds) {
                        delay(1.seconds - elapsed)
                    }
                }
                lastRequestMark = timeSource.markNow()
                hasRequested = true
            }

            val response: GeocodeResponse = client.get("https://geocode.maps.co/reverse") {
                parameter("lat", lat)
                parameter("lon", lon)
                parameter("api_key", apiKey)
            }.body()

            val address = response.address
            val cityName = address?.city
                ?: address?.town
                ?: address?.village
                ?: address?.county
                ?: "Unknown location"

            cache[key] = cityName
            Result.success(cityName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

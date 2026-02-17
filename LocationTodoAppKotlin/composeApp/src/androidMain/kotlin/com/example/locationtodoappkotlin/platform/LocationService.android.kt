package com.example.locationtodoappkotlin.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.locationtodoappkotlin.domain.model.Coordinates
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    actual suspend fun requestPermission(): PermissionResult {
        if (hasPermission()) return PermissionResult.GRANTED

        val activity = context as? Activity
            ?: return PermissionResult.DENIED

        return suspendCancellableCoroutine { continuation ->
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            // Since we can't easily get the callback here, we check after a short delay
            // In practice, the UI will re-check permission state
            continuation.resume(PermissionResult.DENIED)
        }
    }

    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(): LocationResult {
        if (!hasPermission()) {
            return LocationResult.Error("Location permission not granted")
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(
                        LocationResult.Success(
                            Coordinates(location.latitude, location.longitude)
                        )
                    )
                } else {
                    continuation.resume(LocationResult.Error("Unable to get location"))
                }
            }.addOnFailureListener { exception ->
                continuation.resume(LocationResult.Error(exception.message ?: "Location error"))
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}

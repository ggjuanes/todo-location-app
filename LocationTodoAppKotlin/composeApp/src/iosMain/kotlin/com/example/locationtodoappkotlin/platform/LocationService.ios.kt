package com.example.locationtodoappkotlin.platform

import com.example.locationtodoappkotlin.domain.model.Coordinates
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationService {

    private val locationManager = CLLocationManager()

    actual suspend fun requestPermission(): PermissionResult {
        if (hasPermission()) return PermissionResult.GRANTED

        val status = locationManager.authorizationStatus
        if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) {
            return PermissionResult.PERMANENTLY_DENIED
        }

        return suspendCancellableCoroutine { continuation ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val newStatus = manager.authorizationStatus
                    if (newStatus != kCLAuthorizationStatusNotDetermined) {
                        val result = when (newStatus) {
                            kCLAuthorizationStatusAuthorizedWhenInUse,
                            kCLAuthorizationStatusAuthorizedAlways -> PermissionResult.GRANTED
                            kCLAuthorizationStatusDenied,
                            kCLAuthorizationStatusRestricted -> PermissionResult.PERMANENTLY_DENIED
                            else -> PermissionResult.DENIED
                        }
                        continuation.resume(result)
                    }
                }
            }
            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): LocationResult {
        if (!hasPermission()) {
            return LocationResult.Error("Location permission not granted")
        }

        return suspendCancellableCoroutine { continuation ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (location != null) {
                        manager.stopUpdatingLocation()
                        val coords = location.coordinate.useContents {
                            Coordinates(latitude, longitude)
                        }
                        continuation.resume(LocationResult.Success(coords))
                    }
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    manager.stopUpdatingLocation()
                    continuation.resume(
                        LocationResult.Error(didFailWithError.localizedDescription)
                    )
                }
            }
            locationManager.delegate = delegate
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()

            continuation.invokeOnCancellation {
                locationManager.stopUpdatingLocation()
            }
        }
    }

    actual fun hasPermission(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
    }
}

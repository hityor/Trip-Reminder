package com.example.tripreminder.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class UserLocationProvider(private val context: Context) {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(forceRefresh: Boolean = false): UserLocation? {
        if (!hasLocationPermission()) return null

        val lastKnownLocation = bestLastKnownLocation()
        if (!forceRefresh && lastKnownLocation != null) {
            return lastKnownLocation.toUserLocation()
        }

        val provider = bestEnabledProvider() ?: return null

        val freshLocation = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                currentLocationFromProvider(provider)
            } else {
                singleLocationUpdate(provider)
            }
        }

        return freshLocation ?: lastKnownLocation?.toUserLocation()
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnownLocation(): Location? =
        locationManager.getProviders(true)
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { location -> location.time }

    private fun bestEnabledProvider(): String? = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocationFromProvider(provider: String): UserLocation? =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            locationManager.getCurrentLocation(
                provider,
                cancellationSignal,
                context.mainExecutor,
            ) { location ->
                if (continuation.isActive) {
                    continuation.resume(location?.toUserLocation())
                }
            }
            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun singleLocationUpdate(provider: String): UserLocation? =
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location.toUserLocation())
                    }
                }
            }

            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            continuation.invokeOnCancellation {
                locationManager.removeUpdates(listener)
            }
        }

    private fun Location.toUserLocation(): UserLocation =
        UserLocation(latitude = latitude, longitude = longitude)

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 5_000L
    }
}

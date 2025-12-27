package com.ensiie.justine.parcoursup.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// https://medium.com/@munbonecci/how-to-get-your-location-in-jetpack-compose-f085031df4c1
class LocationHelper(private val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun areLocationPermissionsGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        if (areLocationPermissionsGranted()) {
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onGetCurrentLocationSuccess(Pair(location.latitude, location.longitude))
                } else {
                    onGetCurrentLocationFailed(Exception("Localisation non disponible. Vérifiez que la localisation est activée."))
                }
            }.addOnFailureListener { exception ->
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit
    ) {
        if (areLocationPermissionsGranted()) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                    }
                }
                .addOnFailureListener { exception ->
                    onGetLastLocationFailed(exception)
                }
        }
    }
}

data class LocationFilter(
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Int,
    val cityName: String? = null
)

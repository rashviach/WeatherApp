package com.android.e1ko0o.weatherapp.utils

import android.Manifest
import android.app.Activity
import android.app.Activity.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class Location(private val activity: Activity) {
    val fusedLocationProvider: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(activity)
    }
    val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }
    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                longitude = location.longitude
                latitude = location.latitude
            }
        }
    }

    var longitude: Double = 0.0
    var latitude: Double = 0.0


    fun isGPSInternetEnabled() {
        val lm = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            AlertDialog.Builder(activity)
                .setMessage("GPS or Internet isn't available. Turn on?")
                .setPositiveButton("Yes") { _, _ ->
                    activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    fun getLocation() {
        fusedLocationProvider.lastLocation
            .addOnSuccessListener { location: Location? ->
                longitude = location?.longitude ?: 0.0
                latitude = location?.latitude ?: 0.0
            }
    }

    fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(activity)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK") { _, _ -> requestLocationPermission() }
                    .create()
                    .show()
            } else
                requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    fun getPermissionCode(): Int = MY_PERMISSIONS_REQUEST_LOCATION

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}
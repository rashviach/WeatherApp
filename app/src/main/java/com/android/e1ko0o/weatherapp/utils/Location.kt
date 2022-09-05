package com.android.e1ko0o.weatherapp.utils

import android.Manifest
import android.app.Activity
import android.app.Activity.*
import com.android.e1ko0o.weatherapp.R
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    private var longitude: Double = 0.0
    private var latitude: Double = 0.0

    fun getLongitude(): Double = longitude
    fun getLatitude(): Double = latitude

    fun isNetworkEnabled(): Boolean {
        val cm = activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            } else {
                AlertDialog.Builder(activity)
                    .setMessage(activity.resources.getText(R.string.network_is_not_available))
                    .setPositiveButton("Yes") { _, _ ->
                        activity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    .setNegativeButton("No", null)
                    .show()
                return true
            }
        }
        return false

    }

    fun isGPSEnabled(): Boolean {
        val lm = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        var gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) {
            AlertDialog.Builder(activity)
                .setMessage(activity.resources.getText(R.string.gps_is_not_available))
                .setPositiveButton("Yes") { _, _ ->
                    activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("No", null)
                .show()
        }
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return gpsEnabled
    }

    fun observeLastLocation() {
        fusedLocationProvider.lastLocation
            .addOnSuccessListener { location: Location? ->
                longitude = location?.longitude ?: -1.0
                latitude = location?.latitude ?: -1.0
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
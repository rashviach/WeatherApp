package com.android.e1ko0o.weatherapp.ui.main.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import by.kirich1409.viewbindingdelegate.viewBinding
import com.android.e1ko0o.weatherapp.R
import com.android.e1ko0o.weatherapp.data.api.ApiHelper
import com.android.e1ko0o.weatherapp.data.api.RetrofitBuilder
import com.android.e1ko0o.weatherapp.databinding.ActivityMainBinding
import com.android.e1ko0o.weatherapp.ui.base.ViewModelFactory
import com.android.e1ko0o.weatherapp.ui.main.viewmodel.MainViewModel
import com.android.e1ko0o.weatherapp.utils.Location
import com.android.e1ko0o.weatherapp.utils.Status.*

private const val ENCRYPTED_PREFS_FILE = "encrypted_storage.txt"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewBinding: ActivityMainBinding by viewBinding()
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[MainViewModel::class.java]
    }

    private val sharedPrefs by lazy { createSharedPreferences() }

    private val location: Location by lazy { Location(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        putKeyInSharedPreferences()

        with(viewBinding) {
            btnSearch.setOnClickListener {
//                    TODO("get text from etCity and send request")
                searchCity()
            }
            btnUseLocation.setOnClickListener {
                location.checkLocationPermission()
                if (location.isNetworkEnabled() && location.isGPSEnabled()) {
                    location.getLocation()
                    searchGPS()
                }
            }
        }
    }

    private fun searchCity() {

    }

    private fun searchGPS() {
        viewModel.getWeather(
            location.getLatitude(),
            location.getLongitude(),
            getKeyFromSharedPreferences(),
            "metric"
        ).observe(this) { response ->
            response?.let { resource ->
                when (resource.status) {
                    SUCCESS -> {
                        resource.data?.let {
                            when (it.weather[0].main) {
                                "Clear" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.clear,
                                        null
                                    )
                                )
                                "Clouds" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.clouds,
                                        null
                                    )
                                )
                                "Thunderstorm", "Extreme" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(R.mipmap.thunderstorm, null)
                                )
                                "Drizzle" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.drizzle,
                                        null
                                    )
                                )
                                "Rain" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.rain,
                                        null
                                    )
                                )
                                "Snow" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.snow,
                                        null
                                    )
                                )
                                "Tornado" -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.tornado,
                                        null
                                    )
                                )
                                else -> viewBinding.ivBackground.setImageDrawable(
                                    resources.getDrawable(
                                        R.mipmap.error,
                                        null
                                    )
                                )
                            }
                        }
                    }
                    ERROR -> {
                        Toast.makeText(
                            this,
                            resources.getText(R.string.error_try_later),
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("MY_TAG", response.message.toString())
                    }
                    LOADING -> {
                        Toast.makeText(
                            this,
                            resources.getText(R.string.loading),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun getKeyFromSharedPreferences(): String = sharedPrefs.getString("AAPID", "") ?: ""

    private fun putKeyInSharedPreferences() {
        sharedPrefs.edit().apply {
            putString("AAPID", "60a22589751faa51d04e0b2f64deb060")
            apply()
        }
    }

    private fun createSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            applicationContext,
            ENCRYPTED_PREFS_FILE,
            MasterKey.Builder(applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            location.fusedLocationProvider.requestLocationUpdates(
                location.locationRequest,
                location.locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            location.fusedLocationProvider.removeLocationUpdates(location.locationCallback)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            location.getPermissionCode() -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        location.fusedLocationProvider.requestLocationUpdates(
                            location.locationRequest,
                            location.locationCallback,
                            Looper.getMainLooper()
                        )
                    }
                } else {
                    Toast.makeText(this, "permission denied - 1", Toast.LENGTH_LONG).show()
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            )
                        )
                    }
                }
                return
            }
        }
    }
}
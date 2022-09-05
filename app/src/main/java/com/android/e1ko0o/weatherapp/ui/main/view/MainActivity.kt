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
import android.view.View
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
import com.android.e1ko0o.weatherapp.data.model.Weather
import com.android.e1ko0o.weatherapp.databinding.ActivityMainBinding
import com.android.e1ko0o.weatherapp.ui.base.ViewModelFactory
import com.android.e1ko0o.weatherapp.ui.main.viewmodel.MainViewModel
import com.android.e1ko0o.weatherapp.utils.Constant
import com.android.e1ko0o.weatherapp.utils.Location
import com.android.e1ko0o.weatherapp.utils.Resource
import com.android.e1ko0o.weatherapp.utils.Status.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewBinding: ActivityMainBinding by viewBinding()
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        )[MainViewModel::class.java]
    }
    private val sharedPrefs by lazy { createSharedPreferences() }
    private val location: Location by lazy { Location(this) }
    private val usedUnits = Constant.UNITS_TYPE_METRIC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        putKeyInSharedPreferences()
        location.observeLastLocation()

        viewBinding.btnSearch.setOnClickListener {
            if (location.isNetworkEnabled()) {
                searchCity(viewBinding.etCity.text.toString())
            }
        }
        viewBinding.btnUseLocation.setOnClickListener {
            location.checkLocationPermission()
            if (location.isNetworkEnabled() && location.isGPSEnabled()) {
                searchGPS()
            }
        }
    }

    private fun searchCity(city: String) {
        viewModel.getWeather(
            city,
            getKeyFromSharedPreferences(),
            usedUnits
        ).observe(this) { response -> handleResponse(response) }
    }

    private fun searchGPS() {
        viewModel.getWeather(
            location.getLatitude(),
            location.getLongitude(),
            getKeyFromSharedPreferences(),
            usedUnits
        ).observe(this) { response -> handleResponse(response) }
    }

    private fun handleMain(main: String) {
        when (main) {
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

    private fun handleTemp(value: Double) {
        val unit = when (usedUnits) {
            Constant.UNITS_TYPE_METRIC -> "C"
            Constant.UNITS_TYPE_IMPERIAL -> "F"
            else -> "K"
        }
        viewBinding.tvTempValue.text = "$value $unit"
        viewBinding.tvTempValue.visibility = View.VISIBLE
        viewBinding.tvTempLabel.visibility = View.VISIBLE
    }

    private fun converthPaTommHg(hPa: Double): Double {
        return hPa * 0.75006157584566
    }

    private fun handlePressure(value: Double) {
        viewBinding.tvPressureValue.text = "${converthPaTommHg(value).toInt()} mm Hg"
        viewBinding.tvPressureValue.visibility = View.VISIBLE
        viewBinding.tvPressureLabel.visibility = View.VISIBLE
    }

    private fun handleWind(value: Double) {
        val unit = when(usedUnits) {
            Constant.UNITS_TYPE_METRIC -> "meter/sec"
            Constant.UNITS_TYPE_IMPERIAL -> "miles/hour"
            else -> "meter/sec"
        }
        viewBinding.tvWindValue.text = "$value $unit"
        viewBinding.tvWindValue.visibility = View.VISIBLE
        viewBinding.tvWindLabel.visibility = View.VISIBLE
    }

    private fun handleLastUpdateTime() {
        viewBinding.tvTimeValue.text = SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
        viewBinding.tvTimeValue.visibility = View.VISIBLE
        viewBinding.tvTimeLabel.visibility = View.VISIBLE
    }

    private fun handleResponse(response: Resource<Weather>) {
        response.let { resource ->
            when (resource.status) {
                SUCCESS -> {
                    resource.data?.let {
                        handleMain(it.weather[0].main)
                        handleTemp(it.main.temp)
                        handlePressure(it.main.pressure)
                        handleWind(it.wind.speed)
                        handleLastUpdateTime()
                    }
                }
                ERROR -> {
                    Toast.makeText(
                        this,
                        resources.getText(R.string.error_try_later),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("MY_TAG", response.message.toString())
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
            Constant.ENCRYPTED_PREFS_FILE,
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
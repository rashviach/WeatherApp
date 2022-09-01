package com.android.e1ko0o.weatherapp.data.repository

import com.android.e1ko0o.weatherapp.data.api.ApiHelper

class MainRepository(private val apiHelper: ApiHelper) {
    suspend fun getWeather(lat: Double, lon: Double, appid: String, units: String) =
        apiHelper.getWeather(lat, lon, appid, units)
}
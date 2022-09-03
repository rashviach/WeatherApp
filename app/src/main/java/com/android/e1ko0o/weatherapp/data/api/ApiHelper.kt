package com.android.e1ko0o.weatherapp.data.api

class ApiHelper(private val apiService: ApiService) {
    suspend fun getWeather(lat: Double, lon: Double, appid: String, units: String) =
        apiService.getWeather(lat, lon, appid, units)

    suspend fun getWeather(city: String, appid: String, units: String) =
        apiService.getWeather(city, appid, units)
}
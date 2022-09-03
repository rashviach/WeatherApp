package com.android.e1ko0o.weatherapp.data.api

import com.android.e1ko0o.weatherapp.data.model.Weather
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Weather

    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Weather
}
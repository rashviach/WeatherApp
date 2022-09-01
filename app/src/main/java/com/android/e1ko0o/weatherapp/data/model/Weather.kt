package com.android.e1ko0o.weatherapp.data.model

data class Weather(
    val coord: CoordSub,
    val weather: List<WeatherSub>,
    val base: String,
    val main: MainSub,
    val visibility: Int,
    val wind: WindSub,
    val clouds: CloudsSub,
    val dt: Long,
    val sys: SysSub,
    val timezone: Int,
    val id: Long,
    val name: String,
    val cod: Int
)

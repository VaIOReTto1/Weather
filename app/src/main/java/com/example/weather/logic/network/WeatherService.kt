package com.example.weather.logic.network

import com.example.weather.WeatherApplication
import com.example.weather.logic.model.AlterResponse
import com.example.weather.logic.model.DailyResponse
import com.example.weather.logic.model.HourlyResponse
import com.example.weather.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {

    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<RealtimeResponse>

    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/hourly?hourlysteps=24")
    fun getHourlyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<HourlyResponse>

    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/daily?dailysteps=15")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<DailyResponse>

    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/realtime?alert=true")
    fun getAlterWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<AlterResponse>
}
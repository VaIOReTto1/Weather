package com.example.weather.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.example.weather.WeatherApplication
import com.example.weather.logic.model.Place
import com.google.gson.Gson

object PlaceDao {
    private fun sharedPreferences()=WeatherApplication.context.getSharedPreferences("weather",Context.MODE_PRIVATE)

    fun isPlaceSaved()= sharedPreferences().contains("place")

    fun getSavedPlace():Place{
        val placeJson= sharedPreferences().getString("place","")
        return Gson().fromJson(placeJson,Place::class.java)
    }

    fun savePlace(place: Place){
        sharedPreferences().edit {
            putString("place",Gson().toJson(place))
        }
    }
}
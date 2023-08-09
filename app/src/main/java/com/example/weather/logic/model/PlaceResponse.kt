package com.example.weather.logic.model

import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.weather.R
import com.example.weather.logic.network.WeatherNetwork
import com.google.gson.annotations.SerializedName

data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(val name: String, val location: Location, @SerializedName("formatted_address") val address: String)

data class Location(val lng: String, val lat: String)
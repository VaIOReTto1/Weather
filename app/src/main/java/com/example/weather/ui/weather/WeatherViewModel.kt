package com.example.weather.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.weather.logic.Repository
import com.example.weather.logic.model.Location

class WeatherViewModel:ViewModel() {
    var locationLng=""

    var locationLat=""

    var placeName=""
}
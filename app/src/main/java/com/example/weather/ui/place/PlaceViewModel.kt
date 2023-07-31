package com.example.weather.ui.place

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.weather.logic.Repository
import com.example.weather.logic.dao.PlaceDao
import com.example.weather.logic.model.Place

class PlaceViewModel:ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val placeList=ArrayList<Place>()

    val placeLiveData=searchLiveData.switchMap{ query ->
        Repository.searchPlaces(query)
    }

    fun searchPlacces(query:String){
        searchLiveData.value=query
    }

    fun savePlace(place: Place)= Repository.savePlace(place)

    fun getSavePlace()= Repository.getSavePlace()

    fun isPlaceSaved()= Repository.isPlaceSaved()
}
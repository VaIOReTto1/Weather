package com.example.weather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log

class WeatherApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN="WYbvhFwiNABvwXDg"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}
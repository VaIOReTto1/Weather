package com.example.weather

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weather.logic.PlaceDatabase

class MainActivity : AppCompatActivity() {
    var open=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val placeDatabaseHelper = PlaceDatabase(this)
        open= intent.getStringExtra("open")?: ""
        val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
        if (historyPlaces.isNotEmpty())
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Handle the back navigation action
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
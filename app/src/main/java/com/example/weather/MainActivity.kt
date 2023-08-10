package com.example.weather

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.weather.logic.PlaceDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    var open = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("com.example.weather.PREFERENCES", Context.MODE_PRIVATE)
        open = sharedPreferences.getString("open", "") ?: ""

        if (open.isEmpty()) {
            open = ""
            val editor = sharedPreferences.edit()
            editor.putString("open", "1")
            editor.apply()
        } else {
            open = intent.getStringExtra("open")?:"1"
        }

        Log.d("MainActivity",open)

        val placeDatabaseHelper = PlaceDatabase(this)
        val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
        if (historyPlaces.isNotEmpty()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    // Handle the back navigation action
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

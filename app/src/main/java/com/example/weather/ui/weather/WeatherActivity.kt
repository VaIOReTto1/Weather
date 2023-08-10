package com.example.weather.ui.weather

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.logic.PlaceDatabase
import com.example.weather.logic.model.Place
import com.example.weather.logic.model.Weather
import com.example.weather.logic.network.WeatherNetwork
import com.example.weather.ui.place.PlaceFragment
import kotlinx.coroutines.launch

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewpager)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val textView: TextView = findViewById(R.id.placeName)
        val placeDatabaseHelper = PlaceDatabase(this)
        val historyPlaces = placeDatabaseHelper.getHistoryPlaces()

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val indicatorRecyclerView: RecyclerView = findViewById(R.id.indicatorRecyclerView)

        // Create the WeatherPagerAdapter
        val weatherAdapter = WeatherPagerAdapter(this)
        viewPager.adapter = weatherAdapter

        // Set the LinearLayoutManager for the indicatorRecyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorRecyclerView.layoutManager = layoutManager

        // Create and set the IndicatorAdapter
        val indicatorAdapter = IndicatorAdapter(weatherAdapter.itemCount)
        indicatorRecyclerView.adapter = indicatorAdapter

        val addButton: ImageButton =findViewById(R.id.addButton)
        addButton.setOnClickListener {
            intent= Intent(this,MainActivity::class.java).apply {
                putExtra("open", "0")
            }
            startActivity(intent)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                indicatorAdapter.setSelectedPosition(position)
                textView.text = historyPlaces[position].name
            }
        })
    }
}

class IndicatorAdapter(private val pageCount: Int) : RecyclerView.Adapter<IndicatorViewHolder>() {
    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndicatorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tab_item, parent, false)
        return IndicatorViewHolder(view)
    }

    override fun onBindViewHolder(holder: IndicatorViewHolder, position: Int) {
        holder.bind(position == selectedPosition)
    }

    override fun getItemCount(): Int = pageCount

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}

class IndicatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(selected: Boolean) {
        // Set circle indicator background
        val drawableRes =
            if (selected) R.drawable.circle_selected_shape else R.drawable.circle_unselected_shape
        itemView.setBackgroundResource(drawableRes)
    }
}

class WeatherPagerAdapter(private val activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {
    val placeDatabaseHelper = PlaceDatabase(activity)
    val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
    override fun getItemCount(): Int = historyPlaces.size
    override fun createFragment(position: Int): Fragment {
        if (position < 0 || position >= historyPlaces.size) {
            throw IllegalArgumentException("Invalid position: $position")
        }

        return createWeatherFragment(historyPlaces[position])
    }

    private suspend fun fetchWeather(place: Place): Weather? {
        return try {
            val deferredRealtime = WeatherNetwork.getRealtimeWeather(place.location.lng, place.location.lat)
            val deferredDaily = WeatherNetwork.getDailyWeather(place.location.lng, place.location.lat)
            val deferredHour = WeatherNetwork.getHourlyWeather(place.location.lng, place.location.lat)
            if (deferredRealtime.status == "ok" && deferredDaily.status == "ok") {
                Log.d("WeatherActivity",place.name)
                Weather(
                    deferredRealtime.result.realtime,
                    deferredDaily.result.daily,
                    deferredHour.result.hourly
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createWeatherFragment(place: Place): WeatherFragment {
        val weatherFragment = WeatherFragment(place)

        activity.lifecycleScope.launch {
            val weather = fetchWeather(place)
            if (weather != null) {
                weatherFragment.updateWeather(weather)
            }
        }
        return weatherFragment
    }
}
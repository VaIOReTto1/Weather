package com.example.weather.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.launch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class WeatherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viewpager)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val textView: TextView = findViewById(R.id.placeName)
        val placeDatabaseHelper = PlaceDatabase(this)
        val historyPlaces = placeDatabaseHelper.getHistoryPlaces()

        //创建viewpager
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val indicatorRecyclerView: RecyclerView = findViewById(R.id.indicatorRecyclerView)
        val weatherAdapter = WeatherPagerAdapter(this)
        viewPager.adapter = weatherAdapter
        val intentPosition = intent.getIntExtra("position", 0)

        //创建指示标
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorRecyclerView.layoutManager = layoutManager
        val indicatorAdapter = IndicatorAdapter(weatherAdapter.itemCount,intentPosition)
        indicatorRecyclerView.adapter = indicatorAdapter
        // 设置初始选中页面
        textView.text = historyPlaces[intentPosition].name
        viewPager.setCurrentItem(intentPosition, false)
        //获取位置更新toolbar标题
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                indicatorAdapter.setSelectedPosition(position)
                textView.text = historyPlaces[position].name
            }
        })

        //进入搜索页面
        val addButton: ImageButton =findViewById(R.id.addButton)
        addButton.setOnClickListener {
            intent= Intent(this,MainActivity::class.java).apply {
                putExtra("open", "0")
            }
            startActivity(intent)
        }
    }


}

class IndicatorAdapter(private val pageCount: Int, position: Int) : RecyclerView.Adapter<IndicatorViewHolder>() {
    private var selectedPosition = position

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
    private val placeDatabaseHelper = PlaceDatabase(activity)
    private val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
    override fun getItemCount(): Int = historyPlaces.size
    override fun createFragment(position: Int): Fragment {
        if (position < 0 || position >= historyPlaces.size) {
            throw IllegalArgumentException("Invalid position: $position")
        }

        return createWeatherFragment(historyPlaces[position])
    }

    //获取天气数据
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
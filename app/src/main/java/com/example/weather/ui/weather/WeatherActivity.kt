package com.example.weather.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weather.R
import com.example.weather.logic.PlaceDatabase
import com.example.weather.logic.model.HourlyForecast
import com.example.weather.logic.model.Weather
import com.example.weather.logic.model.getSky
import com.example.weather.ui.place.PlaceAdapter
import com.example.weather.ui.place.PlaceFragment
import com.example.weather.ui.place.PlaceHistoryAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }

    private val hourlyForecastList = ArrayList<HourlyForecast>()

    private lateinit var hourlyAdapter: HourlyAdapter
    lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_weather)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        val hourRecyclerView: RecyclerView =findViewById(R.id.hourRecyclerView)
        hourRecyclerView.layoutManager = layoutManager
        hourlyAdapter = HourlyAdapter(hourlyForecastList)
        hourRecyclerView.adapter = hourlyAdapter

        lineChart= findViewById(R.id.lineChart)


        val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.swipeRefresh)

        if (viewModel.locationLng.isEmpty())
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""

        if (viewModel.locationLat.isEmpty())
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""

        if (viewModel.placeName.isEmpty())
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""

        viewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null)
                showWeatherInfo(weather)
            else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }

            swipeRefresh.isRefreshing = false
        }
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        hourRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 计算滚动的偏移量
                // 将NestedScrollView滚动偏移量累加
                lineChart.scrollBy(dx, 0)
            }
        })

        val navBtn: Button = findViewById(R.id.navBtn)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                val placeDatabaseHelper=PlaceDatabase(this@WeatherActivity)
                val placeFragment = supportFragmentManager.findFragmentById(R.id.placeFragment) as PlaceFragment
                val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
                val historyAdapter = PlaceHistoryAdapter(placeFragment, historyPlaces)
                placeFragment.updateRecyclerViewAdapter(historyAdapter)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDrawerClosed(drawerView: View) {
                refreshWeather()
                val placeDatabaseHelper=PlaceDatabase(this@WeatherActivity)
                val placeFragment = supportFragmentManager.findFragmentById(R.id.placeFragment) as PlaceFragment
                val historyPlaces = placeDatabaseHelper.getHistoryPlaces()
                val historyAdapter = PlaceHistoryAdapter(placeFragment, historyPlaces)
                Handler(Looper.getMainLooper()).postDelayed({
                    placeFragment.updateRecyclerViewAdapter(historyAdapter)
                }, 100)
                refreshWeather()
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })
    }

    fun refreshWeather() {
        Log.d("WeatherActivity",viewModel.locationLng+","+viewModel.locationLat)
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.swipeRefresh)
        swipeRefresh.isRefreshing = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showWeatherInfo(weather: Weather) {
        val placeName: TextView = findViewById(R.id.placeName)
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        val currentTempText = "${realtime.temperature.toInt()} ℃"
        val currentTemp: TextView = findViewById(R.id.currentTemp)
        currentTemp.text = currentTempText
        val currentSky: TextView = findViewById(R.id.currentSky)
        currentSky.text = getSky(realtime.skycon).info

        val currentPM25TEXT = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        val currentAQI: TextView = findViewById(R.id.currentAQI)
        currentAQI.text = currentPM25TEXT

        val nowLayout: RelativeLayout = findViewById(R.id.nowLayout)
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        val forecastLayout: LinearLayout = findViewById(R.id.forcecastLayout)

        hourlyForecastList.clear()
        val hourly = weather.hourly
        val hours = hourly.skycon.size
        for (i in 0 until hours) {
            val temVal = hourly.temperature[i].value
            val skyVal = hourly.skycon[i].value
            val datetime = hourly.skycon[i].datetime
            hourlyForecastList.add(HourlyForecast(temVal, skyVal, datetime))
        }
        hourlyAdapter.notifyDataSetChanged()

        setupLineChart(lineChart)
        updateLineChartData(lineChart)

        //解决重复加载预报天数问题
        forecastLayout.removeAllViews()

        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_item,
                forecastLayout, false
            )
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfor) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }
        val lifeIndex = daily.lifeIndex
        val coldRiskText: TextView = findViewById(R.id.coldRiskText)
        val dressingText: TextView = findViewById(R.id.dressingText)
        val ultravioletText: TextView = findViewById(R.id.ultravioletText)
        val carWashingText: TextView = findViewById(R.id.carWashingText)
        val humidity:TextView=findViewById(R.id.humidityText)
        val apprentTemperature:TextView=findViewById(R.id.apparentTemperatureText)
        val weatherLayout: ScrollView = findViewById(R.id.weatherLayout)

        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        humidity.text=realtime.humidity
        apprentTemperature.text=realtime.apparent_temperature
        weatherLayout.visibility = View.VISIBLE
    }

    private fun setupLineChart(lineChart: LineChart) {
        lineChart.description = Description().apply { text = "" }
        lineChart.xAxis.isEnabled = false
        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.setDrawGridBackground(false)
        lineChart.isDragEnabled = true
        lineChart.setTouchEnabled(false)
        lineChart.setScaleEnabled(true) // Allow scaling
        lineChart.setPinchZoom(true)
    }

    private fun updateLineChartData(lineChart: LineChart) {
        val entries = ArrayList<Entry>()
        for ((index, forecast) in hourlyForecastList.withIndex()) {
            entries.add(Entry(index.toFloat(), forecast.temVal))
        }

        val dataSet = LineDataSet(entries, "Temperature")
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.valueTextSize = 12f

        val lineDataSets = ArrayList<ILineDataSet>()
        lineDataSets.add(dataSet)

        val data = LineData(lineDataSets)
        lineChart.data = data

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }
        dataSet.setDrawIcons(false)

        lineChart.legend.isEnabled = false

        lineChart.moveViewToX(hourlyForecastList.size.toFloat())

        lineChart.invalidate()
    }
}


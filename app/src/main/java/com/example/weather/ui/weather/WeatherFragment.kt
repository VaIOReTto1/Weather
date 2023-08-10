package com.example.weather.ui.weather

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.logic.model.HourlyForecast
import com.example.weather.logic.model.Place
import com.example.weather.logic.model.Weather
import com.example.weather.logic.model.getSky
import com.example.weather.logic.network.WeatherNetwork
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherFragment(private val place: Place) : Fragment() {
    private var load=0
    private lateinit var weather: Weather

    private val hourlyForecastList = ArrayList<HourlyForecast>()
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        val hourRecyclerView: RecyclerView = view.findViewById(R.id.hourRecyclerView)
        hourRecyclerView.layoutManager = layoutManager
        hourlyAdapter = HourlyAdapter(hourlyForecastList)
        hourRecyclerView.adapter = hourlyAdapter
        lineChart = view.findViewById(R.id.lineChart)

        //判断是否再viewpager中加载完毕
        if (load==1)
            showWeatherInfo(weather)

        hourRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // 将NestedScrollView滚动偏移量累加
                lineChart.scrollBy(dx, 0)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showWeatherInfo(weather: Weather) {
        val realtime = weather.realtime
        val daily = weather.daily

        val currentTempText = "${realtime.temperature.toInt()} ℃"
        val currentTemp: TextView = view?.findViewById(R.id.currentTemp) ?: return
        currentTemp.text = currentTempText
        val currentSky: TextView = view?.findViewById(R.id.currentSky) ?: return
        currentSky.text = getSky(realtime.skycon).info

        val currentPM25TEXT = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        val currentAQI: TextView = view?.findViewById(R.id.currentAQI) ?: return
        currentAQI.text = currentPM25TEXT

        val nowLayout: RelativeLayout = view?.findViewById(R.id.nowLayout) ?: return
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        val forecastLayout: LinearLayout = view?.findViewById(R.id.forcecastLayout) ?: return

        //24小时
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
//15天
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(requireContext()).inflate(
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

        //生活指数
        val lifeIndex = daily.lifeIndex
        val coldRiskText: TextView = view?.findViewById(R.id.coldRiskText) ?: return
        val dressingText: TextView = view?.findViewById(R.id.dressingText) ?: return
        val ultravioletText: TextView = view?.findViewById(R.id.ultravioletText) ?: return
        val carWashingText: TextView = view?.findViewById(R.id.carWashingText) ?: return
        val humidity: TextView = view?.findViewById(R.id.humidityText) ?: return
        val apprentTemperature: TextView =
            view?.findViewById(R.id.apparentTemperatureText) ?: return
        val weatherLayout: ScrollView = view?.findViewById(R.id.weatherLayout) ?: return

        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        humidity.text = realtime.humidity
        apprentTemperature.text = realtime.apparent_temperature
        weatherLayout.visibility = View.VISIBLE

        //预警
        lifecycleScope.launch {
            try {
                val alterButton: Button = view?.findViewById(R.id.alterButton) ?: return@launch
                alterButton.visibility = View.GONE
                val deferredAlter =
                    WeatherNetwork.getAlterWeather(place.location.lng, place.location.lat)
                if (deferredAlter.result.alert?.content != null) {
                    val title = deferredAlter.result.alert.content[0]?.title
                    val description = deferredAlter.result.alert.content[0]?.description
                    alterButton.visibility = View.GONE
                    alterButton.text = title
                    alterButton.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setTitle(title)
                        builder.setMessage("\n" + description)
                        builder.setPositiveButton("OK", null)
                        val dialog = builder.create()
                        dialog.show()
                    }
                    alterButton.visibility = View.VISIBLE
                }

            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupLineChart(lineChart: LineChart) {
        //定义折线图
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
        //生产折线图
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

    //更新weather
    fun updateWeather(newWeather: Weather) {
        weather = newWeather
        load=1
    }
}
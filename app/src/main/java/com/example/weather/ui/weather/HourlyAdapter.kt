package com.example.weather.ui.weather

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.logic.model.HourlyForecast
import com.example.weather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class HourlyAdapter(private val hourlyForecastList: List<HourlyForecast>): RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val hourTemperatureInfo: TextView = view.findViewById(R.id.hourTemperatureInfo)
        val hourSkyIcon: ImageView = view.findViewById(R.id.hourSkyIcon)
        val hourDateInfo: TextView = view.findViewById(R.id.hourDateInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hourly_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return hourlyForecastList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hourlyForecast = hourlyForecastList[position]
        val hourTemperatureInfoText = hourlyForecast.temVal.toInt()
        holder.hourTemperatureInfo.text = "${hourTemperatureInfoText}°"
        val sky = getSky(hourlyForecast.skyVal)
        holder.hourSkyIcon.setImageResource(sky.icon)
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.hourDateInfo.text = simpleDateFormat.format(hourlyForecast.datetime)
    }
}
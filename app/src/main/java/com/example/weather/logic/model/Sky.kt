package com.example.weather.logic.model

import com.example.weather.R

class Sky(val info:String,val icon:Int,val bg:Int)

private val sky= mapOf(
    "CLEAR_DAY" to Sky("晴", R.mipmap.ic_clear_day, R.mipmap.bg_clear_day),
    "CLEAR_NIGHT" to Sky("晴", R.mipmap.ic_clear_night, R.mipmap.bg_clear_night),
    "PARTLY_CLOUDY_DAY" to Sky("多云", R.mipmap.ic_partly_cloud_day,
        R.mipmap.bg_partly_cloudy_day),
    "PARTLY_CLOUDY_NIGHT" to Sky("多云", R.mipmap.ic_partly_cloud_night,
        R.mipmap.bg_partly_cloudy_night),
    "CLOUDY" to Sky("阴", R.mipmap.ic_cloudy, R.mipmap.bg_cloudy),
    "WIND" to Sky("大风", R.mipmap.ic_cloudy, R.mipmap.bg_wind),
    "LIGHT_RAIN" to Sky("小雨", R.mipmap.ic_light_rain, R.mipmap.bg_rain),
    "MODERATE_RAIN" to Sky("中雨", R.mipmap.ic_moderate_rain, R.mipmap.bg_rain),
    "HEAVY_RAIN" to Sky("大雨", R.mipmap.ic_heavy_rain, R.mipmap.bg_rain),
    "STORM_RAIN" to Sky("暴雨", R.mipmap.ic_storm_rain, R.mipmap.bg_rain),
    "THUNDER_SHOWER" to Sky("雷阵雨", R.mipmap.ic_thunder_shower, R.mipmap.bg_rain),
    "SLEET" to Sky("雨夹雪", R.mipmap.ic_sleet, R.mipmap.bg_rain),
    "LIGHT_SNOW" to Sky("小雪", R.mipmap.ic_light_snow, R.mipmap.bg_snow),
    "MODERATE_SNOW" to Sky("中雪", R.mipmap.ic_moderate_snow, R.mipmap.bg_snow),
    "HEAVY_SNOW" to Sky("大雪", R.mipmap.ic_heavy_snow, R.mipmap.bg_snow),
    "STORM_SNOW" to Sky("暴雪", R.mipmap.ic_heavy_snow, R.mipmap.bg_snow),
    "HAIL" to Sky("冰雹", R.mipmap.ic_hail, R.mipmap.bg_snow),
    "LIGHT_HAZE" to Sky("轻度雾霾", R.mipmap.ic_light_haze, R.mipmap.bg_fog),
    "MODERATE_HAZE" to Sky("中度雾霾", R.mipmap.ic_moderate_haze, R.mipmap.bg_fog),
    "HEAVY_HAZE" to Sky("重度雾霾", R.mipmap.ic_heavy_haze, R.mipmap.bg_fog),
    "FOG" to Sky("雾", R.mipmap.ic_fog, R.mipmap.bg_fog),
    "DUST" to Sky("浮尘", R.mipmap.ic_fog, R.mipmap.bg_fog)
)

fun getSky(skycon:String):Sky{
    return sky[skycon]?: sky["CLEAR_DAY"]!!
}
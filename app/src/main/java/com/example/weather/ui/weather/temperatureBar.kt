package com.example.weather.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.LinearGradient
import android.util.AttributeSet
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class TemperatureChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // 定义属性，用于设置数据和状态
    private var min: Int = -10
    private var max: Int = 10
    private var currentMin: Int = -10
    private var currentMax: Int = 10
    private var currentTemperature: Int = 0
    private var currentTemperaturePercentageVisible:Boolean=false
    private var currentMinColor = getTemperatureColor(currentMin)// 这里应该是你的颜色转换逻辑
    private var currentMaxColor = getTemperatureColor(currentMax) // 这里应该是你的颜色转换逻辑
    private var num = max - min

    private val gradientShader: Shader = LinearGradient(
        (width / num.toFloat()) * (currentMin - min), 0f,
        (width / num.toFloat()) * (currentMax - min), 0f,
        intArrayOf(currentMinColor.toArgb(), currentMaxColor.toArgb()),
        null,
        Shader.TileMode.CLAMP
    )

    fun setCurrentTemperature(
        currentTemperature: Int,
        min: Int,
        max: Int,
        currentMin: Int,
        currentMax: Int,
        currentTemperaturePercentageVisible:Boolean
    ) {
        this.min=min
        this.max=max
        this.currentMin=currentMin
        this.currentMax=currentMax
        this.currentTemperature=currentTemperature
        this.currentTemperaturePercentageVisible=currentTemperaturePercentageVisible
        this.num = max - min
        currentMinColor = getTemperatureColor(currentMin)
        currentMaxColor = getTemperatureColor(currentMax)
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            //创建曲线
            val linePaint = Paint().apply {
                style = Paint.Style.STROKE
            }

            // 绘制背景灰色线条
            drawLine(
                0f, 0f, width.toFloat(), 0f,
                linePaint.apply {
                    color = android.graphics.Color.GRAY
                    strokeWidth = 24f
                }
            )

            // 计算渐变线条的起点和终点位置
            val gradientStartX = (width / num.toFloat()) * (currentMin - min)
            val gradientEndX = (width / num.toFloat()) * (currentMax - min)

            // 创建渐变着色器
            val gradientShader = LinearGradient(
                gradientStartX, 0f, gradientEndX, 0f,
                currentMinColor.toArgb(), currentMaxColor.toArgb(),
                Shader.TileMode.CLAMP
            )

            // 绘制渐变温度线条
            drawLine(gradientStartX, 0f, gradientEndX, 0f, linePaint.apply {
                shader = gradientShader
                strokeWidth = 24f
            })

            if (currentTemperaturePercentageVisible){
                val dotX = width / num * (currentTemperature - min)

                // 设置圆点的颜色
                val dotPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    style = Paint.Style.FILL
                }

                // 绘制小白圆点
                drawCircle(dotX.toFloat(), 6f, 7f, dotPaint) // 调整圆点半径为您想要的大小
            }
        }
    }
}

//温度颜色获取
private fun getTemperatureColor(temperature: Int): androidx.compose.ui.graphics.Color {
    return if (temperature < -20) {
        Color(red = 26, green = 92, blue = 249)
    } else if (temperature < -15) {
        Color(red = 16, green = 103, blue = 255)
    } else if (temperature < -10) {
        Color(red = 28, green = 122, blue = 254)
    } else if (temperature < -5) {
        Color(red = 52, green = 151, blue = 229)
    } else if (temperature < 0) {
        Color(red = 65, green = 174, blue = 250)
    } else if (temperature < 5) {
        Color(red = 86, green = 201, blue = 205)
    } else if (temperature < 10) {
        Color(red = 86, green = 203, blue = 299)
    } else if (temperature < 15) {
        Color(red = 151, green = 201, blue = 142)
    } else if (temperature < 20) {
        Color(red = 247, green = 196, blue = 34)
    } else if (temperature < 25) {
        Color(red = 209, green = 123, blue = 11)
    } else if (temperature < 30) {
        Color(red = 253, green = 138, blue = 11)
    } else {
        Color(red = 248, green = 60, blue = 30)
    }
}
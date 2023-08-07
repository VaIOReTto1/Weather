package com.example.weather.logic.model

class AlterResponse(val result: Result) {
    data class Result( val alert: Alert?)

    data class Alert(val content: List<Content?>)

    data class Content(val title: String, val description: String)
}
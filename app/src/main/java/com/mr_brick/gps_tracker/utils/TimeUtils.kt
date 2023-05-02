package com.mr_brick.gps_tracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")

    fun getTime(timeInMillis: Long) : String{
        val dateTime = Calendar.getInstance()
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        dateTime.timeInMillis = timeInMillis
        return timeFormatter.format(dateTime.time)
    }

}
package com.mr_brick.gps_tracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtils {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm")

    fun getTime(timeInMillis: Long) : String{
        val dateTime = Calendar.getInstance()
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        dateTime.timeInMillis = timeInMillis
        return timeFormatter.format(dateTime.time)
    }

    fun getDate(): String{
        val cv = Calendar.getInstance()
        return dateFormatter.format(cv.time)
    }

}
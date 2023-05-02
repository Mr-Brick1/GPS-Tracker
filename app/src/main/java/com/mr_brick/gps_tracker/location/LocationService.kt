package com.mr_brick.gps_tracker.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.mr_brick.gps_tracker.MainActivity
import com.mr_brick.gps_tracker.R

class LocationService : Service() {

    private var distance = 0.0f
    private var lastLocation : Location? = null
    private lateinit var locProvider : FusedLocationProviderClient
    private lateinit var locRequest : LocationRequest

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        startLocationUpdates()
        isRunning = true
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        initLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locCallBack)
    }

    private val locCallBack = object : LocationCallback(){
        override fun onLocationResult(lResult: LocationResult) {
            super.onLocationResult(lResult)
            val currentLocation = lResult.lastLocation
            if(lastLocation != null && currentLocation != null){
                if(currentLocation.speed > 0.2)distance += lastLocation?.distanceTo((currentLocation))!!
            }
            lastLocation = currentLocation
            Log.d("MyLog", "Distance: $distance")

        }
    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifChanel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notifManager =
                getSystemService(NotificationManager::class.java) as NotificationManager
            notifManager.createNotificationChannel(notifChanel)
        }

        val notifIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            10,
            notifIntent,
            0
        )

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker running!")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(99, notification)
    }

    private fun initLocation(){
        locRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY,5000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        locProvider.requestLocationUpdates(
            locRequest,
            locCallBack,
            Looper.myLooper()
        )
    }

    companion object {
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
        var startTime = 0L
    }





















}
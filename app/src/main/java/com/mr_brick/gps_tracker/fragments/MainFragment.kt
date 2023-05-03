package com.mr_brick.gps_tracker.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mr_brick.gps_tracker.MainViewModel
import com.mr_brick.gps_tracker.R
import com.mr_brick.gps_tracker.databinding.FragmentMainBinding
import com.mr_brick.gps_tracker.location.LocationModel
import com.mr_brick.gps_tracker.location.LocationService
import com.mr_brick.gps_tracker.utils.DialogManager
import com.mr_brick.gps_tracker.utils.TimeUtils
import com.mr_brick.gps_tracker.utils.checkPermisson
import com.mr_brick.gps_tracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MainFragment : Fragment() {

    private var pl : Polyline? = null

    private var timer : Timer? = null // Таймер
    private var startTime = 0L // Время старта Таймера

    private var isServiceRunning: Boolean = false // Запущен ли сервис
    private var firstStart : Boolean = true // Первый запуск
    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>> // Регистрация разрешений
    private val model : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Настройка, присваивание слушателей, состояние сервиса
        registerPermissons()
        setOnClicks()
        checkServiceState()
        updateTime()
        registerLocReciever()
        locationUpdates()
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission()
    }


    override fun onDetach() {
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .unregisterReceiver(reciever)
    }

    // Настройка Open Street Maps и сохранение тайлов карты в SharedPreferences
    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    // Регистрация разрешений на использование местоположения
    private fun registerPermissons() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                initOSM()
                checkLocationEnabled()
            } else {
                showToast("Вы не дали разрешение на использование местоположения!")
            }
        }
    }

    // Инициализация Open Street Maps
    private fun initOSM() = with(binding) {
        pl = Polyline()
        pl?.outlinePaint?.color = Color.BLUE
        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val myLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        myLocOverlay.enableMyLocation()
        myLocOverlay.enableFollowLocation()
        myLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(myLocOverlay)
            map.overlays.add(pl)

        }
    }

    // Включен ли GPS
    private fun checkLocationEnabled() {
        val lManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabledGps = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabledGps) { // Если GPS выключен, запускаем диалог
            DialogManager.showLocEnableDialog(
                activity as AppCompatActivity,
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) // Запускаем GPS
                    }
                }
            )
        } else {
            showToast("Location enabled")
        }
    }

    private fun setOnClicks() = with(binding) {
        val listener = onClicks()
        StartStop.setOnClickListener(listener)
    }

    private fun onClicks(): OnClickListener {
        return OnClickListener {
            when (it.id) {
                R.id.StartStop -> startStopService()
            }
        }
    }

    private fun updateTime(){
        model.timeData.observe(viewLifecycleOwner){
            binding.time.text = it
        }
    }


    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread { // Запуск в основном потоке
                    model.timeData.value = getCurentTime()
                }
            }
        }, 1000, 1000)
    }

    private fun getCurentTime() : String{
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }

    private fun startStopService() {
        if (!isServiceRunning) {
            startLocService()
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.StartStop.setImageResource(R.drawable.ic_play)
            timer?.cancel()
        }
        isServiceRunning = !isServiceRunning
    }

    private fun startLocService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.StartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()
    }

    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.StartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }

    // Проверка разрешений
    private fun checkLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)//Если версия больше либо равно Android 10
        {
            checkPermissonAfter10()
        } else {
            checkPermissonBefore10()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissonAfter10() {
        if (checkPermisson(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermisson(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            initOSM()
            checkLocationEnabled()
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    private fun checkPermissonBefore10() {
        if (checkPermisson(Manifest.permission.ACCESS_FINE_LOCATION)) {
            initOSM()
            checkLocationEnabled()
        } else {
            pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private val reciever = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.LOC_MODEL_INTENT){
                val locModel = intent.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                model.locationUpdates.value = locModel
            }
        }
    }

    private fun registerLocReciever(){
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity)
            .registerReceiver(reciever, locFilter)
    }

    private fun locationUpdates(){
        model.locationUpdates.observe(viewLifecycleOwner){
            // заменить хард код
            val distance = "Distance: ${String.format("%.1f", it.distance)} m"
            val velocity = "Velocity: ${String.format("%.1f", 3.6f * it.velocity)} km/h"
            val aVelocity = "Average velocity: ${getAverageSpeed(it.distance)} km/h"
            binding.distance.text = distance
            binding.velosity.text = velocity
            binding.averageVelocity.text = aVelocity
            updatePolyLine(it.geoPointsList)
        }
    }

    // Метод для получения средней скорости движения
    private fun getAverageSpeed(distance : Float): String{
        return String.format("%.1f", 3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000.0f)))
    }

    // Добавить последнюю зафиксированную точку на карту
    private fun addLastPoint(list : List<GeoPoint>){
        pl?.addPoint(list[list.size - 1])
    }

    // Заполнить список точек маршрута
    private fun fillPolyLine(list : List<GeoPoint>){
        list.forEach {
            pl?.addPoint(it)
        }
    }

    private fun updatePolyLine(list : List<GeoPoint>){
        if(list.size > 1 && firstStart){
            fillPolyLine(list)
            firstStart = false
        } else {
            addLastPoint(list)
        }
    }



    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}
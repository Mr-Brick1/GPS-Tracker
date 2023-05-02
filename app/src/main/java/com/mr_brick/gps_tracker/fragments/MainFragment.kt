package com.mr_brick.gps_tracker.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.MutableLiveData
import com.mr_brick.gps_tracker.R
import com.mr_brick.gps_tracker.databinding.FragmentMainBinding
import com.mr_brick.gps_tracker.location.LocationService
import com.mr_brick.gps_tracker.utils.DialogManager
import com.mr_brick.gps_tracker.utils.TimeUtils
import com.mr_brick.gps_tracker.utils.checkPermisson
import com.mr_brick.gps_tracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

class MainFragment : Fragment() {

    private var timer : Timer? = null // Таймер
    private var startTime = 0L // Время старта Таймера
    private val timeData = MutableLiveData<String>()
    private var isServiceRunning: Boolean = false // Запущен ли сервис
    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>> // Регистрация разрешений

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
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission()
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
        map.controller.setZoom(20.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val myLocOverlay = MyLocationNewOverlay(mLocProvider, map)
        myLocOverlay.enableMyLocation()
        myLocOverlay.enableFollowLocation()
        myLocOverlay.runOnFirstFix {
            map.overlays.clear()
            map.overlays.add(myLocOverlay)

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
        timeData.observe(viewLifecycleOwner){
            binding.time.text = it
        }
    }


    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        startTime = System.currentTimeMillis()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread { // Запуск в основном потоке
                    timeData.value = getCurentTime()
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
        startTimer()
    }

    private fun checkServiceState() {
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning) {
            binding.StartStop.setImageResource(R.drawable.ic_stop)
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



    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}
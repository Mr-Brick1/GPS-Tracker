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
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mr_brick.gps_tracker.databinding.FragmentMainBinding
import com.mr_brick.gps_tracker.utils.DialogManager
import com.mr_brick.gps_tracker.utils.checkPermisson
import com.mr_brick.gps_tracker.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>


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
        registerPermissons()
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

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}
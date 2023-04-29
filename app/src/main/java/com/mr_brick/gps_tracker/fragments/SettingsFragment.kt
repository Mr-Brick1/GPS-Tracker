package com.mr_brick.gps_tracker.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mr_brick.gps_tracker.R

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var timePref: Preference
    private lateinit var colorPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        init()
    }

    private fun init() {
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initpref()
    }

    private fun onChangeListener(): Preference.OnPreferenceChangeListener {

        return Preference.OnPreferenceChangeListener { pref, value ->
            when (pref.key) {
                "update_time_key" -> onTimeChanged(value.toString())
                "color_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
            }
            true
        }
    }

    private fun onTimeChanged(value: String) {
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)

        val title = timePref.title.toString().substringBefore(":")
        timePref.title = "$title: ${nameArray[valueArray.indexOf(value)]}"
    }

    private fun initpref() {
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.loc_time_update_name)
        val valueArray = resources.getStringArray(R.array.loc_time_update_value)

        val title = timePref.title
        timePref.title =
            "$title: ${nameArray[valueArray.indexOf(pref?.getString("update_time_key", "3000"))]}"
        val trackColor = pref?.getString("color_key", "#0000FF")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }
}
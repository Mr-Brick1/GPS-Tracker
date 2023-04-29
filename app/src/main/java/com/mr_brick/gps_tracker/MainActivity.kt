package com.mr_brick.gps_tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mr_brick.gps_tracker.databinding.ActivityMainBinding
import com.mr_brick.gps_tracker.fragments.MainFragment
import com.mr_brick.gps_tracker.fragments.SettingsFragment
import com.mr_brick.gps_tracker.fragments.TracksFragment
import com.mr_brick.gps_tracker.utils.openFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBottomNavClicks()
        openFragment(MainFragment.newInstance())
    }

    private fun onBottomNavClicks() {
        binding.bNav.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.id_home -> openFragment(MainFragment.newInstance())
                R.id.id_tracks -> openFragment(TracksFragment.newInstance())
                R.id.id_settings -> openFragment(SettingsFragment())

            }
            true
        }
    }
}
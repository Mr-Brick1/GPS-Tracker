package com.mr_brick.gps_tracker.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.mr_brick.gps_tracker.MainApp
import com.mr_brick.gps_tracker.MainViewModel
import com.mr_brick.gps_tracker.R
import com.mr_brick.gps_tracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class ViewTrackFragment : Fragment() {

    private lateinit var binding: ViewTrackBinding
    private val model : MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm()
        binding = ViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTrack()
    }

    private fun getTrack() = with(binding){
        model.currentTrack.observe(viewLifecycleOwner){

            val tempDate = "Date: ${it.date}"
            val tempAverageVelocity = "Average velocity: ${it.velocity} km/h"
            val tempDistance = "${it.distance} m"

            data.text = tempDate
            time.text = it.time
            averageVelocity.text = tempAverageVelocity
            distance.text = tempDistance


            val polyline = getPolyLine(it.geoPoints)
            map.overlays.add(polyline)
            setMarkers(polyline.actualPoints)
            goToStartPosition(polyline.actualPoints[0])
            polyline.outlinePaint.color = Color.BLUE
        }
    }

    private fun goToStartPosition(startPos: GeoPoint){
        binding.map.controller.zoomTo(18.0)
        binding.map.controller.animateTo(startPos)
    }

    private fun setMarkers(list: List<GeoPoint>) = with(binding){
        val startMarker = Marker(map)
        val finishMarker = Marker(map)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.icon = getDrawable(requireContext(), R.drawable.ic_location_start)
        finishMarker.icon = getDrawable(requireContext(), R.drawable.ic_location_finish)
        startMarker.position = list[0]
        finishMarker.position = list[list.size-1]
        map.overlays.add(startMarker)
        map.overlays.add(finishMarker)
    }

    private fun getPolyLine(geoPoints: String): Polyline{
        val polyline = Polyline()
        polyline.outlinePaint.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("color_key", "#0000FF")
        )
        val list = geoPoints.split("/")
        list.forEach {
            if(it.isEmpty()) return@forEach
            val points = it.split(",")
            polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
        }
        return polyline
    }

    private fun settingsOsm() {
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewTrackFragment()
    }

}
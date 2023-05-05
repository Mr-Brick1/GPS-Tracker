package com.mr_brick.gps_tracker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mr_brick.gps_tracker.MainApp
import com.mr_brick.gps_tracker.MainViewModel
import com.mr_brick.gps_tracker.databinding.TracksBinding
import com.mr_brick.gps_tracker.db.TrackAdapter
import com.mr_brick.gps_tracker.db.TrackItem
import com.mr_brick.gps_tracker.utils.openFragment

class TracksFragment : Fragment(), TrackAdapter.Listener {

    private lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter
    private val model : MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        getTracks()
    }

    private fun getTracks(){
        model.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.tvEmpty.visibility = if(it.isEmpty()){
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun initRcView() = with(binding){
        adapter = TrackAdapter(this@TracksFragment)
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = TracksFragment()
    }

    override fun onClick(track: TrackItem, clickType: TrackAdapter.ClickType) {
        when(clickType){
            TrackAdapter.ClickType.DELETE -> model.deleteTrack(track)
            TrackAdapter.ClickType.OPEN -> {
                model.currentTrack.value = track
                openFragment(ViewTrackFragment.newInstance())
            }
        }
    }

}
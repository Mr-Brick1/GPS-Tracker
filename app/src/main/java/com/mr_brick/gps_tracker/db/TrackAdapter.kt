package com.mr_brick.gps_tracker.db

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mr_brick.gps_tracker.R
import com.mr_brick.gps_tracker.databinding.TrackItemBinding

class TrackAdapter(private val listener: Listener) : ListAdapter<TrackItem, TrackAdapter.Holder>(Comparator()) {

    class Holder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view), OnClickListener{
        private val binding = TrackItemBinding.bind(view)
        private var trackTemp: TrackItem? = null

        init {
            binding.ibDelete.setOnClickListener(this)
        }

        fun bind(trackItem: TrackItem) = with(binding) {
            trackTemp = trackItem
            tvData.text = trackItem.date
            tvVelocity.text = trackItem.velocity
            tvTime.text = trackItem.time
            tvDistance.text = trackItem.distance
        }

        override fun onClick(v: View?) {
            trackTemp?.let { listener.onClick(it) }

        }
    }

    class Comparator : DiffUtil.ItemCallback<TrackItem>(){
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id  == newItem.id
        }
        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return Holder(view, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    interface Listener{
        fun onClick(track: TrackItem)
    }

}
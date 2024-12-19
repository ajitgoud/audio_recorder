package com.black.audiorecorder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.black.audiorecorder.data.model.RecordingItem
import com.black.audiorecorder.databinding.ItemRecordingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingListAdapter(private val listener: RecordingItemClickListener) :
    ListAdapter<RecordingItem, RecordingListAdapter.RecordingViewHolder>(DIFF_UTIL) {

    interface RecordingItemClickListener {
        fun onRecordingItemClicked(recordingItem: RecordingItem)
    }

    inner class RecordingViewHolder(private val binding: ItemRecordingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recordingItem: RecordingItem, listener: RecordingItemClickListener) {
            with(binding) {
                val minutes = recordingItem.length / 60
                val seconds = recordingItem.length % 60
                val durationFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                recordingLengthTextView.text = durationFormatted
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                val date = Date(recordingItem.time)
                val timeFormatted = dateFormat.format(date)
                recordingDateTimeTextView.text = timeFormatted
                root.setOnClickListener {
                    listener.onRecordingItemClicked(recordingItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding =
            ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<RecordingItem>() {
            override fun areItemsTheSame(oldItem: RecordingItem, newItem: RecordingItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: RecordingItem,
                newItem: RecordingItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
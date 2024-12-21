package com.black.audiorecorder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.black.audiorecorder.R
import com.black.audiorecorder.data.model.RecordingItem
import com.black.audiorecorder.databinding.ItemRecordingBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingListAdapter(
    private val listener: RecordingItemClickListener,
    isSelectionMode: Boolean = false
) :
    ListAdapter<RecordingItem, RecordingListAdapter.RecordingViewHolder>(DIFF_UTIL) {

    interface RecordingItemClickListener {
        fun onRecordingItemClicked(recordingItem: RecordingItem)
        fun onRecordingItemSelectionChanged(recordingItem: RecordingItem)
        fun onRecordingItemLongClicked(recordingItem: RecordingItem)
    }

    private var isSelectionMode = false

    init {
        this.isSelectionMode = isSelectionMode
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

                val icon = if (recordingItem.isSelected) {
                    root.setCardBackgroundColor(
                        root.context.getColor(R.color.md_theme_highlight)
                    )
                    AppCompatResources.getDrawable(root.context, R.drawable.ic_check)

                } else {
                    root.setCardBackgroundColor(
                        root.context.getColor(R.color.md_theme_surfaceContainer)
                    )
                    AppCompatResources.getDrawable(root.context, R.drawable.ic_play_circle)
                }

                Glide.with(root).load(icon).into(playButton)

                root.setOnClickListener {
                    if (isSelectionMode) {
                        recordingItem.isSelected = !recordingItem.isSelected
                        notifyItemChanged(adapterPosition)
                        listener.onRecordingItemSelectionChanged(recordingItem)
                    } else {
                        listener.onRecordingItemClicked(recordingItem)
                    }
                }

                if (!isSelectionMode) {
                    root.setOnLongClickListener {
                        isSelectionMode = true
                        recordingItem.isSelected = true
                        notifyItemChanged(adapterPosition)
                        listener.onRecordingItemLongClicked(recordingItem)
                        true
                    }
                }
            }
        }
    }

    fun clearSelectionMode() {
        isSelectionMode = false
        currentList.forEach {
            it.isSelected = false
        }
        notifyDataSetChanged()
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
                oldItem: RecordingItem, newItem: RecordingItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
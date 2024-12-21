package com.black.audiorecorder.ui.home

import androidx.lifecycle.ViewModel
import com.black.audiorecorder.data.model.RecordingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _isRecordingItemSelected = MutableSharedFlow<Boolean>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    val isRecordingItemSelected = _isRecordingItemSelected.asSharedFlow().distinctUntilChanged()

    private val _recordingList = mutableListOf<RecordingItem>()
    val recordingList: List<RecordingItem>
        get() = _recordingList

    fun addRecording(recordingItem: RecordingItem) {
        if (recordingList.isEmpty()) {
            setSelectionMode(true)
        }
        _recordingList.add(recordingItem)

    }

    fun removeRecording(recordingItem: RecordingItem) {
        _recordingList.remove(recordingItem)
        if (recordingList.isEmpty()) {
            setSelectionMode(false)
        }
    }

    private fun setSelectionMode(isSelected: Boolean) {
        _isRecordingItemSelected.tryEmit(isSelected)
    }

    fun clearRecordingList() {
        _recordingList.clear()
        _isRecordingItemSelected.tryEmit(false)
    }


}
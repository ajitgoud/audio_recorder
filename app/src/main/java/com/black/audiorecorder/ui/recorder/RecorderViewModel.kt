package com.black.audiorecorder.ui.recorder

import androidx.lifecycle.ViewModel
import com.black.audiorecorder.utils.AudioRecorder
import com.black.audiorecorder.utils.FileHelper
import com.black.audiorecorder.utils.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    val recorderStateFlow = audioRecorder.recorderStateFlow
    var recorderState: RecorderState = RecorderState.Idle
        private set

    fun startRecording() {
        audioRecorder.startRecording(
            settingsDataStore.audioSampleRate,
            settingsDataStore.audioChannel
        )
    }

    fun stopRecording() {
        audioRecorder.stopRecording()
    }

    fun pauseRecording() {
        audioRecorder.pauseRecording()
    }

    fun resumeRecording() {
        audioRecorder.resumeRecording()
    }

    fun updateRecorderState(state: RecorderState) {
        recorderState = state
    }

    enum class RecorderState {
        Idle,
        RecordingStarted,
        RecordingPaused,
        RecordingResumed,
        RecordingStopped
    }


}
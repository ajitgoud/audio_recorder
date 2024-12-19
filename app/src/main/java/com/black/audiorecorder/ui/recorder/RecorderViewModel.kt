package com.black.audiorecorder.ui.recorder

import androidx.lifecycle.ViewModel
import com.black.audiorecorder.utils.AudioRecorder
import com.black.audiorecorder.utils.FileHelper
import com.black.audiorecorder.utils.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder
) : ViewModel() {

    val recorderStateFlow = audioRecorder.recorderStateFlow

    fun startRecording() {
        audioRecorder.startRecording()
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


}
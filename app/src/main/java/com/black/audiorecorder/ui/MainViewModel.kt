package com.black.audiorecorder.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.audiorecorder.data.model.RecordingItem
import com.black.audiorecorder.utils.AudioRecorder
import com.black.audiorecorder.utils.FileHelper
import com.black.audiorecorder.utils.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileHelper: FileHelper,
    private val settingsDataStore: SettingsDataStore,
    private val audioRecorder: AudioRecorder
) : ViewModel() {

    private val _recordings: MutableList<RecordingItem> = mutableListOf()
    val recordings = _recordings.toList()

    private val _recordingsListFlow: MutableSharedFlow<RecordingFileState> = MutableSharedFlow(
        replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val recordingsListFlow = _recordingsListFlow.asSharedFlow()

    init {
        initObserver()
    }

    private fun initObserver() {
        viewModelScope.launch {
            settingsDataStore.getLastSavedSettings()
            audioRecorder.initRecorder(
                settingsDataStore.audioSampleRate,
                settingsDataStore.audioChannel
            )
        }
        viewModelScope.launch {
            settingsDataStore.getSettings().collectLatest {
                audioRecorder.initRecorder(
                    settingsDataStore.audioSampleRate,
                    settingsDataStore.audioChannel
                )
            }
        }
    }


    fun saveAudioRecording(context: Context, audioBytes: ByteArray) {
        // Save audio recording
        Timber.i("Saving audio recording: ${audioBytes.size}")
        val isSaved = fileHelper.saveAudioRecording(context, audioBytes)
        if (isSaved) {
            _recordingsListFlow.tryEmit(RecordingFileState.NewRecordingAvailable)
        }
    }

    fun getAudioRecordings(context: Context) {
        // Get audio recordings
        Timber.i("Getting audio recordings")
        val recordings = fileHelper.getAudioRecordings(context)
        _recordings.clear()
        _recordings.addAll(recordings)
        _recordingsListFlow.tryEmit(RecordingFileState.Success(recordings))
    }

    fun deleteRecordings(recordingList: List<RecordingItem>) {
        // Delete audio recordings
        Timber.i("Deleting audio recordings")
        fileHelper.deleteAudioRecordings(recordingList)
        _recordingsListFlow.tryEmit(RecordingFileState.NewRecordingAvailable)
    }


    sealed interface RecordingFileState {
        data object NewRecordingAvailable : RecordingFileState
        data class Success(val recordings: List<RecordingItem>) : RecordingFileState
    }
}
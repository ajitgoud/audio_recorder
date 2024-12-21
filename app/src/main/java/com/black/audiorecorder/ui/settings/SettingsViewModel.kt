package com.black.audiorecorder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.audiorecorder.utils.AudioRecordBuilder
import com.black.audiorecorder.utils.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsDataStore: SettingsDataStore) :
    ViewModel() {

    val audioSampleRate: Int = settingsDataStore.audioSampleRate
    val audioChannel: Int = settingsDataStore.audioChannel

    fun getSampleRates(): List<Int> = AudioRecordBuilder.getSampleRates()


    fun updateSettings(sampleRate: Int, channel: Int) {
        viewModelScope.launch { settingsDataStore.updateSettings(sampleRate, channel) }
    }
}
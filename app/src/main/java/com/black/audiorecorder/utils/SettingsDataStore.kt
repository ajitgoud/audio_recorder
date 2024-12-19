package com.black.audiorecorder.utils

import android.content.Context
import android.media.AudioFormat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "audio_recorder_settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        const val KEY_RECORDING_CHANNEL = "recording_channel"
        const val KEY_RECORDING_SAMPLE_RATE = "recording_sample_rate"
        val recordingChannelKey = intPreferencesKey(KEY_RECORDING_CHANNEL)
        val recordingSampleRateKey = intPreferencesKey(KEY_RECORDING_SAMPLE_RATE)

    }

    var audioSampleRate: Int = AudioRecordBuilder.DEFAULT_SAMPLE_RATE
        private set
    var audioChannel: Int = AudioRecordBuilder.DEFAULT_RECORDING_CHANNEL
        private set
    var audioFormat: Int = AudioRecordBuilder.DEFAULT_AUDIO_FORMAT
        private set

    suspend fun updateRecordingChannel(channel: Int) {
        dataStore.edit { settings ->
            settings[recordingChannelKey] = channel
            audioChannel = channel
        }
    }

    suspend fun updateRecordingSampleRate(sampleRate: Int) {
        dataStore.edit { settings ->
            settings[recordingSampleRateKey] = sampleRate
            audioSampleRate = sampleRate
        }
    }

    suspend fun getRecordingChannel(): Int {
        val preferences = dataStore.data.first()
        audioChannel =
            preferences[recordingChannelKey] ?: AudioRecordBuilder.DEFAULT_RECORDING_CHANNEL
        return audioChannel
    }

    suspend fun getRecordingSampleRate(): Int {
        val preferences = dataStore.data.first()
        audioSampleRate =
            preferences[recordingSampleRateKey] ?: AudioRecordBuilder.DEFAULT_SAMPLE_RATE
        return audioSampleRate
    }

    suspend fun getLastSavedSettings() {
        val preferences = dataStore.data.first()
        audioChannel =
            preferences[recordingChannelKey] ?: AudioRecordBuilder.DEFAULT_RECORDING_CHANNEL
        audioSampleRate =
            preferences[recordingSampleRateKey] ?: AudioRecordBuilder.DEFAULT_SAMPLE_RATE
    }
}
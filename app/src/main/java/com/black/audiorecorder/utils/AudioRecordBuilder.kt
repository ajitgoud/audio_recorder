package com.black.audiorecorder.utils

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class AudioRecordBuilder private constructor() {
    companion object {
        enum class SampleRate(val rate: Int) {
            RATE_8000(8000),
            RATE_16000(16000),
            RATE_44100(44100),
            RATE_48000(48000)
        }

        val DEFAULT_SAMPLE_RATE = SampleRate.RATE_44100.rate
        const val DEFAULT_RECORDING_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        fun getSampleRates(): List<Int> {
            return SampleRate.entries.map { it.rate }
        }
    }

    class Builder {
        private var audioSource = MediaRecorder.AudioSource.MIC
        private var sampleRateInHz = DEFAULT_SAMPLE_RATE
        private var channelConfig = DEFAULT_RECORDING_CHANNEL
        private var audioFormat = DEFAULT_AUDIO_FORMAT
        private var bufferSizeInBytes = AudioRecord.getMinBufferSize(
            sampleRateInHz,
            channelConfig,
            audioFormat
        )

        fun setAudioSource(audioSource: Int) = apply { this.audioSource = audioSource }
        fun setSampleRate(sampleRate: Int) = apply { this.sampleRateInHz = sampleRate }
        fun setChannelConfig(channelConfig: Int) = apply { this.channelConfig = channelConfig }
        fun setAudioFormat(audioFormat: Int) = apply { this.audioFormat = audioFormat }

        fun setBufferSizeInBytes(bufferSizeInBytes: Int) =
            apply { this.bufferSizeInBytes = bufferSizeInBytes }

        @SuppressLint("MissingPermission")
        fun build() = AudioRecord(
            audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSizeInBytes
        )
    }

}
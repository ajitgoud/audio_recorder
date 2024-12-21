package com.black.audiorecorder.utils

import android.media.AudioRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var recorder: AudioRecord
    private var isRecording: Boolean = false
    private val _recorderStateFlow: MutableSharedFlow<RecorderState> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val recorderStateFlow = _recorderStateFlow.asSharedFlow()

    private val audioBuffers: MutableList<ByteArray> = mutableListOf()

    fun initRecorder(sampleRate: Int, channel: Int) {
        // Initialize the recorder
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channel,
            AudioRecordBuilder.DEFAULT_AUDIO_FORMAT
        )
        recorder =
            AudioRecordBuilder.Builder()
                .setSampleRate(sampleRate)
                .setChannelConfig(channel)
                .setBufferSizeInBytes(minBufferSize)
                .build()
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            emitState(RecorderState.Error("Recorder not initialized"))
            return
        }
        emitState(RecorderState.Initialized)
    }


    fun startRecording(sampleRate: Int, channel: Int): Boolean {
        // Start recording
        if (!ensureRecorderInitialized(sampleRate, channel)) {
            emitState(RecorderState.Error("Recorder not initialized"))
            return false
        }
        if (!isRecording) {
            isRecording = true
            recorder.startRecording()
            recordAudio()
            emitState(RecorderState.RecordingStarted)
            return true
        }
        return false

    }

    private fun recordAudio() {
        coroutineScope.launch {
            val buffer = ByteArray(recorder.bufferSizeInFrames * 2)
            while (isRecording) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    audioBuffers.add(buffer.copyOf())
                }
            }
        }
    }

    fun stopRecording() {
        // Stop recording
        if (!isRecorderInitialized()) {
            emitState(RecorderState.Error("Recorder not initialized"))
            return
        }
        if (isRecording) {
            isRecording = false
            recorder.stop()
            coroutineScope.launch {
                emitState(RecorderState.RecordingStopped(audioBuffers.reduce { acc, bytes -> acc + bytes }))
                audioBuffers.clear()
            }
        }
    }

    fun pauseRecording() {
        // Pause recording
        if (!isRecorderInitialized()) {
            emitState(RecorderState.Error("Recorder not initialized"))
            return
        }
        if (isRecording) {
            recorder.stop()
            emitState(RecorderState.Paused)
        }
    }

    fun resumeRecording() {
        // Resume recording
        if (!isRecorderInitialized()) {
            emitState(RecorderState.Error("Recorder not initialized"))
            return
        }
        if (isRecording) {
            recorder.startRecording()
            emitState(RecorderState.Resumed)
        }
    }

    fun release() {
        // Release the recorder
        if (::recorder.isInitialized && recorder.state == AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            emitState(RecorderState.Released)
        }
    }

    private fun ensureRecorderInitialized(sampleRate: Int, channel: Int): Boolean {
        if (!::recorder.isInitialized || recorder.state != AudioRecord.STATE_INITIALIZED) {
            initRecorder(sampleRate, channel)
        }
        return recorder.state == AudioRecord.STATE_INITIALIZED
    }

    private fun isRecorderInitialized(): Boolean {
        return ::recorder.isInitialized && recorder.state == AudioRecord.STATE_INITIALIZED
    }

    private fun emitState(state: RecorderState) {
        coroutineScope.launch {
            _recorderStateFlow.emit(state)
        }
    }

    sealed interface RecorderState {
        data object Initialized : RecorderState
        data object RecordingStarted : RecorderState
        data object Paused : RecorderState
        data object Resumed : RecorderState
        data class RecordingStopped(val audioBytes: ByteArray) : RecorderState {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as RecordingStopped

                return audioBytes.contentEquals(other.audioBytes)
            }

            override fun hashCode(): Int {
                return audioBytes.contentHashCode()
            }
        }

        data object Released : RecorderState
        data class Error(val message: String) : RecorderState
    }
}
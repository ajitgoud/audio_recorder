package com.black.audiorecorder.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import com.black.audiorecorder.data.model.RecordingItem
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FileHelper @Inject constructor(private val settingsDataStore: SettingsDataStore) {

    companion object {
        private const val RECORDING_DIRECTORY = "recordings"
    }

    private fun getRecordingDirectory(context: Context): String {
        return context.filesDir.absolutePath + "/" + RECORDING_DIRECTORY
    }

    private fun getFileName(context: Context): String {
        val recordingDirName = getRecordingDirectory(context)
        if (recordingDirName.isNotEmpty()) {
            val dir = File(recordingDirName)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
        return "${recordingDirName}/recording_${System.currentTimeMillis()}.wav"
    }

    private fun getRecordingFile(context: Context): File {
        return File(getFileName(context))
    }

    fun saveAudioRecording(context: Context, audioBytes: ByteArray): Boolean {
        val file = getRecordingFile(context)
        // Save the audio recording to the file
        val fileStream = FileOutputStream(file)
        AudioOperation.writeWavHeader(
            fileStream,
            settingsDataStore.audioChannel,
            settingsDataStore.audioSampleRate,
            settingsDataStore.audioFormat
        )
        fileStream.write(audioBytes)
        AudioOperation.updateWavHeader(file)
        fileStream.close()
        return true
    }

    private fun listAudioRecordings(context: Context): List<File> {
        val recordingDir = File(getRecordingDirectory(context))
        return recordingDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun getAudioRecordings(context: Context): List<RecordingItem> {
        val recordings = listAudioRecordings(context)
        if (recordings.isEmpty()) return emptyList()
        return recordings.map { file ->
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            retriever.release()
            val recordingItem = RecordingItem(
                name = file.name,
                path = file.absolutePath,
                length = duration / 1000,
                time = file.lastModified()
            )
            recordingItem
        }


    }


}
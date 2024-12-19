package com.black.audiorecorder.data.model

data class RecordingItem(
    val name: String,
    val path: String,
    val length: Long,
    val time: Long
)
package com.example.saltplayerremote.models

data class NowPlaying(
    val status: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val isPlaying: Boolean?,
    val position: Long?,
    val volume: Float?,
    val timestamp: Long?
)
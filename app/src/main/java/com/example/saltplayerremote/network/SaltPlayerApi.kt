package com.example.saltplayerremote.network

import com.example.saltplayerremote.models.NowPlaying
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface SaltPlayerApi {
    @GET("/api/now-playing")
    suspend fun getNowPlaying(): Response<NowPlaying>

    @GET("/api/play-pause")
    suspend fun togglePlayPause(): Response<Void>

    @GET("/api/next-track")
    suspend fun nextTrack(): Response<Void>

    @GET("/api/previous-track")
    suspend fun previousTrack(): Response<Void>

    @GET("/api/volume/up")
    suspend fun volumeUp(): Response<Void>

    @GET("/api/volume/down")
    suspend fun volumeDown(): Response<Void>

    @GET("/api/mute")
    suspend fun toggleMute(): Response<Void>

    companion object {
        fun create(baseUrl: String): SaltPlayerApi {
            return Retrofit.Builder()
                .baseUrl("http://$baseUrl:35373")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SaltPlayerApi::class.java)
        }
    }
}
package com.example.saltplayerremote.network

import com.example.saltplayerremote.models.NeteaseSong
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NeteaseApi {
    @GET("https://music.163.com/api/search/get")
    suspend fun searchSong(
        @Query("type") type: Int = 1,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 1,
        @Query("s") query: String
    ): Response<SearchResponse>

    @GET("https://api.injahow.cn/meting/")
    suspend fun getSongInfo(
        @Query("type") type: String = "song",
        @Query("id") id: String
    ): Response<List<NeteaseSong>>

    companion object {
        fun create(): NeteaseApi {
            return Retrofit.Builder()
                .baseUrl("https://api.injahow.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NeteaseApi::class.java)
        }
    }
}

data class SearchResponse(
    val result: SearchResult?,
    val code: Int
)

data class SearchResult(
    val songs: List<Song>?,
    val hasMore: Boolean,
    val songCount: Int
)

data class Song(
    val id: Long,
    val name: String,
    val artists: List<Artist>,
    val album: Album
)

data class Artist(
    val id: Long,
    val name: String
)

data class Album(
    val id: Long,
    val name: String
)
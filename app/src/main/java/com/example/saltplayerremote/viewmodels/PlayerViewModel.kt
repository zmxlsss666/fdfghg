package com.example.saltplayerremote.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saltplayerremote.models.NowPlaying
import com.example.saltplayerremote.network.NeteaseApi
import com.example.saltplayerremote.network.SaltPlayerApi
import com.example.saltplayerremote.utils.NetworkUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class PlayerViewModel : ViewModel() {

    private var _nowPlaying = MutableLiveData<NowPlaying?>()
    val nowPlaying: LiveData<NowPlaying?> get() = _nowPlaying

    private var _coverUrl = MutableLiveData<String?>()
    val coverUrl: LiveData<String?> get() = _coverUrl

    private var _lyricsUrl = MutableLiveData<String?>()
    val lyricsUrl: LiveData<String?> get() = _lyricsUrl

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var refreshJob: Job? = null
    private var currentIp: String? = null
    private var saltPlayerApi: SaltPlayerApi? = null
    private val neteaseApi = NeteaseApi.create()

    fun init(ipAddress: String) {
        currentIp = ipAddress
        saltPlayerApi = SaltPlayerApi.create(ipAddress)
        startRefreshing()
    }

    fun stop() {
        refreshJob?.cancel()
    }

    private fun startRefreshing() {
        refreshJob = viewModelScope.launch {
            while (true) {
                refreshNowPlaying()
                delay(3000) // 每3秒刷新一次
            }
        }
    }

    private suspend fun refreshNowPlaying() {
        try {
            _isLoading.postValue(true)
            val response = saltPlayerApi?.getNowPlaying()
            if (response?.isSuccessful == true) {
                _nowPlaying.postValue(response.body())
                response.body()?.let { fetchSongInfo(it) }
            } else {
                _errorMessage.postValue("Failed to get current song: ${response?.code()}")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Network error: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }

    private suspend fun fetchSongInfo(nowPlaying: NowPlaying) {
        try {
            val title = nowPlaying.title ?: return
            val artist = nowPlaying.artist ?: return
            val query = "$title - $artist"
            
            val searchResponse = neteaseApi.searchSong(query = query)
            if (searchResponse.isSuccessful) {
                val songId = searchResponse.body()?.result?.songs?.firstOrNull()?.id ?: return
                val songInfoResponse = neteaseApi.getSongInfo(id = songId.toString())
                if (songInfoResponse.isSuccessful) {
                    val neteaseSong = songInfoResponse.body()?.firstOrNull()
                    _coverUrl.postValue(neteaseSong?.pic)
                    _lyricsUrl.postValue(neteaseSong?.lrc)
                }
            }
        } catch (e: Exception) {
            // 静默失败，不影响主功能
        }
    }

    suspend fun togglePlayPause() {
        saltPlayerApi?.togglePlayPause()
        refreshNowPlaying()
    }

    suspend fun nextTrack() {
        saltPlayerApi?.nextTrack()
        refreshNowPlaying()
    }

    suspend fun previousTrack() {
        saltPlayerApi?.previousTrack()
        refreshNowPlaying()
    }

    suspend fun volumeUp() {
        saltPlayerApi?.volumeUp()
    }

    suspend fun volumeDown() {
        saltPlayerApi?.volumeDown()
    }

    suspend fun toggleMute() {
        saltPlayerApi?.toggleMute()
    }
}
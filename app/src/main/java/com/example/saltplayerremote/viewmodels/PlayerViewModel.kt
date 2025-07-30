package com.example.saltplayerremote.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saltplayerremote.models.NowPlaying
import com.example.saltplayerremote.network.NeteaseApi
import com.example.saltplayerremote.network.SaltPlayerApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class PlayerViewModel : ViewModel() {

    private val _nowPlaying = MutableLiveData<NowPlaying?>(null)
    val nowPlaying: LiveData<NowPlaying?> get() = _nowPlaying

    private val _coverUrl = MutableLiveData<String?>(null)
    val coverUrl: LiveData<String?> get() = _coverUrl

    private val _lyricsUrl = MutableLiveData<String?>(null)
    val lyricsUrl: LiveData<String?> get() = _lyricsUrl

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var refreshJob: Job? = null
    private var saltPlayerApi: SaltPlayerApi? = null
    private val neteaseApi = NeteaseApi.create()

    fun init(ipAddress: String) {
        saltPlayerApi = SaltPlayerApi.create(ipAddress)
        startRefreshing()
    }

    fun stop() {
        refreshJob?.cancel()
    }

    private fun startRefreshing() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                refreshNowPlaying()
                delay(3000) // 每3秒刷新一次
            }
        }
    }

    private suspend fun refreshNowPlaying() {
        try {
            _isLoading.value = true
            val response = saltPlayerApi?.getNowPlaying()
            if (response != null && response.isSuccessful) {
                _nowPlaying.value = response.body()
                response.body()?.let { fetchSongInfo(it) }
            } else {
                _errorMessage.value = "Failed to get current song: ${response?.code()}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Network error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun fetchSongInfo(nowPlaying: NowPlaying) {
        try {
            val title = nowPlaying.title ?: return
            val artist = nowPlaying.artist ?: return
            val query = "$title - $artist"
            
            val searchResponse = neteaseApi.searchSong(query = query)
            if (searchResponse.isSuccessful) {
                val song = searchResponse.body()?.result?.songs?.firstOrNull()
                val songId = song?.id ?: return
                
                val songInfoResponse = neteaseApi.getSongInfo(id = songId.toString())
                if (songInfoResponse.isSuccessful) {
                    val neteaseSong = songInfoResponse.body()?.firstOrNull()
                    _coverUrl.value = neteaseSong?.pic
                    _lyricsUrl.value = neteaseSong?.lrc
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

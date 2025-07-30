package com.example.saltplayerremote.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.saltplayerremote.R
import com.example.saltplayerremote.databinding.ActivityPlayerControlBinding
import com.example.saltplayerremote.viewmodels.PlayerViewModel
import kotlinx.coroutines.launch

class PlayerControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerControlBinding
    private val viewModel: PlayerViewModel by viewModels()
    private var deviceIp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceIp = intent.getStringExtra("device_ip") ?: run {
            finish()
            return
        }

        setupViewModel()
        setupListeners()
        setupObservers()
    }

    private fun setupViewModel() {
        deviceIp?.let { viewModel.init(it) }
    }

    private fun setupListeners() {
        binding.playPauseButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.togglePlayPause()
            }
        }
        
        binding.nextButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.nextTrack()
            }
        }
        
        binding.prevButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.previousTrack()
            }
        }
        
        binding.volumeUpButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.volumeUp()
            }
        }
        
        binding.volumeDownButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.volumeDown()
            }
        }
        
        binding.muteButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.toggleMute()
            }
        }
        
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.nowPlaying.observe(this) { nowPlaying ->
            nowPlaying?.let {
                binding.songTitle.text = it.title ?: getString(R.string.unknown_title)
                binding.songArtist.text = it.artist ?: getString(R.string.unknown_artist)
                binding.songAlbum.text = it.album ?: getString(R.string.unknown_album)
                
                if (it.isPlaying == true) {
                    binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                } else {
                    binding.playPauseButton.setImageResource(R.drawable.ic_play)
                }
            }
        }
        
        viewModel.coverUrl.observe(this) { url ->
            if (url != null) {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.layout_cover_placeholder)
                    .into(binding.coverImage)
            } else {
                binding.coverImage.setImageResource(R.drawable.layout_cover_placeholder)
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stop()
    }
}

package com.pulse.player.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.pulse.player.data.model.PlaybackState
import com.pulse.player.data.model.PlayerState
import com.pulse.player.data.model.StreamQuality
import com.pulse.player.data.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Media3PlayerManager(context: Context) {

    private val trackSelector = DefaultTrackSelector(context)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .build()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentVideoItem: VideoItem? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val state = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.IDLE
                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                    Player.STATE_READY -> PlaybackState.READY
                    Player.STATE_ENDED -> PlaybackState.ENDED
                    else -> PlaybackState.IDLE
                }
                _playerState.update {
                    it.copy(
                        playbackState = state,
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                extractAvailableQualities(tracks)
            }
        })
    }

    fun play(videoItem: VideoItem, streamUri: String) {
        currentVideoItem = videoItem

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(videoItem.title)
            .setArtist(videoItem.channelTitle)
            .setArtworkUri(if (videoItem.thumbnailUrl.isNotBlank()) Uri.parse(videoItem.thumbnailUrl) else null)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(streamUri))
            .setMediaId(videoItem.id)
            .setMediaMetadata(mediaMetadata)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        _playerState.update {
            it.copy(
                isPlaying = true,
                errorMessage = null,
                currentPositionMs = 0L,
                durationMs = videoItem.durationSeconds * 1000L
            )
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun seekForward(offsetMs: Long = 10000L) {
        seekTo((exoPlayer.currentPosition + offsetMs).coerceAtMost(exoPlayer.duration))
    }

    fun seekBackward(offsetMs: Long = 10000L) {
        seekTo((exoPlayer.currentPosition - offsetMs).coerceAtLeast(0L))
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
        _playerState.update { it.copy(playbackSpeed = speed) }
    }

    fun selectQuality(quality: StreamQuality) {
        if (quality == StreamQuality.AUTO) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            )
        } else {
            // Apply track selection override
            val currentTracks = exoPlayer.currentTracks
            for (trackGroup in currentTracks.groups) {
                if (trackGroup.type == C.TRACK_TYPE_VIDEO && trackGroup.isSupported) {
                    val group = trackGroup.mediaTrackGroup
                    if (quality.trackIndex in 0 until group.length) {
                        trackSelector.setParameters(
                            trackSelector.buildUponParameters()
                                .setOverrideForType(
                                    TrackSelectionOverride(group, quality.trackIndex)
                                )
                        )
                        break
                    }
                }
            }
        }
        _playerState.update { it.copy(selectedQuality = quality) }
    }

    private fun extractAvailableQualities(tracks: Tracks) {
        val qualities = mutableListOf<StreamQuality>()
        qualities.add(StreamQuality.AUTO)

        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_VIDEO) {
                for (i in 0 until group.length) {
                    if (group.isTrackSupported(i)) {
                        val format = group.getTrackFormat(i)
                        val height = format.height
                        val label = if (height > 0) "${height}p" else "Video $i"
                        qualities.add(
                            StreamQuality(
                                label = label,
                                width = format.width,
                                height = format.height,
                                bitrate = format.bitrate.toLong(),
                                mimeType = format.sampleMimeType ?: "",
                                trackIndex = i
                            )
                        )
                    }
                }
            }
        }
        _playerState.update { it.copy(availableQualities = qualities) }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _playerState.update {
                        it.copy(
                            currentPositionMs = exoPlayer.currentPosition,
                            durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        exoPlayer.release()
    }
}

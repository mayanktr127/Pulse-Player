package com.pulse.player.ui.screens.detail

import android.content.Intent
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.player.YouTubeIFramePlayerView
import com.pulse.player.ui.components.VideoCard
import com.pulse.player.ui.theme.CyanAccent
import com.pulse.player.ui.theme.DarkBackground
import com.pulse.player.ui.theme.DarkSurface
import com.pulse.player.ui.theme.DarkSurfaceVariant
import com.pulse.player.ui.theme.LightTextPrimary
import com.pulse.player.ui.theme.LightTextSecondary
import com.pulse.player.ui.theme.RedPrimary

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VideoDetailScreen(
    viewModel: VideoDetailViewModel,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onFullscreenClick: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTermsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Details", color = LightTextPrimary, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LightTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = RedPrimary
                    )
                }

                uiState.video != null -> {
                    val video = uiState.video!!

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // 1. Embedded Player Container (16:9)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(Color.Black)
                            ) {
                                if (video.sourceType == VideoSourceType.YOUTUBE) {
                                    // Official YouTube IFrame Player
                                    YouTubeIFramePlayerView(videoId = video.id)
                                } else {
                                    // AndroidX Media3 ExoPlayer for authorized remote and local media
                                    AndroidView(
                                        factory = { ctx ->
                                            PlayerView(ctx).apply {
                                                player = viewModel.playerManager.exoPlayer
                                                useController = true
                                                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Fullscreen button overlay
                                    IconButton(
                                        onClick = { onFullscreenClick(video.id, video.streamUrl) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
                                    }
                                }
                            }
                        }

                        // 2. Video Header & Metadata
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LightTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${video.channelTitle} • ${video.publishedAt}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightTextSecondary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))

                                    // Source Tag
                                    val tagColor = if (video.sourceType == VideoSourceType.YOUTUBE) RedPrimary else CyanAccent
                                    val tagText = if (video.sourceType == VideoSourceType.YOUTUBE) "YouTube" else "ExoPlayer"
                                    Box(
                                        modifier = Modifier
                                            .background(tagColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = tagText, color = tagColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    // Bookmark
                                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = if (uiState.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark",
                                                tint = if (uiState.isBookmarked) CyanAccent else LightTextPrimary
                                            )
                                            Text(
                                                text = if (uiState.isBookmarked) "Saved" else "Save",
                                                fontSize = 10.sp,
                                                color = LightTextSecondary
                                            )
                                        }
                                    }

                                    // Download Button
                                    IconButton(
                                        onClick = {
                                            if (video.sourceType == VideoSourceType.YOUTUBE) {
                                                showTermsDialog = true
                                            } else {
                                                viewModel.downloadVideo { msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Download",
                                                tint = if (video.isDownloadable) LightTextPrimary else LightTextSecondary.copy(alpha = 0.5f)
                                            )
                                            Text("Download", fontSize = 10.sp, color = LightTextSecondary)
                                        }
                                    }

                                    // Share Button
                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, if (video.sourceType == VideoSourceType.YOUTUBE) "https://www.youtube.com/watch?v=${video.id}" else video.streamUrl)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                                        }
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = LightTextPrimary)
                                            Text("Share", fontSize = 10.sp, color = LightTextSecondary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = DarkSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))

                                // Description Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurface)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = video.description.ifBlank { "No description available for this video." },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightTextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // 3. Related Videos Header
                        item {
                            Text(
                                text = "Related Streams",
                                style = MaterialTheme.typography.titleMedium,
                                color = LightTextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // 4. Related Videos List
                        items(uiState.relatedVideos) { related ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                VideoCard(
                                    video = related,
                                    onClick = { onVideoClick(related) }
                                )
                            }
                        }
                    }
                }
            }

            // YouTube Developer Terms Notice Dialog
            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = RedPrimary) },
                    title = { Text("Download Policy Notice") },
                    text = {
                        Text(
                            text = "Downloading or offline caching of YouTube videos is strictly prohibited by YouTube Developer Terms of Service.\n\nDownloads in Pulse Player are reserved exclusively for authorized open media and personal device files.",
                            color = LightTextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showTermsDialog = false }) {
                            Text("Understood", color = RedPrimary)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}

package com.pulse.player.ui.screens.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.player.data.local.entity.DownloadEntity
import com.pulse.player.data.local.entity.DownloadStatus
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.data.repository.DownloadRepository
import com.pulse.player.ui.theme.CyanAccent
import com.pulse.player.ui.theme.DarkBackground
import com.pulse.player.ui.theme.DarkSurface
import com.pulse.player.ui.theme.LightTextPrimary
import com.pulse.player.ui.theme.LightTextSecondary
import com.pulse.player.ui.theme.RedPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloadRepository: DownloadRepository,
    onPlayOfflineMedia: (VideoItem) -> Unit
) {
    val downloads by downloadRepository.getAllDownloads().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Authorized Downloads",
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
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
            if (downloads.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = null,
                        tint = LightTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No downloads yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Authorized open streams and local files you download will be stored here for offline playback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(downloads) { download ->
                        DownloadItemCard(
                            download = download,
                            onClick = {
                                if (download.status == DownloadStatus.COMPLETED) {
                                    val videoItem = VideoItem(
                                        id = download.videoId,
                                        title = download.title,
                                        description = "Offline downloaded stream",
                                        thumbnailUrl = "",
                                        channelTitle = "Offline Media",
                                        streamUrl = download.localFilePath,
                                        sourceType = VideoSourceType.LOCAL_FILE
                                    )
                                    onPlayOfflineMedia(videoItem)
                                }
                            },
                            onDelete = {
                                scope.launch { downloadRepository.deleteDownload(download.id) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadItemCard(
    download: DownloadEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (download.status) {
                    DownloadStatus.COMPLETED -> Icons.Default.DownloadDone
                    DownloadStatus.DOWNLOADING -> Icons.Default.Downloading
                    DownloadStatus.FAILED -> Icons.Default.ErrorOutline
                    else -> Icons.Default.Downloading
                }
                val iconColor = when (download.status) {
                    DownloadStatus.COMPLETED -> CyanAccent
                    DownloadStatus.DOWNLOADING -> RedPrimary
                    DownloadStatus.FAILED -> Color.Red
                    else -> LightTextSecondary
                }

                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = LightTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val statusText = when (download.status) {
                        DownloadStatus.COMPLETED -> "Ready for offline playback"
                        DownloadStatus.DOWNLOADING -> "Downloading... (${download.bytesDownloaded / (1024 * 1024)} MB)"
                        DownloadStatus.FAILED -> "Failed: ${download.errorMessage ?: "Network error"}"
                        DownloadStatus.PAUSED -> "Paused"
                        DownloadStatus.PENDING -> "Waiting in queue"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LightTextSecondary)
                }
            }

            if (download.status == DownloadStatus.DOWNLOADING) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = RedPrimary
                )
            }
        }
    }
}

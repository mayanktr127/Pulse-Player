package com.pulse.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.ui.theme.CyanAccent
import com.pulse.player.ui.theme.DarkSurface
import com.pulse.player.ui.theme.LightTextPrimary
import com.pulse.player.ui.theme.LightTextSecondary
import com.pulse.player.ui.theme.RedPrimary

@Composable
fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onDownloadClick: (() -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail container with duration badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                if (video.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Source Type Badge (Top Left)
                val badgeColor = when (video.sourceType) {
                    VideoSourceType.YOUTUBE -> RedPrimary
                    VideoSourceType.REMOTE_STREAM -> CyanAccent
                    VideoSourceType.LOCAL_FILE -> Color(0xFF4CAF50)
                }
                val badgeText = when (video.sourceType) {
                    VideoSourceType.YOUTUBE -> "YouTube"
                    VideoSourceType.REMOTE_STREAM -> "HLS / Open"
                    VideoSourceType.LOCAL_FILE -> "Local File"
                }

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Duration Badge (Bottom Right)
                if (video.durationSeconds > 0) {
                    val minutes = video.durationSeconds / 60
                    val seconds = video.durationSeconds % 60
                    val timeString = String.format("%d:%02d", minutes, seconds)

                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = timeString,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Metadata row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Channel Avatar / Placeholder
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.channelTitle.firstOrNull()?.toString()?.uppercase() ?: "P",
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = LightTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${video.channelTitle} • ${formatViewCount(video.viewCount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LightTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = LightTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        if (video.isDownloadable && onDownloadClick != null) {
                            DropdownMenuItem(
                                text = { Text("Download Media", color = LightTextPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onDownloadClick()
                                }
                            )
                        }
                        if (onBookmarkClick != null) {
                            DropdownMenuItem(
                                text = { Text("Save to Bookmarks", color = LightTextPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onBookmarkClick()
                                }
                            )
                        }
                        if (onShareClick != null) {
                            DropdownMenuItem(
                                text = { Text("Share Link", color = LightTextPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onShareClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatViewCount(views: Long): String {
    return when {
        views >= 1_000_000_000 -> String.format("%.1fB views", views / 1_000_000_000.0)
        views >= 1_000_000 -> String.format("%.1fM views", views / 1_000_000.0)
        views >= 1_000 -> String.format("%.1fK views", views / 1_000.0)
        views > 0 -> "$views views"
        else -> "Stream"
    }
}

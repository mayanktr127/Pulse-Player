package com.pulse.player.ui.screens.library

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.player.data.local.entity.BookmarkEntity
import com.pulse.player.data.local.entity.HistoryEntity
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.repository.MediaRepository
import com.pulse.player.ui.components.VideoCard
import com.pulse.player.ui.theme.CyanAccent
import com.pulse.player.ui.theme.DarkBackground
import com.pulse.player.ui.theme.DarkSurface
import com.pulse.player.ui.theme.LightTextPrimary
import com.pulse.player.ui.theme.LightTextSecondary
import com.pulse.player.ui.theme.RedPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    mediaRepository: MediaRepository,
    onVideoClick: (VideoItem) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("History", "Bookmarks", "Local Media")
    val scope = rememberCoroutineScope()

    val historyList by mediaRepository.getWatchHistory().collectAsState(initial = emptyList())
    val bookmarkList by mediaRepository.getAllBookmarks().collectAsState(initial = emptyList())
    var localVideos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var localAudio by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var localSubTab by remember { mutableIntStateOf(0) } // 0 = Video, 1 = Audio

    LaunchedEffect(Unit) {
        localVideos = mediaRepository.getLocalVideos()
        localAudio = mediaRepository.getLocalAudio()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DarkBackground)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Library & Saved",
                            color = LightTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        if (selectedTab == 0 && historyList.isNotEmpty()) {
                            IconButton(onClick = { scope.launch { mediaRepository.clearHistory() } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = LightTextSecondary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkBackground,
                    contentColor = RedPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = RedPrimary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = (selectedTab == index),
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) RedPrimary else LightTextSecondary,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                // 1. History Tab
                0 -> {
                    if (historyList.isEmpty()) {
                        EmptyLibraryState("No watch history yet", "Videos you watch will appear here.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(historyList) { history ->
                                val videoItem = VideoItem(
                                    id = history.videoId,
                                    title = history.title,
                                    thumbnailUrl = history.thumbnailUrl,
                                    channelTitle = history.channelTitle,
                                    durationSeconds = history.durationSeconds,
                                    sourceType = history.sourceType,
                                    streamUrl = history.videoId
                                )
                                VideoCard(
                                    video = videoItem,
                                    onClick = { onVideoClick(videoItem) }
                                )
                            }
                        }
                    }
                }

                // 2. Bookmarks Tab
                1 -> {
                    if (bookmarkList.isEmpty()) {
                        EmptyLibraryState("No bookmarks saved", "Save videos to your bookmarks for quick access.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(bookmarkList) { bookmark ->
                                val videoItem = VideoItem(
                                    id = bookmark.videoId,
                                    title = bookmark.title,
                                    thumbnailUrl = bookmark.thumbnailUrl,
                                    channelTitle = bookmark.channelTitle,
                                    durationSeconds = bookmark.durationSeconds,
                                    sourceType = bookmark.sourceType,
                                    streamUrl = bookmark.videoId
                                )
                                VideoCard(
                                    video = videoItem,
                                    onClick = { onVideoClick(videoItem) }
                                )
                            }
                        }
                    }
                }

                // 3. Local Media Tab (Device Videos & Audio)
                2 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-toggle Videos / Music
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { localSubTab = 0 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (localSubTab == 0) RedPrimary else DarkSurface
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Videos (${localVideos.size})", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { localSubTab = 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (localSubTab == 1) CyanAccent else DarkSurface
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = if (localSubTab == 1) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Music (${localAudio.size})", color = if (localSubTab == 1) Color.Black else Color.White, fontSize = 12.sp)
                            }
                        }

                        val currentList = if (localSubTab == 0) localVideos else localAudio
                        if (currentList.isEmpty()) {
                            EmptyLibraryState("No local media found", "Grant media permissions or add audio/video files to your device storage.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(currentList) { item ->
                                    VideoCard(
                                        video = item,
                                        onClick = { onVideoClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SdCard,
                contentDescription = null,
                tint = LightTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = LightTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary
            )
        }
    }
}

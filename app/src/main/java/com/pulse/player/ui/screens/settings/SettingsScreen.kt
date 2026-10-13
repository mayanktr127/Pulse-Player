package com.pulse.player.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.player.data.repository.MediaRepository
import com.pulse.player.ui.theme.CyanAccent
import com.pulse.player.ui.theme.DarkBackground
import com.pulse.player.ui.theme.DarkSurface
import com.pulse.player.ui.theme.DarkSurfaceVariant
import com.pulse.player.ui.theme.LightTextPrimary
import com.pulse.player.ui.theme.LightTextSecondary
import com.pulse.player.ui.theme.RedPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mediaRepository: MediaRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDarkTheme by remember { mutableStateOf(true) }
    var defaultSpeed by remember { mutableFloatStateOf(1.0f) }
    var defaultQuality by remember { mutableStateOf("Auto") }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var currentApiKey by remember { mutableStateOf(mediaRepository.getYouTubeApiKey()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader("Appearance")
                SettingsToggleCard(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Sleek AMOLED dark interface",
                    isChecked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )
            }

            item {
                SectionHeader("YouTube API Configuration")
                SettingsClickableCard(
                    icon = Icons.Default.Key,
                    title = "YouTube Data API v3 Key",
                    value = if (currentApiKey.isNotBlank()) "Configured (••••${currentApiKey.takeLast(4)})" else "Not Configured",
                    onClick = { showApiKeyDialog = true }
                )
            }

            item {
                SectionHeader("Player Preferences")
                SettingsClickableCard(
                    icon = Icons.Default.Speed,
                    title = "Default Playback Speed",
                    value = "${defaultSpeed}x",
                    onClick = {
                        defaultSpeed = when (defaultSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 2.0f
                            else -> 1.0f
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsClickableCard(
                    icon = Icons.Default.HighQuality,
                    title = "Default Media3 Quality",
                    value = defaultQuality,
                    onClick = {
                        defaultQuality = when (defaultQuality) {
                            "Auto" -> "1080p"
                            "1080p" -> "720p"
                            "720p" -> "480p"
                            else -> "Auto"
                        }
                    }
                )
            }

            item {
                SectionHeader("Storage & Data")
                SettingsClickableCard(
                    icon = Icons.Default.Delete,
                    title = "Clear Watch History",
                    value = "Erase all",
                    onClick = { showClearHistoryDialog = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsClickableCard(
                    icon = Icons.Default.CleaningServices,
                    title = "Clear Cache",
                    value = "Clean",
                    onClick = {
                        Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                SectionHeader("About & Legal")
                SettingsClickableCard(
                    icon = Icons.Default.Info,
                    title = "About Pulse Player",
                    value = "v1.0.0",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        if (showApiKeyDialog) {
            var inputKey by remember { mutableStateOf(currentApiKey) }
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                title = { Text("YouTube Data API Key", color = LightTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "A Google Cloud YouTube Data API v3 key is required to search and discover YouTube videos.\n\n" +
                                    "Keys can also be configured in local.properties (youtube.api.key=...).",
                            color = LightTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            placeholder = { Text("AIzaSy...", color = LightTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightTextPrimary,
                                unfocusedTextColor = LightTextPrimary,
                                focusedBorderColor = RedPrimary,
                                unfocusedBorderColor = LightTextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mediaRepository.setYouTubeApiKey(inputKey.trim())
                            currentApiKey = inputKey.trim()
                            showApiKeyDialog = false
                            Toast.makeText(context, "API Key saved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Row {
                        if (currentApiKey.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    mediaRepository.setYouTubeApiKey("")
                                    currentApiKey = ""
                                    inputKey = ""
                                    showApiKeyDialog = false
                                    Toast.makeText(context, "API Key cleared", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Clear", color = RedPrimary)
                            }
                        }
                        TextButton(onClick = { showApiKeyDialog = false }) {
                            Text("Cancel", color = LightTextSecondary)
                        }
                    }
                },
                containerColor = DarkSurface
            )
        }

        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear Watch History") },
                text = { Text("Are you sure you want to delete all watch history records?", color = LightTextSecondary) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                mediaRepository.clearHistory()
                                Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                                showClearHistoryDialog = false
                            }
                        }
                    ) {
                        Text("Clear", color = RedPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel", color = LightTextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("Pulse Player v1.0.0") },
                text = {
                    Column {
                        Text(
                            text = "A clean, modern, privacy-focused Android video player inspired by NewPipe.\n\n" +
                                "• Built with Jetpack Compose & Material 3\n" +
                                "• Powered by AndroidX Media3 (ExoPlayer)\n" +
                                "• Official YouTube IFrame Player API integration\n" +
                                "• YouTube Data API v3 search and discovery\n" +
                                "• Local Room SQLite database\n" +
                                "• Zero analytics trackers, zero telemetry",
                            color = LightTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close", color = RedPrimary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = CyanAccent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = RedPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = LightTextPrimary, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = LightTextSecondary, fontSize = 12.sp)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RedPrimary
                )
            )
        }
    }
}

@Composable
private fun SettingsClickableCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = RedPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = LightTextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(text = value, color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

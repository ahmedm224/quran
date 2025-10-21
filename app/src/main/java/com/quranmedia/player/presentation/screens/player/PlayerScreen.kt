package com.quranmedia.player.presentation.screens.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    reciterId: String,
    surahNumber: Int,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsState()

    LaunchedEffect(reciterId, surahNumber) {
        viewModel.loadAudio(reciterId, surahNumber)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Surah info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Surah $surahNumber",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Reciter: $reciterId",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (playbackState.currentAyah != null) {
                    Text(
                        text = "Ayah ${playbackState.currentAyah}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Seek bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = playbackState.currentPosition.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..playbackState.duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(playbackState.currentPosition))
                    Text(formatTime(playbackState.duration))
                }
            }

            // Playback controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousAyah() }) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous Ayah",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(onClick = { viewModel.nextAyah() }) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next Ayah",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Nudge controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.nudgeBackward() }) {
                        Text("-250ms")
                    }
                    OutlinedButton(onClick = { viewModel.nudgeForward() }) {
                        Text("+250ms")
                    }
                }
            }

            // Speed control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Speed: ${String.format("%.1fx", playbackState.playbackSpeed)}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { viewModel.setPlaybackSpeed(0.75f) }) {
                        Text("0.75x")
                    }
                    FilledTonalButton(onClick = { viewModel.setPlaybackSpeed(1.0f) }) {
                        Text("1.0x")
                    }
                    FilledTonalButton(onClick = { viewModel.setPlaybackSpeed(1.25f) }) {
                        Text("1.25x")
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

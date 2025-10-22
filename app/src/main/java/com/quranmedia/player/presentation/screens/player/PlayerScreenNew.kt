package com.quranmedia.player.presentation.screens.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreenNew(
    reciterId: String,
    surahNumber: Int,
    resumeFromSaved: Boolean = false,
    startFromAyah: Int? = null,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val bookmarkSaved by viewModel.bookmarkSaved.collectAsState()

    LaunchedEffect(reciterId, surahNumber, startFromAyah) {
        val resumePosition = if (resumeFromSaved) {
            viewModel.getSavedPosition(reciterId, surahNumber)
        } else null

        viewModel.loadAudio(reciterId, surahNumber, resumePosition, startFromAyah)
    }

    // Islamic green theme colors
    val islamicGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val darkGreen = Color(0xFF1B5E20)
    val goldAccent = Color(0xFFFFD700)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = playbackState.currentSurahNameEnglish ?: "Alfurqan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        playbackState.currentSurahNameArabic?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = islamicGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ayah display card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ayah number indicator
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = islamicGreen.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = if (playbackState.currentAyah != null && playbackState.totalAyahs != null) {
                                "آية ${playbackState.currentAyah} من ${playbackState.totalAyahs}"
                            } else {
                                "جاري التحميل..."
                            },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = islamicGreen,
                            fontSize = 16.sp
                        )
                    }

                    // Arabic ayah text - Adaptive sizing for different ayah lengths
                    if (playbackState.currentAyahText != null) {
                        val ayahText = playbackState.currentAyahText!!
                        val textLength = ayahText.length

                        // Adaptive font size based on ayah length
                        val fontSize = when {
                            textLength < 50 -> 28.sp      // Very short ayahs
                            textLength < 100 -> 24.sp     // Short ayahs
                            textLength < 200 -> 20.sp     // Medium ayahs
                            textLength < 400 -> 18.sp     // Long ayahs
                            else -> 16.sp                 // Very long ayahs
                        }

                        val lineHeight = fontSize * 1.6f

                        // For very long ayahs, use single-line marquee instead of multiline
                        if (textLength > 300) {
                            // Single line with auto-scrolling marquee for long ayahs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ayahText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontSize = fontSize,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            delayMillis = 1000,
                                            initialDelayMillis = 2000,
                                            velocity = 30.dp
                                        )
                                )
                            }
                        } else {
                            // Multiline scrollable for shorter ayahs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ayahText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontSize = fontSize,
                                    lineHeight = lineHeight,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(32.dp),
                            color = islamicGreen
                        )
                    }

                    // Reciter name
                    playbackState.currentReciterName?.let {
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = islamicGreen.copy(alpha = 0.2f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = islamicGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = islamicGreen,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Progress bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Slider(
                        value = if (playbackState.duration > 0) {
                            playbackState.currentPosition.toFloat().coerceIn(0f, playbackState.duration.toFloat())
                        } else 0f,
                        onValueChange = { newValue ->
                            if (playbackState.duration > 0 && !playbackState.isBuffering) {
                                viewModel.seekTo(newValue.toLong().coerceIn(0, playbackState.duration))
                            }
                        },
                        valueRange = 0f..playbackState.duration.toFloat().coerceAtLeast(1f),
                        enabled = playbackState.duration > 0 && !playbackState.isBuffering,
                        colors = SliderDefaults.colors(
                            thumbColor = islamicGreen,
                            activeTrackColor = lightGreen,
                            inactiveTrackColor = lightGreen.copy(alpha = 0.3f),
                            disabledThumbColor = islamicGreen.copy(alpha = 0.5f),
                            disabledActiveTrackColor = lightGreen.copy(alpha = 0.2f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(playbackState.currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            formatTime(playbackState.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Playback controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Main controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.previousAyah() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = lightGreen.copy(alpha = 0.2f),
                                contentColor = islamicGreen
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous Ayah",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = islamicGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        FilledIconButton(
                            onClick = { viewModel.nextAyah() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = lightGreen.copy(alpha = 0.2f),
                                contentColor = islamicGreen
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next Ayah",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Nudge controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.nudgeBackward() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = islamicGreen
                            )
                        ) {
                            Icon(Icons.Default.FastRewind, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("-250ms")
                        }
                        OutlinedButton(
                            onClick = { viewModel.nudgeForward() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = islamicGreen
                            )
                        ) {
                            Text("+250ms")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.FastForward, null, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Bookmark button
                    Button(
                        onClick = { viewModel.createBookmark() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bookmarkSaved) lightGreen else goldAccent.copy(alpha = 0.2f),
                            contentColor = if (bookmarkSaved) Color.White else Color(0xFFF57C00)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (bookmarkSaved) Icons.Default.Check else Icons.Default.Bookmark,
                            contentDescription = if (bookmarkSaved) "Bookmark saved" else "Add bookmark",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (bookmarkSaved) "Bookmark Saved!" else "Bookmark This Position",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

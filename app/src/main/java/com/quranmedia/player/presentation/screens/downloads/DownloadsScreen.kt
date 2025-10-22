package com.quranmedia.player.presentation.screens.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranmedia.player.data.database.entity.DownloadStatus
import com.quranmedia.player.data.database.entity.DownloadTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val islamicGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val darkGreen = Color(0xFF1B5E20)
    val creamBackground = Color(0xFFFAF8F3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = islamicGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = creamBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = islamicGreen
                    )
                }
                state.downloads.isEmpty() -> {
                    EmptyDownloadsView(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.downloads, key = { it.id }) { download ->
                            DownloadCard(
                                download = download,
                                onPause = { viewModel.pauseDownload(download.id) },
                                onResume = { viewModel.resumeDownload(download.id) },
                                onCancel = { viewModel.cancelDownload(download.id) },
                                onDelete = { viewModel.deleteDownload(download.reciterId, download.surahNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDownloadsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CloudDownload,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Downloads Yet",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Download surahs for offline listening",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DownloadCard(
    download: DownloadTaskEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val islamicGreen = Color(0xFF2E7D32)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Surah info and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Surah ${download.surahNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = download.reciterId,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status badge
                StatusBadge(status = download.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar (for in-progress downloads)
            if (download.status == DownloadStatus.IN_PROGRESS.name) {
                Column {
                    LinearProgressIndicator(
                        progress = download.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = islamicGreen,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(download.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = formatBytes(download.bytesDownloaded) + " / " + formatBytes(download.bytesTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Error message (for failed downloads)
            if (download.status == DownloadStatus.FAILED.name && download.errorMessage != null) {
                Text(
                    text = "Error: ${download.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (download.status) {
                    DownloadStatus.IN_PROGRESS.name -> {
                        TextButton(onClick = onPause) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                        TextButton(onClick = onCancel) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                    DownloadStatus.PAUSED.name -> {
                        TextButton(onClick = onResume) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                        TextButton(onClick = onCancel) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                    DownloadStatus.COMPLETED.name -> {
                        TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                    DownloadStatus.FAILED.name -> {
                        TextButton(onClick = onResume) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry")
                        }
                        TextButton(onClick = onCancel) {
                            Icon(Icons.Default.Cancel, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                    else -> {
                        // PENDING
                        TextButton(onClick = onCancel) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        DownloadStatus.PENDING.name -> Pair(Color.Gray, "Pending")
        DownloadStatus.IN_PROGRESS.name -> Pair(Color(0xFF1976D2), "Downloading")
        DownloadStatus.PAUSED.name -> Pair(Color(0xFFF57C00), "Paused")
        DownloadStatus.COMPLETED.name -> Pair(Color(0xFF388E3C), "Completed")
        DownloadStatus.FAILED.name -> Pair(Color(0xFFD32F2F), "Failed")
        else -> Pair(Color.Gray, "Unknown")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    return "%.1f MB".format(mb)
}

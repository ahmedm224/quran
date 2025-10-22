package com.quranmedia.player.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranmedia.player.domain.model.Reciter
import com.quranmedia.player.domain.model.Surah
import com.quranmedia.player.presentation.screens.home.LastPlaybackInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenNew(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToReciters: () -> Unit,
    onNavigateToSurahs: () -> Unit,
    onNavigateToPlayer: (String, Int, Boolean) -> Unit,
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {}
) {
    val lastPlaybackInfo by viewModel.lastPlaybackInfo.collectAsState()

    // Refresh last playback info when screen is shown
    LaunchedEffect(Unit) {
        viewModel.refreshLastPlaybackInfo()
    }

    // Islamic green theme colors
    val islamicGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val darkGreen = Color(0xFF1B5E20)
    val goldAccent = Color(0xFFFFD700)
    val creamBackground = Color(0xFFFAF8F3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الفرقان",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alfurqan",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "About",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = islamicGreen,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = creamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Banner with Bismillah
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = islamicGreen
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(islamicGreen, darkGreen)
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = 28.sp,
                        color = goldAccent,
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "In the name of Allah, the Most Gracious, the Most Merciful",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }

            // Continue Listening Card
            if (lastPlaybackInfo != null && lastPlaybackInfo?.surah != null && lastPlaybackInfo?.reciter != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = lightGreen.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = islamicGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue Listening",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Surah info
                        Text(
                            text = lastPlaybackInfo?.surah?.nameArabic ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = islamicGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lastPlaybackInfo?.surah?.nameEnglish ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Reciter: ${lastPlaybackInfo?.reciter?.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resume button
                        Button(
                            onClick = {
                                lastPlaybackInfo?.let { info: LastPlaybackInfo ->
                                    info.reciter?.let { reciter: Reciter ->
                                        info.surah?.let { surah: Surah ->
                                            // Navigate with resume=true to restore saved position
                                            onNavigateToPlayer(reciter.id, surah.number, true)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = islamicGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resume Playback",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = islamicGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue Listening",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent playback. Start your journey with the Quran!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Main Menu Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القائمة الرئيسية",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGreen
                )
                Text(
                    text = "Main Menu",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // Main Action Cards in Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: Listen (Surahs) - Full width featured card
                IslamicActionCard(
                    title = "استمع للقرآن",
                    subtitle = "Listen to Quran",
                    icon = Icons.Default.LibraryMusic,
                    backgroundColor = islamicGreen.copy(alpha = 0.15f),
                    iconColor = darkGreen,
                    onClick = onNavigateToSurahs,
                    modifier = Modifier.fillMaxWidth()
                )

                // Row 2: Bookmarks and Downloads
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IslamicActionCard(
                        title = "المفضلة",
                        subtitle = "Bookmarks",
                        icon = Icons.Default.Bookmark,
                        backgroundColor = Color(0xFFFFE082).copy(alpha = 0.3f),
                        iconColor = Color(0xFFF57C00),
                        onClick = onNavigateToBookmarks,
                        modifier = Modifier.weight(1f)
                    )
                    IslamicActionCard(
                        title = "التنزيلات",
                        subtitle = "Downloads",
                        icon = Icons.Default.Download,
                        backgroundColor = Color(0xFF81C784).copy(alpha = 0.3f),
                        iconColor = Color(0xFF388E3C),
                        onClick = onNavigateToDownloads,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with Hadith/Quote
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = lightGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = islamicGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"The best among you are those who learn the Quran and teach it\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = darkGreen,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "— Prophet Muhammad ﷺ",
                        style = MaterialTheme.typography.bodySmall,
                        color = islamicGreen,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun IslamicActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

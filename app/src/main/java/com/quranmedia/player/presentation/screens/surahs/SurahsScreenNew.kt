package com.quranmedia.player.presentation.screens.surahs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranmedia.player.domain.model.RevelationType
import com.quranmedia.player.domain.model.Surah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahsScreenNew(
    viewModel: SurahsViewModel = hiltViewModel(),
    onSurahClick: (Surah) -> Unit,
    onBack: () -> Unit
) {
    val surahs by viewModel.surahs.collectAsState()

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
                            text = "السور",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Surahs",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = Color.White
                        )
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
        if (surahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = islamicGreen)
                    Text(
                        text = "Loading surahs...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = lightGreen.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = islamicGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "114 Surahs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = darkGreen
                            )
                            Text(
                                text = "Select a surah to begin listening",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Surahs list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(surahs) { surah ->
                        SurahItemNew(
                            surah = surah,
                            onClick = { onSurahClick(surah) }
                        )
                    }
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SurahItemNew(
    surah: Surah,
    onClick: () -> Unit
) {
    val islamicGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val goldAccent = Color(0xFFFFD700)
    val darkGreen = Color(0xFF1B5E20)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah number circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (surah.number == 1) goldAccent.copy(alpha = 0.2f)
                        else lightGreen.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (surah.number == 1) Color(0xFFF57C00) else islamicGreen
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Surah info (English)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameEnglish,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = when (surah.revelationType) {
                            RevelationType.MECCAN -> "Meccan"
                            RevelationType.MEDINAN -> "Medinan"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${surah.ayahCount} ayahs",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Arabic name
            Text(
                text = surah.nameArabic,
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = islamicGreen
            )
        }
    }
}

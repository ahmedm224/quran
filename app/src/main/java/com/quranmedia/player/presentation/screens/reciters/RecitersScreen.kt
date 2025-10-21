package com.quranmedia.player.presentation.screens.reciters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quranmedia.player.domain.model.Reciter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersScreen(
    viewModel: RecitersViewModel = hiltViewModel(),
    onReciterClick: (Reciter) -> Unit,
    onBack: () -> Unit
) {
    val reciters by viewModel.reciters.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reciters") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (reciters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No reciters available")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reciters) { reciter ->
                    ReciterItem(
                        reciter = reciter,
                        onClick = { onReciterClick(reciter) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReciterItem(
    reciter: Reciter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reciter.name,
                    style = MaterialTheme.typography.titleMedium
                )
                reciter.nameArabic?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.quranmedia.player.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onSurahClick: (Int) -> Unit,
    onReciterClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        placeholder = { Text("Search surahs, reciters...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.search("")
                                }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, "Search")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (searchState.surahs.isNotEmpty()) {
                item {
                    Text(
                        "Surahs",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(searchState.surahs) { surah ->
                    Card(
                        onClick = { onSurahClick(surah.number) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(surah.nameEnglish, style = MaterialTheme.typography.titleMedium)
                                Text(surah.nameTransliteration, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(surah.nameArabic, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            if (searchState.reciters.isNotEmpty()) {
                item {
                    Text(
                        "Reciters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(searchState.reciters) { reciter ->
                    Card(
                        onClick = { onReciterClick(reciter.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(reciter.name, style = MaterialTheme.typography.titleMedium)
                                reciter.nameArabic?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            if (searchQuery.isNotEmpty() && searchState.surahs.isEmpty() && searchState.reciters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("No results found")
                    }
                }
            }
        }
    }
}

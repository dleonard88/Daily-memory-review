package com.dan.dailyrecall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dan.dailyrecall.DailyRecallApp
import com.dan.dailyrecall.data.EntryWithLabels
import com.dan.dailyrecall.data.LabelType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(onAdd: () -> Unit, onOpen: (LocalDate) -> Unit) {
    val app = LocalContext.current.applicationContext as DailyRecallApp
    val vm: TimelineViewModel = viewModel(
        factory = viewModelFactory { initializer { TimelineViewModel(app.repository) } }
    )
    val entries by vm.entries.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily recall") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Yesterday") },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                placeholder = { Text("Search entries, people, places, tags") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (query.isBlank()) {
                            "No entries yet. Tap the button to record yesterday."
                        } else {
                            "No matches."
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(entries, key = { item -> item.entry.id }) { item ->
                        EntryCard(item) { onOpen(LocalDate.parse(item.entry.date)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryCard(item: EntryWithLabels, onClick: () -> Unit) {
    val e = item.entry
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    LocalDate.parse(e.date).format(DisplayDateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text("${e.mood}/10", style = MaterialTheme.typography.labelLarge)
            }
            Text(
                e.what.ifBlank { "No summary" },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.labels.isNotEmpty()) {
                Text(
                    item.labels.joinToString("  ") { l ->
                        if (l.type == LabelType.TAG) "#${l.name}" else l.name
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

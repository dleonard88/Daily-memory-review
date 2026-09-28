package com.dan.dailyrecall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dan.dailyrecall.DailyRecallApp
import com.dan.dailyrecall.data.LabelType
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(initialDate: LocalDate, onClose: () -> Unit) {
    val app = LocalContext.current.applicationContext as DailyRecallApp
    val vm: EditorViewModel = viewModel(
        factory = viewModelFactory { initializer { EditorViewModel(app.repository, initialDate) } }
    )
    val ui = vm.ui
    val focus = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily recall") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Date selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { vm.changeDay(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
                }
                Text(ui.date.format(DisplayDateFormatter), style = MaterialTheme.typography.titleLarge)
                IconButton(
                    onClick = { vm.changeDay(1) },
                    enabled = ui.date.isBefore(LocalDate.now()),
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
                }
            }

            // Mood
            Column {
                Text("Mood: ${ui.mood}/10", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = ui.mood.toFloat(),
                    onValueChange = { v -> vm.update { it.copy(mood = v.roundToInt()) } },
                    valueRange = 1f..10f,
                    steps = 8,
                )
            }

            TextArea("What happened", ui.what, minLines = 4) { v -> vm.update { it.copy(what = v) } }

            ChipInput(
                label = "Who I saw",
                chips = ui.people,
                onChipsChange = { v -> vm.update { it.copy(people = v) } },
                suggestionsFor = { q -> vm.suggestions(LabelType.PERSON, q) },
            )

            ChipInput(
                label = "Places visited",
                chips = ui.places,
                onChipsChange = { v -> vm.update { it.copy(places = v) } },
                suggestionsFor = { q -> vm.suggestions(LabelType.PLACE, q) },
            )

            Text("Achievements and key tasks", style = MaterialTheme.typography.titleMedium)
            TextArea("Personal", ui.personalWins) { v -> vm.update { it.copy(personalWins = v) } }
            TextArea("Professional", ui.professionalWins) { v -> vm.update { it.copy(professionalWins = v) } }

            TextArea("Decisions and follow-ups", ui.decisions) { v -> vm.update { it.copy(decisions = v) } }

            ChipInput(
                label = "Tags",
                chips = ui.tags,
                onChipsChange = { v -> vm.update { it.copy(tags = v) } },
                suggestionsFor = { q -> vm.suggestions(LabelType.TAG, q) },
            )

            Button(
                onClick = {
                    // Clearing focus makes any half-typed chip text commit before we save.
                    focus.clearFocus()
                    vm.save(onClose)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save entry") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TextArea(
    label: String,
    value: String,
    minLines: Int = 2,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
    )
}

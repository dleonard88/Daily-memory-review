package com.dan.dailyrecall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * Reusable chip input used for people, places and tags.
 *
 * - Type and press Done or comma to turn text into a chip
 * - Tap a chip's x to remove it
 * - Suggestion chips appear underneath (popular labels when the field is empty,
 *   prefix matches while typing); tap one to add it
 * - Any half-typed text is committed as a chip when the field loses focus
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChipInput(
    label: String,
    chips: List<String>,
    onChipsChange: (List<String>) -> Unit,
    suggestionsFor: suspend (String) -> List<String>,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(text, chips) {
        suggestions = suggestionsFor(text)
            .filter { s -> chips.none { it.equals(s, ignoreCase = true) } }
    }

    fun add(raw: String) {
        val value = raw.trim().trim(',').trim()
        if (value.isNotEmpty() && chips.none { it.equals(value, ignoreCase = true) }) {
            onChipsChange(chips + value)
        }
        text = ""
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall)

        if (chips.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                chips.forEach { chip ->
                    InputChip(
                        selected = false,
                        onClick = { onChipsChange(chips - chip) },
                        label = { Text(chip) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove $chip")
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { new -> if (new.endsWith(",")) add(new) else text = new },
            placeholder = { Text("Add and press Done") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { add(text) }),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state -> if (!state.isFocused && text.isNotBlank()) add(text) },
        )

        if (suggestions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                suggestions.forEach { s ->
                    SuggestionChip(onClick = { add(s) }, label = { Text(s) })
                }
            }
        }
    }
}

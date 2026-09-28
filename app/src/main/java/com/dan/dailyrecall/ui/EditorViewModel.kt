package com.dan.dailyrecall.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.dailyrecall.data.EntryDraft
import com.dan.dailyrecall.data.EntryRepository
import com.dan.dailyrecall.data.EntryWithLabels
import com.dan.dailyrecall.data.LabelType
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EditorUiState(
    val date: LocalDate,
    val mood: Int = 5,
    val what: String = "",
    val personalWins: String = "",
    val professionalWins: String = "",
    val decisions: String = "",
    val notes: String = "",
    val people: List<String> = emptyList(),
    val places: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
) {
    /** True if anything beyond the default mood has been entered. */
    val hasContent: Boolean
        get() = listOf(what, personalWins, professionalWins, decisions, notes).any { it.isNotBlank() } ||
            people.isNotEmpty() || places.isNotEmpty() || tags.isNotEmpty()

    fun toDraft() = EntryDraft(
        date = date, mood = mood, what = what, personalWins = personalWins,
        professionalWins = professionalWins, decisions = decisions, notes = notes,
        people = people, places = places, tags = tags,
    )
}

private fun EntryWithLabels.toUiState(date: LocalDate) = EditorUiState(
    date = date,
    mood = entry.mood,
    what = entry.what,
    personalWins = entry.personalWins,
    professionalWins = entry.professionalWins,
    decisions = entry.decisions,
    notes = entry.notes,
    people = labels.filter { it.type == LabelType.PERSON }.map { it.name },
    places = labels.filter { it.type == LabelType.PLACE }.map { it.name },
    tags = labels.filter { it.type == LabelType.TAG }.map { it.name },
)

class EditorViewModel(
    private val repository: EntryRepository,
    initialDate: LocalDate,
) : ViewModel() {

    var ui by mutableStateOf(EditorUiState(date = initialDate))
        private set

    init {
        load(initialDate)
    }

    private fun load(date: LocalDate) {
        viewModelScope.launch {
            ui = repository.getEntry(date)?.toUiState(date) ?: EditorUiState(date = date)
        }
    }

    fun update(transform: (EditorUiState) -> EditorUiState) {
        ui = transform(ui)
    }

    /** Moves to the previous/next day, saving the current one first if it has content. */
    fun changeDay(delta: Long) {
        val current = ui
        val target = current.date.plusDays(delta)
        viewModelScope.launch {
            if (current.hasContent) repository.save(current.toDraft())
            ui = repository.getEntry(target)?.toUiState(target) ?: EditorUiState(date = target)
        }
    }

    fun save(onDone: () -> Unit) {
        val draft = ui.toDraft()
        viewModelScope.launch {
            repository.save(draft)
            onDone()
        }
    }

    suspend fun suggestions(type: LabelType, query: String): List<String> =
        repository.labelSuggestions(type, query)
}

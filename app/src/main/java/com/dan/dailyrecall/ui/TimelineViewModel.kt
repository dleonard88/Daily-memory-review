package com.dan.dailyrecall.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.dailyrecall.data.EntryRepository
import com.dan.dailyrecall.data.EntryWithLabels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class TimelineViewModel(repository: EntryRepository) : ViewModel() {

    val query = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val entries: StateFlow<List<EntryWithLabels>> = query
        .debounce(200)
        .flatMapLatest { repository.observeEntries(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

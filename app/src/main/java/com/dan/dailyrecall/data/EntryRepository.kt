package com.dan.dailyrecall.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Everything the editor screen collects, ready to persist. */
data class EntryDraft(
    val date: LocalDate,
    val mood: Int,
    val what: String,
    val personalWins: String,
    val professionalWins: String,
    val decisions: String,
    val notes: String,
    val people: List<String>,
    val places: List<String>,
    val tags: List<String>,
)

class EntryRepository(private val db: AppDatabase) {
    private val entryDao = db.entryDao()
    private val labelDao = db.labelDao()

    fun observeEntries(query: String): Flow<List<EntryWithLabels>> =
        if (query.isBlank()) entryDao.observeAll() else entryDao.search("%${query.trim()}%")

    suspend fun getEntry(date: LocalDate): EntryWithLabels? = entryDao.getByDate(date.toString())

    /** Popular labels when [query] is blank, otherwise prefix matches. */
    suspend fun labelSuggestions(type: LabelType, query: String): List<String> =
        if (query.isBlank()) labelDao.popular(type, 8) else labelDao.suggest(type, query.trim(), 8)

    suspend fun save(draft: EntryDraft) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            val existing = entryDao.getByDate(draft.date.toString())?.entry

            val entryId = if (existing == null) {
                entryDao.insert(
                    Entry(
                        date = draft.date.toString(),
                        mood = draft.mood,
                        what = draft.what.trim(),
                        personalWins = draft.personalWins.trim(),
                        professionalWins = draft.professionalWins.trim(),
                        decisions = draft.decisions.trim(),
                        notes = draft.notes.trim(),
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            } else {
                entryDao.update(
                    existing.copy(
                        mood = draft.mood,
                        what = draft.what.trim(),
                        personalWins = draft.personalWins.trim(),
                        professionalWins = draft.professionalWins.trim(),
                        decisions = draft.decisions.trim(),
                        notes = draft.notes.trim(),
                        updatedAt = now,
                    )
                )
                existing.id
            }

            val labelIds = resolve(LabelType.PERSON, draft.people) +
                resolve(LabelType.PLACE, draft.places) +
                resolve(LabelType.TAG, draft.tags)

            entryDao.clearLabels(entryId)
            entryDao.insertCrossRefs(labelIds.distinct().map { EntryLabelCrossRef(entryId, it) })
        }
    }

    private suspend fun resolve(type: LabelType, names: List<String>): List<Long> =
        names.map { normalize(type, it) }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .map { name ->
                labelDao.find(name, type)?.id ?: labelDao.insert(Label(name = name, type = type))
            }

    // Tags are lowercased so "Work" and "work" are one tag. People and places keep
    // the casing you typed; the column is NOCASE so they still match case-insensitively.
    private fun normalize(type: LabelType, raw: String): String {
        val cleaned = raw.trim().replace(Regex("\\s+"), " ")
        return if (type == LabelType.TAG) cleaned.lowercase() else cleaned
    }
}

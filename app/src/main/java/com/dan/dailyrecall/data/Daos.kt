package com.dan.dailyrecall.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Transaction
    @Query("SELECT * FROM entries ORDER BY date DESC")
    fun observeAll(): Flow<List<EntryWithLabels>>

    // Simple LIKE search across text fields and label names. Fine for a personal
    // journal; swap for an FTS table later if it ever feels slow.
    @Transaction
    @Query(
        """
        SELECT DISTINCT e.* FROM entries e
        LEFT JOIN entry_labels el ON el.entryId = e.id
        LEFT JOIN labels l ON l.id = el.labelId
        WHERE e.what LIKE :pattern
           OR e.personalWins LIKE :pattern
           OR e.professionalWins LIKE :pattern
           OR e.decisions LIKE :pattern
           OR e.notes LIKE :pattern
           OR l.name LIKE :pattern
        ORDER BY e.date DESC
        """
    )
    fun search(pattern: String): Flow<List<EntryWithLabels>>

    @Transaction
    @Query("SELECT * FROM entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): EntryWithLabels?

    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Query("DELETE FROM entry_labels WHERE entryId = :entryId")
    suspend fun clearLabels(entryId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<EntryLabelCrossRef>)
}

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels WHERE name = :name AND type = :type LIMIT 1")
    suspend fun find(name: String, type: LabelType): Label?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(label: Label): Long

    @Query(
        "SELECT name FROM labels WHERE type = :type AND name LIKE :prefix || '%' " +
            "ORDER BY name LIMIT :limit"
    )
    suspend fun suggest(type: LabelType, prefix: String, limit: Int): List<String>

    @Query(
        """
        SELECT l.name FROM labels l
        JOIN entry_labels el ON el.labelId = l.id
        WHERE l.type = :type
        GROUP BY l.id
        ORDER BY COUNT(*) DESC, l.name
        LIMIT :limit
        """
    )
    suspend fun popular(type: LabelType, limit: Int): List<String>
}

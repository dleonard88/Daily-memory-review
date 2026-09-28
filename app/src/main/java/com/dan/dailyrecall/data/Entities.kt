package com.dan.dailyrecall.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class LabelType { PERSON, PLACE, TAG }

/** One entry per day. [date] is ISO yyyy-MM-dd, the day being recalled. */
@Entity(tableName = "entries", indices = [Index(value = ["date"], unique = true)])
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mood: Int,
    val what: String,
    val personalWins: String,
    val professionalWins: String,
    val decisions: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
)

/** People, places and tags share one table, distinguished by [type]. */
@Entity(tableName = "labels", indices = [Index(value = ["name", "type"], unique = true)])
data class Label(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
    val type: LabelType,
)

@Entity(
    tableName = "entry_labels",
    primaryKeys = ["entryId", "labelId"],
    foreignKeys = [
        ForeignKey(Entry::class, ["id"], ["entryId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(Label::class, ["id"], ["labelId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("labelId")],
)
data class EntryLabelCrossRef(val entryId: Long, val labelId: Long)

data class EntryWithLabels(
    @Embedded val entry: Entry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryLabelCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "labelId",
        ),
    )
    val labels: List<Label>,
)

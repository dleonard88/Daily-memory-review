package com.dan.dailyrecall

import android.app.Application
import com.dan.dailyrecall.data.AppDatabase
import com.dan.dailyrecall.data.EntryRepository

class DailyRecallApp : Application() {
    val database by lazy { AppDatabase.build(this) }
    val repository by lazy { EntryRepository(database) }
}

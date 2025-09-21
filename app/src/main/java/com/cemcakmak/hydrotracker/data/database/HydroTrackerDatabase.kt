package com.cemcakmak.hydrotracker.data.database

import androidx.room.*
import com.cemcakmak.hydrotracker.data.database.dao.WaterIntakeDao
import com.cemcakmak.hydrotracker.data.database.dao.DailySummaryDao
import com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry
import com.cemcakmak.hydrotracker.data.database.entities.DailySummary

@Database(
    entities = [
        WaterIntakeEntry::class,
        DailySummary::class
    ],
    version = 4,
    exportSchema = true
)
abstract class HydroTrackerDatabase : RoomDatabase() {

    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun dailySummaryDao(): DailySummaryDao

    companion object {
        const val DATABASE_NAME = "hydrotracker_database"
    }
}


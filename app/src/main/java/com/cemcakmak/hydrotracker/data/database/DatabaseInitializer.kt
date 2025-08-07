// DatabaseInitializer.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/data/database/DatabaseInitializer.kt

package com.cemcakmak.hydrotracker.data.database

import android.content.Context
import androidx.room.Room
import com.cemcakmak.hydrotracker.data.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.repository.UserRepository

object DatabaseInitializer {

    @Volatile
    private var database: HydroTrackerDatabase? = null

    @Volatile
    private var waterIntakeRepository: WaterIntakeRepository? = null

    fun getDatabase(context: Context): HydroTrackerDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                HydroTrackerDatabase::class.java,
                HydroTrackerDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // Remove in production
                .build()
            database = instance
            instance
        }
    }

    fun getWaterIntakeRepository(context: Context, userRepository: UserRepository): WaterIntakeRepository {
        return waterIntakeRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val instance = WaterIntakeRepository(
                waterIntakeDao = db.waterIntakeDao(),
                dailySummaryDao = db.dailySummaryDao(),
                userRepository = userRepository,
                context = context
            )
            waterIntakeRepository = instance
            instance
        }
    }
}
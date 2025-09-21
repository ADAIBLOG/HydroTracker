package com.cemcakmak.hydrotracker.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.repository.UserRepository

object DatabaseInitializer {

    @Volatile
    private var database: HydroTrackerDatabase? = null

    // Migration from version 1 to version 2
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Version 2 was used during development but never deployed
            // This migration should not be needed in production
        }
    }

    // Migration from version 1 to version 3 (adding health_connect_record_id)
    private val MIGRATION_1_3 = object : Migration(1, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new health_connect_record_id column to water_intake_entries table
            db.execSQL(
                "ALTER TABLE water_intake_entries ADD COLUMN health_connect_record_id TEXT"
            )
        }
    }

    // Migration from version 2 to version 3 (adding health_connect_record_id)
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new health_connect_record_id column to water_intake_entries table
            db.execSQL(
                "ALTER TABLE water_intake_entries ADD COLUMN health_connect_record_id TEXT"
            )
        }
    }

    // Migration from version 3 to version 4 (adding is_hidden)
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new is_hidden column to water_intake_entries table
            db.execSQL(
                "ALTER TABLE water_intake_entries ADD COLUMN is_hidden INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    fun getDatabase(context: Context): HydroTrackerDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                HydroTrackerDatabase::class.java,
                HydroTrackerDatabase.DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4)
                .build()
            database = instance
            instance
        }
    }

    fun getWaterIntakeRepository(context: Context, userRepository: UserRepository): WaterIntakeRepository {
        // Create a new repository instance each time to avoid memory leaks
        // Use applicationContext to prevent Activity context leaks
        val db = getDatabase(context)
        return WaterIntakeRepository(
            waterIntakeDao = db.waterIntakeDao(),
            dailySummaryDao = db.dailySummaryDao(),
            userRepository = userRepository,
            context = context.applicationContext // Use application context to prevent leaks
        )
    }
}
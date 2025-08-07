// WaterIntakeDao.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/data/database/dao/WaterIntakeDao.kt

package com.cemcakmak.hydrotracker.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry

@Dao
interface WaterIntakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WaterIntakeEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<WaterIntakeEntry>)

    @Query("SELECT * FROM water_intake_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: String): Flow<List<WaterIntakeEntry>>

    @Query("SELECT * FROM water_intake_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<WaterIntakeEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM water_intake_entries WHERE date = :date")
    fun getTotalIntakeForDate(date: String): Flow<Double>

    @Query("SELECT COUNT(*) FROM water_intake_entries WHERE date = :date")
    suspend fun getEntryCountForDate(date: String): Int

    @Query("SELECT * FROM water_intake_entries WHERE date >= date('now', '-30 days') ORDER BY timestamp DESC")
    fun getLast30DaysEntries(): Flow<List<WaterIntakeEntry>>

    @Update
    suspend fun updateEntry(entry: WaterIntakeEntry)

    @Delete
    suspend fun deleteEntry(entry: WaterIntakeEntry)

    @Query("DELETE FROM water_intake_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Query("DELETE FROM water_intake_entries")
    suspend fun deleteAllEntries()

    @Query("""
        SELECT date, SUM(amount) as totalAmount, COUNT(*) as entryCount
        FROM water_intake_entries 
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date 
        ORDER BY date ASC
    """)
    suspend fun getDailyTotals(startDate: String, endDate: String): List<DailyTotal>
}

data class DailyTotal(
    val date: String,
    val totalAmount: Double,
    val entryCount: Int
)


// WaterIntakeEntry.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/data/database/entities/WaterIntakeEntry.kt

package com.cemcakmak.hydrotracker.data.database.entities

import androidx.room.*

@Entity(
    tableName = "water_intake_entries",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["date"])
    ]
)
data class WaterIntakeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "container_type")
    val containerType: String,

    @ColumnInfo(name = "container_volume")
    val containerVolume: Double,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val hours = (timestamp / (1000 * 60 * 60)) % 24
        val minutes = (timestamp / (1000 * 60)) % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getFormattedAmount(): String {
        return when {
            amount >= 1000 -> "${String.format("%.1f", amount / 1000)} L"
            else -> "${amount.toInt()} ml"
        }
    }

    companion object {
        fun create(
            amount: Double,
            containerType: String,
            containerVolume: Double,
            note: String? = null
        ): WaterIntakeEntry {
            val now = System.currentTimeMillis()
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())

            return WaterIntakeEntry(
                amount = amount,
                timestamp = now,
                date = today,
                containerType = containerType,
                containerVolume = containerVolume,
                note = note
            )
        }
    }
}


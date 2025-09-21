package com.cemcakmak.hydrotracker.data.database.entities

import androidx.room.*
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "health_connect_record_id")
    val healthConnectRecordId: String? = null,

    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false
) {
    /**
     * Returns an ISO-8601 *time* string in UTC for the given Unix epoch milliseconds.
     */
    fun getFormattedTime(): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss'Z'")
            .withLocale(Locale.US)
            .withZone(ZoneOffset.UTC)
        return formatter.format(instant)
    }

    fun getFormattedAmount(): String {
        return if (amount >= 1000) {
            val liters = amount / 1000
            val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
            formatter.maximumFractionDigits = 1
            "${formatter.format(liters)} L"
        } else {
            "${amount.toInt()} ml"
        }
    }

    /**
     * Check if this entry was imported from an external Health Connect app
     */
    fun isExternalEntry(): Boolean {
        return note?.startsWith("Imported from ") == true
    }

    companion object {
        fun create(
            amount: Double,
            containerType: String,
            containerVolume: Double,
            note: String? = null
        ): WaterIntakeEntry {
            val now = System.currentTimeMillis()
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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


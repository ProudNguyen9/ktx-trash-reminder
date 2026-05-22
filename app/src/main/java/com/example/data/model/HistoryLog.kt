package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_logs")
data class HistoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: String // "FULL", "DUMPED", "CONFIG", "SYSTEM"
)

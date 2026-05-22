package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_state")
data class TrashState(
    @PrimaryKey val id: Int = 1,
    val currentTurnIndex: Int = 0, // 0 to 6 (represents standard index of roommates)
    val isTrashFull: Boolean = false,
    val reportedByName: String = "",
    val reportedAt: Long = 0L,
    val firebaseDbUrl: String = "",
    val firebaseApiKey: String = "",
    val firebaseProjectId: String = "",
    val resendApiKey: String = "",
    val webConfirmUrl: String = "",
    val lastSyncedAt: Long = 0L,
    val adminEmail: String = "nguyenhaohuu9@gmail.com",
    val adminPassword: String = "admin999"
)

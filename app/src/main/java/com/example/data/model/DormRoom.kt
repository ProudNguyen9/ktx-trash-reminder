package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dorm_rooms")
data class DormRoom(
    @PrimaryKey val roomName: String, // e.g. "Phòng D514"
    val adminName: String,
    val adminEmail: String,
    val adminPassword: String = "admin999"
)

data class LoginMatch(
    val roomName: String,
    val role: String, // "admin" or "user"
    val memberName: String
)

package com.example.data.model

import androidx.room.Entity

@Entity(tableName = "members", primaryKeys = ["roomName", "id"])
data class Member(
    val roomName: String,
    val id: Int, // 1 to 7 within the room
    val name: String,
    val email: String,
    val password: String = "user123",
    val isAbsent: Boolean = false,
    val turnOrder: Int = id,
    val absentReturnDistance: Int = -1
)

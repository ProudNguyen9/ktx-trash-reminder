package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey val id: Int, // 1 to 7
    val name: String,
    val email: String,
    val password: String = "user123"
)

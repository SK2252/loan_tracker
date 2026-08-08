package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarColorHex: String = "#2563EB",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

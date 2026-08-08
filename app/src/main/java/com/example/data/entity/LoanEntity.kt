package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["friendId"])]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: Long,
    val friendName: String,
    val loanAmount: Double,
    val startDate: Long,
    val repaymentMonths: Int,
    val monthlyAmount: Double,
    val totalRepayment: Double,
    val notes: String = "",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

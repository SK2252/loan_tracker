package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FriendDao
import com.example.data.dao.LoanDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.FriendEntity
import com.example.data.entity.LoanEntity
import com.example.data.entity.PaymentEntity

@Database(
    entities = [FriendEntity::class, LoanEntity::class, PaymentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LendFlowDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun loanDao(): LoanDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: LendFlowDatabase? = null

        fun getDatabase(context: Context): LendFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LendFlowDatabase::class.java,
                    "lendflow_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

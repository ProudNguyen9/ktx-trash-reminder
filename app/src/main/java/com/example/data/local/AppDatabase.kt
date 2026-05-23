package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DormRoom
import com.example.data.model.HistoryLog
import com.example.data.model.Member
import com.example.data.model.TrashState

@Database(
    entities = [DormRoom::class, Member::class, TrashState::class, HistoryLog::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dormRoomDao(): DormRoomDao
    abstract fun memberDao(): MemberDao
    abstract fun trashStateDao(): TrashStateDao
    abstract fun historyLogDao(): HistoryLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dorm_trash_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

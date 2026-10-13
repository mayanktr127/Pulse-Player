package com.pulse.player.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pulse.player.data.local.dao.BookmarkDao
import com.pulse.player.data.local.dao.DownloadDao
import com.pulse.player.data.local.dao.HistoryDao
import com.pulse.player.data.local.dao.PlaylistDao
import com.pulse.player.data.local.entity.BookmarkEntity
import com.pulse.player.data.local.entity.DownloadEntity
import com.pulse.player.data.local.entity.HistoryEntity
import com.pulse.player.data.local.entity.PlaylistEntity
import com.pulse.player.data.local.entity.PlaylistItemEntity

@Database(
    entities = [
        HistoryEntity::class,
        BookmarkEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulse_player.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.iotaslife.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iotaslife.app.data.local.dao.BitacoraDao
import com.iotaslife.app.data.local.dao.OrganizationDao
import com.iotaslife.app.data.local.dao.TaskDao
import com.iotaslife.app.data.local.entity.BitacoraEntity
import com.iotaslife.app.data.local.entity.OrganizationEntity
import com.iotaslife.app.data.local.entity.TaskEntity

@Database(
    entities = [
        OrganizationEntity::class,
        BitacoraEntity::class,
        TaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organizationDao(): OrganizationDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iotaslife_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}


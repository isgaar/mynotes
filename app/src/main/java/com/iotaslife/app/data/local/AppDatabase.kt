// File: app/src/main/java/com/iotaslife/app/data/local/AppDatabase.kt
// Description: Room database definition providing type-safe relational data access with safe schema migrations preserving all user data.
// All Rights Reserved isgaar

package com.iotaslife.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iotaslife.app.data.local.dao.BitacoraDao
import com.iotaslife.app.data.local.dao.ContingenciaDao
import com.iotaslife.app.data.local.dao.OrganizationDao
import com.iotaslife.app.data.local.dao.TaskDao
import com.iotaslife.app.data.local.entity.BitacoraEntity
import com.iotaslife.app.data.local.entity.ContingenciaEntity
import com.iotaslife.app.data.local.entity.OrganizationEntity
import com.iotaslife.app.data.local.entity.TaskEntity

@Database(
    entities = [
        OrganizationEntity::class,
        BitacoraEntity::class,
        TaskEntity::class,
        ContingenciaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organizationDao(): OrganizationDao
    abstract fun bitacoraDao(): BitacoraDao
    abstract fun taskDao(): TaskDao
    abstract fun contingenciaDao(): ContingenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `contingencias` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `organizationId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `reason` TEXT NOT NULL,
                        `solution` TEXT NOT NULL,
                        `dateEpochMillis` INTEGER NOT NULL,
                        `taxiCost` REAL NOT NULL,
                        `taxiDetails` TEXT NOT NULL,
                        `isReimbursed` INTEGER NOT NULL,
                        `imagePathsString` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`organizationId`) REFERENCES `organizations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_contingencias_organizationId` ON `contingencias` (`organizationId`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iotaslife_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

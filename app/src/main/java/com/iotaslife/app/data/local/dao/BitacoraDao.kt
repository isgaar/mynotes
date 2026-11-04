package com.iotaslife.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iotaslife.app.data.local.entity.BitacoraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraDao {
    @Query("SELECT * FROM bitacoras WHERE organizationId = :orgId ORDER BY dateEpochMillis DESC, createdAt DESC")
    fun getEntriesForOrganization(orgId: Long): Flow<List<BitacoraEntity>>

    @Query("SELECT * FROM bitacoras WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): BitacoraEntity?

    @Query("""
        SELECT * FROM bitacoras 
        WHERE organizationId = :orgId 
          AND (title LIKE '%' || :query || '%' 
               OR ticketCode LIKE '%' || :query || '%' 
               OR workDone LIKE '%' || :query || '%'
               OR observations LIKE '%' || :query || '%'
               OR actionsTaken LIKE '%' || :query || '%')
        ORDER BY dateEpochMillis DESC
    """)
    fun searchEntries(orgId: Long, query: String): Flow<List<BitacoraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BitacoraEntity): Long

    @Update
    suspend fun update(entry: BitacoraEntity)

    @Delete
    suspend fun delete(entry: BitacoraEntity)

    @Query("DELETE FROM bitacoras WHERE id = :id")
    suspend fun deleteById(id: Long)
}

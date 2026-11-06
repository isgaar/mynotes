// File: app/src/main/java/com/iotaslife/app/data/local/dao/ContingenciaDao.kt
// Description: Data Access Object providing reactive queries and persistence operations for contingency records.
// All Rights Reserved isgaar

package com.iotaslife.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iotaslife.app.data.local.entity.ContingenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContingenciaDao {
    @Query("SELECT * FROM contingencias WHERE organizationId = :organizationId ORDER BY dateEpochMillis DESC")
    fun getContingenciasForOrganization(organizationId: Long): Flow<List<ContingenciaEntity>>

    @Query("SELECT * FROM contingencias WHERE id = :id LIMIT 1")
    suspend fun getContingenciaById(id: Long): ContingenciaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contingencia: ContingenciaEntity): Long

    @Update
    suspend fun update(contingencia: ContingenciaEntity)

    @Delete
    suspend fun delete(contingencia: ContingenciaEntity)

    @Query("DELETE FROM contingencias WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE contingencias SET isReimbursed = :isReimbursed, updatedAt = :updatedAt WHERE id = :id")
    suspend fun toggleReimbursed(id: Long, isReimbursed: Boolean, updatedAt: Long)
}


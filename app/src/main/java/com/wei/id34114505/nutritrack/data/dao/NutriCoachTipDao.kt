package com.wei.id34114505.nutritrack.data.dao

import androidx.room.*
import com.wei.id34114505.nutritrack.data.entities.NutriCoachTip
import kotlinx.coroutines.flow.Flow

@Dao
interface NutriCoachTipDao {
    @Insert
    suspend fun insert(tip: NutriCoachTip): Long

    @Query("SELECT * FROM nutricoach_tips WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getTipsByPatientId(patientId: String): Flow<List<NutriCoachTip>>

    @Query("DELETE FROM nutricoach_tips")
    suspend fun deleteAllTips()
}
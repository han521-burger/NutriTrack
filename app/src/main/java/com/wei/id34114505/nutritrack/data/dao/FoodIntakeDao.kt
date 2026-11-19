package com.wei.id34114505.nutritrack.data.dao

import androidx.room.*
import com.wei.id34114505.nutritrack.data.entities.FoodIntake
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodIntake: FoodIntake): Long

    @Update
    suspend fun update(foodIntake: FoodIntake)

    @Query("SELECT * FROM food_intakes WHERE patientId = :patientId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFoodIntakeByPatientId(patientId: String): FoodIntake?

    @Query("SELECT * FROM food_intakes WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getFoodIntakesByPatientId(patientId: String): Flow<List<FoodIntake>>

    @Query("SELECT * FROM food_intakes")
    fun getAllFoodIntakes(): Flow<List<FoodIntake>>

    @Query("DELETE FROM food_intakes")
    suspend fun deleteAllFoodIntakes()
}
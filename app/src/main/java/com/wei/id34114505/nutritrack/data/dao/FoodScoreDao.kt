package com.wei.id34114505.nutritrack.data.dao

import androidx.room.*
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodScore: FoodScore)

    @Update
    suspend fun update(foodScore: FoodScore)

    @Query("SELECT * FROM food_scores WHERE patientId = :patientId")
    suspend fun getFoodScoreByPatientId(patientId: String): FoodScore?

    @Query("SELECT * FROM food_scores")
    fun getAllFoodScores(): Flow<List<FoodScore>>

    @Query("SELECT AVG(hEIFAtotalscoreMale) FROM food_scores WHERE hEIFAtotalscoreMale IS NOT NULL")
    fun getAverageMaleScore(): Flow<Float>

    @Query("SELECT AVG(hEIFAtotalscoreFemale) FROM food_scores WHERE hEIFAtotalscoreFemale IS NOT NULL")
    fun getAverageFemaleScore(): Flow<Float>

    @Query("SELECT AVG(fruitHEIFAscoreMale) FROM food_scores WHERE fruitHEIFAscoreMale IS NOT NULL")
    fun getAverageMaleFruitScore(): Flow<Float>

    @Query("SELECT AVG(fruitHEIFAscoreFemale) FROM food_scores WHERE fruitHEIFAscoreFemale IS NOT NULL")
    fun getAverageFemaleFruitScore(): Flow<Float>

    @Query("DELETE FROM food_scores")
    suspend fun deleteAllFoodScores()
}
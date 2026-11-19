package com.wei.id34114505.nutritrack.data.repositories

import com.wei.id34114505.nutritrack.data.dao.FoodScoreDao
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling FoodScore-related operations
 */
class FoodScoreRepository(private val foodScoreDao: FoodScoreDao) {
    /**
     * Insert a new food score into the database
     */
    suspend fun insert(foodScore: FoodScore) {
        foodScoreDao.insert(foodScore)
    }

    /**
     * Update existing food score in the database
     */
    suspend fun update(foodScore: FoodScore) {
        foodScoreDao.update(foodScore)
    }

    /**
     * Get a food score by patient ID
     */
    suspend fun getFoodScoreByPatientId(patientId: String): FoodScore? {
        return foodScoreDao.getFoodScoreByPatientId(patientId)
    }

    /**
     * Get all food scores as a Flow
     */
    fun getAllFoodScores(): Flow<List<FoodScore>> {
        return foodScoreDao.getAllFoodScores()
    }

    /**
     * Get the average HEIFA score for male patients
     */
    fun getAverageMaleScore(): Flow<Float> {
        return foodScoreDao.getAverageMaleScore()
    }

    /**
     * Get the average HEIFA score for female patients
     */
    fun getAverageFemaleScore(): Flow<Float> {
        return foodScoreDao.getAverageFemaleScore()
    }

    /**
     * Get the average fruit score for male patients
     */
    fun getAverageMaleFruitScore(): Flow<Float> {
        return foodScoreDao.getAverageMaleFruitScore()
    }

    /**
     * Get the average fruit score for female patients
     */
    fun getAverageFemaleFruitScore(): Flow<Float> {
        return foodScoreDao.getAverageFemaleFruitScore()
    }
}
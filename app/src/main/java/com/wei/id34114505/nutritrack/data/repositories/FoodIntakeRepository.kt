package com.wei.id34114505.nutritrack.data.repositories

import com.wei.id34114505.nutritrack.data.dao.FoodIntakeDao
import com.wei.id34114505.nutritrack.data.entities.FoodIntake
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling FoodIntake-related operations
 */
class FoodIntakeRepository(private val foodIntakeDao: FoodIntakeDao) {
    /**
     * Insert a new food intake record into the database
     */
    suspend fun insert(foodIntake: FoodIntake): Long {
        return foodIntakeDao.insert(foodIntake)
    }

    /**
     * Update existing food intake record in the database
     */
    suspend fun update(foodIntake: FoodIntake) {
        foodIntakeDao.update(foodIntake)
    }

    /**
     * Get the latest food intake record for a specific user
     */
    suspend fun getLatestFoodIntakeByPatientId(patientId: String): FoodIntake? {
        return foodIntakeDao.getLatestFoodIntakeByPatientId(patientId)
    }

    /**
     * Get all food intake records for a specific user as a Flow
     */
    fun getFoodIntakesByPatientId(patientId: String): Flow<List<FoodIntake>> {
        return foodIntakeDao.getFoodIntakesByPatientId(patientId)
    }

    /**
     * Get all food intake records as a Flow
     */
    fun getAllFoodIntakes(): Flow<List<FoodIntake>> {
        return foodIntakeDao.getAllFoodIntakes()
    }
}
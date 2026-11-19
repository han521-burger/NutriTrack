package com.wei.id34114505.nutritrack.data.repositories

import com.wei.id34114505.nutritrack.data.dao.NutriCoachTipDao
import com.wei.id34114505.nutritrack.data.entities.NutriCoachTip
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling NutriCoachTip-related operations
 */
class NutriCoachTipRepository(private val nutriCoachTipDao: NutriCoachTipDao) {
    /**
     * Insert a new nutrition coach tip into the database
     */
    suspend fun insert(tip: NutriCoachTip): Long {
        return nutriCoachTipDao.insert(tip)
    }

    /**
     * Get all nutrition coach tips for a specific user as a Flow
     */
    fun getTipsByPatientId(patientId: String): Flow<List<NutriCoachTip>> {
        return nutriCoachTipDao.getTipsByPatientId(patientId)
    }
}
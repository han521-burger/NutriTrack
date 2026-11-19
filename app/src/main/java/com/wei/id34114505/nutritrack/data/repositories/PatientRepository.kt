package com.wei.id34114505.nutritrack.data.repositories

import com.wei.id34114505.nutritrack.data.dao.PatientDao
import com.wei.id34114505.nutritrack.data.entities.Patient
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling Patient-related operations
 */
class PatientRepository(private val patientDao: PatientDao) {
    /**
     * Insert a new patient into the database
     */
    suspend fun insert(patient: Patient) {
        patientDao.insert(patient)
    }

    /**
     * Update existing patient in the database
     */
    suspend fun update(patient: Patient) {
        patientDao.update(patient)
    }

    /**
     * Get a patient by their ID
     */
    suspend fun getPatientById(patientId: String): Patient? {
        return patientDao.getPatientById(patientId)
    }

    /**
     * Get a patient by their phone number
     */
    suspend fun getPatientByPhoneNumber(phoneNumber: String): Patient? {
        return patientDao.getPatientByPhoneNumber(phoneNumber)
    }

    /**
     * Validate patient credentials for login
     */
    suspend fun validatePatientCredentials(patientId: String, password: String): Patient? {
        return patientDao.validatePatientCredentials(patientId, password)
    }

    /**
     * Get all patients as a Flow
     */
    fun getAllPatients(): Flow<List<Patient>> {
        return patientDao.getAllPatients()
    }

    /**
     * Claim a patient account by setting password
     */
    suspend fun claimAccount(patientId: String, password: String) {
        patientDao.claimAccount(patientId, password)
    }

    /**
     * Get the total count of patients
     */
    suspend fun getPatientCount(): Int {
        return patientDao.getPatientCount()
    }
}
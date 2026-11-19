package com.wei.id34114505.nutritrack.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wei.id34114505.nutritrack.data.NutriTrackDatabase
import com.wei.id34114505.nutritrack.data.entities.Patient
import com.wei.id34114505.nutritrack.data.repositories.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling Patient-related operations
 */
class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PatientRepository
    val allPatients: Flow<List<Patient>>

    private val _currentPatient = MutableLiveData<Patient?>()
    val currentPatient: LiveData<Patient?> = _currentPatient

    init {
        val patientDao = NutriTrackDatabase.getDatabase(application).patientDao()
        repository = PatientRepository(patientDao)
        allPatients = repository.getAllPatients()
    }

    /**
     * Insert a new patient
     */
    fun insert(patient: Patient) = viewModelScope.launch {
        repository.insert(patient)
    }

    /**
     * Update an existing patient
     */
    fun update(patient: Patient) = viewModelScope.launch {
        repository.update(patient)
    }

    /**
     * Load a patient by their ID
     */
    fun loadPatientById(patientId: String) = viewModelScope.launch {
        _currentPatient.value = repository.getPatientById(patientId)
    }

    /**
     * Validate patient login credentials
     */
    suspend fun validateLogin(patientId: String, password: String): Patient? {
        return repository.validatePatientCredentials(patientId, password)
    }

    /**
     * Get a patient by their ID and check if their phone number matches
     * Used for first-time login validation
     */
    suspend fun getPatientByPhone(patientId: String, phoneNumber: String): Patient? {
        val patient = repository.getPatientById(patientId)
        // Check if patient exists and phone number matches
        return if (patient != null && patient.phoneNumber == phoneNumber) {
            patient
        } else {
            null
        }
    }

    /**
     * Claim a patient account by setting password
     */
    suspend fun claimAccount(patientId: String, password: String): Boolean {
        try {
            repository.claimAccount(patientId, password)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Get the count of patients in the database
     */
    suspend fun getPatientCount(): Int {
        return repository.getPatientCount()
    }

    /**
     * Factory class for creating PatientViewModel instances with proper context
     */
    class PatientViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PatientViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
package com.wei.id34114505.nutritrack.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wei.id34114505.nutritrack.data.NutriTrackDatabase
import com.wei.id34114505.nutritrack.data.entities.FoodIntake
import com.wei.id34114505.nutritrack.data.repositories.FoodIntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling FoodIntake-related operations
 */
class FoodIntakeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodIntakeRepository

    init {
        val foodIntakeDao = NutriTrackDatabase.getDatabase(application).foodIntakeDao()
        repository = FoodIntakeRepository(foodIntakeDao)
    }

    /**
     * Insert a new food intake record
     */
    fun insert(foodIntake: FoodIntake) = viewModelScope.launch {
        repository.insert(foodIntake)
    }

    /**
     * Update an existing food intake record
     */
    fun update(foodIntake: FoodIntake) = viewModelScope.launch {
        repository.update(foodIntake)
    }

    /**
     * Get the latest food intake for a specific user
     */
    suspend fun getLatestFoodIntakeByPatientId(patientId: String): FoodIntake? {
        return repository.getLatestFoodIntakeByPatientId(patientId)
    }

    /**
     * Get all food intake records for a specific user
     */
    fun getFoodIntakesByPatientId(patientId: String): Flow<List<FoodIntake>> {
        return repository.getFoodIntakesByPatientId(patientId)
    }

    /**
     * Factory class for creating FoodIntakeViewModel instances with proper context
     */
    class FoodIntakeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FoodIntakeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FoodIntakeViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
package com.wei.id34114505.nutritrack.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wei.id34114505.nutritrack.data.NutriTrackDatabase
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import com.wei.id34114505.nutritrack.data.repositories.FoodScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling FoodScore-related operations
 */
class FoodScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodScoreRepository
    val allFoodScores: Flow<List<FoodScore>>
    val averageMaleScore: Flow<Float>
    val averageFemaleScore: Flow<Float>
    val averageMaleFruitScore: Flow<Float>
    val averageFemaleFruitScore: Flow<Float>

    init {
        val foodScoreDao = NutriTrackDatabase.getDatabase(application).foodScoreDao()
        repository = FoodScoreRepository(foodScoreDao)
        allFoodScores = repository.getAllFoodScores()
        averageMaleScore = repository.getAverageMaleScore()
        averageFemaleScore = repository.getAverageFemaleScore()
        averageMaleFruitScore = repository.getAverageMaleFruitScore()
        averageFemaleFruitScore = repository.getAverageFemaleFruitScore()
    }

    /**
     * Insert a new food score
     */
    fun insert(foodScore: FoodScore) = viewModelScope.launch {
        repository.insert(foodScore)
    }

    /**
     * Update an existing food score
     */
    fun update(foodScore: FoodScore) = viewModelScope.launch {
        repository.update(foodScore)
    }

    /**
     * Get a food score by user ID
     */
    suspend fun getFoodScoreByPatientId(patientId: String): FoodScore? {
        return repository.getFoodScoreByPatientId(patientId)
    }

    /**
     * Factory class for creating FoodScoreViewModel instances with proper context
     */
    class FoodScoreViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FoodScoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FoodScoreViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
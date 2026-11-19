package com.wei.id34114505.nutritrack.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wei.id34114505.nutritrack.api.FruitApiService
import com.wei.id34114505.nutritrack.api.FruitResponse
import com.wei.id34114505.nutritrack.data.NutriTrackDatabase
import com.wei.id34114505.nutritrack.data.entities.NutriCoachTip
import com.wei.id34114505.nutritrack.data.repositories.NutriCoachTipRepository
import com.wei.id34114505.nutritrack.utils.GenAIUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling NutriCoach tip-related operations
 */
class NutriCoachViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NutriCoachTipRepository
    private val fruitApiService = FruitApiService.create()

    init {
        val nutriCoachTipDao = NutriTrackDatabase.getDatabase(application).nutriCoachTipDao()
        repository = NutriCoachTipRepository(nutriCoachTipDao)
    }

    /**
     * Save a new nutrition coach tip
     */
    fun saveTip(patientId: String, content: String) = viewModelScope.launch {
        val tip = NutriCoachTip(
            patientId = patientId,
            content = content
        )
        repository.insert(tip)
    }

    /**
     * Get all tips for a specific patient
     */
    fun getTipsByPatientId(patientId: String): Flow<List<NutriCoachTip>> {
        return repository.getTipsByPatientId(patientId)
    }

    /**
     * Search for a fruit using the Fruityvice API
     */
    suspend fun searchFruit(fruitName: String): FruitResponse? {
        return try {
            val response = fruitApiService.getFruitByName(fruitName)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate a motivational tip for the patient and save it to the database
     */
    fun generateMotivationalTip(patientId: String, fruitScore: Float) = viewModelScope.launch {
        // Create patient context for better AI responses
        val patientData = "This patient consumes approximately ${(fruitScore * 0.5).toInt()} servings of fruit daily."

        // Generate tip using GenAI
        val tipContent = GenAIUtil.generateFruitMotivationalMessage(fruitScore, patientData)

        // Save tip to database
        val tip = NutriCoachTip(
            patientId = patientId,
            content = tipContent
        )

        repository.insert(tip)
    }

    /**
     * Factory class for creating NutriCoachViewModel instances with proper context
     */
    class NutriCoachViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NutriCoachViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NutriCoachViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
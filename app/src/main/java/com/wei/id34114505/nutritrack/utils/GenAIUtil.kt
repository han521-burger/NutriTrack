package com.wei.id34114505.nutritrack.utils

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for generating AI-powered content using the Gemini API
 */
object GenAIUtil {
    private const val TAG = "GenAIUtil"
    private var apiKeyInitialized = false
    private lateinit var generativeModel: GenerativeModel

    /**
     * Initialize the GenAI model with API key
     * Must be called before using any of the generation methods
     */
    fun initialize(apiKey: String) {
        Log.d(TAG, "Initializing GenAI with API key length: ${apiKey.length}")

        if (apiKey.isBlank()) {
            Log.e(TAG, "Cannot initialize GenAI with empty API key")
            return
        }

        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
            apiKeyInitialized = true
            Log.d(TAG, "GenAI model initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize GenAI model", e)
            apiKeyInitialized = false
        }
    }

    /**
     * Check if the API key is properly initialized
     */
    private fun checkInitialized(): Boolean {
        val initialized = apiKeyInitialized && ::generativeModel.isInitialized
        if (!initialized) {
            Log.e(TAG, "GenAI not initialized. Call initialize() with valid API key first.")
        }
        return initialized
    }

    /**
     * Generate a motivational message for improving fruit intake
     * @param fruitScore User's current fruit score (0-10)
     * @param userData Additional context about the user (optional)
     * @return A personalized motivational message
     */
    suspend fun generateFruitMotivationalMessage(fruitScore: Float, userData: String = ""): String {
        Log.d(TAG, "generateFruitMotivationalMessage called with score: $fruitScore")

        // Fallback messages in case of API failure
        val fallbackMessages = when {
            fruitScore >= 8.0f ->
                "Fantastic job with your fruit intake! You're getting a great variety of nutrients. Keep up the excellent work and try experimenting with seasonal fruits for even more diversity."
            fruitScore >= 5.0f ->
                "You're doing well with your fruit intake, but there's room for improvement. Try adding a piece of fruit to your breakfast or as a snack. Berries, apples, and citrus fruits are great options that are easy to incorporate into your daily routine."
            fruitScore >= 3.0f ->
                "Your fruit intake could use some attention. Fruits provide essential vitamins, minerals, and fiber. Consider setting a goal to eat at least 2 servings of fruit each day. Start with fruits you already enjoy and gradually explore new options."
            else ->
                "Your fruit intake needs improvement. Fruits are an important part of a balanced diet, providing crucial nutrients and antioxidants. Start small by adding one serving of fruit each day, perhaps as a snack or dessert alternative. Even small changes can make a big difference to your health over time."
        }

        // If not initialized, return fallback message
        if (!checkInitialized()) {
            Log.w(TAG, "GenAI not initialized, returning fallback message")
            return fallbackMessages
        }

        // Prepare prompt based on user's fruit score
        val prompt = "Generate a short encouraging message (2-3 sentences) to help someone improve their fruit intake. " +
                "The person's current fruit score is $fruitScore out of 10. " +
                if (userData.isNotBlank()) userData else ""

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending prompt to Gemini API...")
                val response = generativeModel.generateContent(prompt)
                val generatedText = response.text?.trim()

                Log.d(TAG, "Gemini API response received: ${generatedText?.take(50)}...")

                // Return AI-generated text or fallback if empty
                generatedText?.takeIf { it.isNotBlank() } ?: fallbackMessages
            } catch (e: Exception) {
                Log.e(TAG, "Error generating fruit motivational message", e)
                Log.e(TAG, "Error details: ${e.message}")
                fallbackMessages
            }
        }
    }

    /**
     * Generate data patterns based on nutrition data
     * @param nutritionData String representation of nutrition data to analyze
     * @return List of identified patterns
     */
    suspend fun generateDataPatterns(nutritionData: String = ""): List<String> {
        Log.d(TAG, "generateDataPatterns called with data: ${nutritionData.take(100)}...")

        // Fallback patterns in case of API failure
        val fallbackPatterns = listOf(
            "Users with high vegetable scores also tend to have high fruit scores, suggesting a consistent preference for plant-based foods.",
            "Female users demonstrate higher average fruit consumption compared to male users.",
            "Users with low discretionary food scores generally have higher overall HEIFA scores, indicating healthier eating patterns."
        )

        // If not initialized, return fallback patterns
        if (!checkInitialized()) {
            Log.w(TAG, "GenAI not initialized, returning fallback patterns")
            return fallbackPatterns
        }

        // Prepare analysis prompt
        val dataToAnalyze = if (nutritionData.isBlank()) {
            "Based on nutrition data from users"
        } else {
            "Based on this nutrition data: $nutritionData"
        }

        val prompt = "Analyze nutrition data and identify exactly 3 interesting patterns in the data. " +
                "$dataToAnalyze. " +
                "Format your response as 3 separate insights, each on its own line. " +
                "Be specific about food group correlations and gender differences if they exist."

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending pattern analysis prompt to Gemini API...")
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text?.trim()

                Log.d(TAG, "Gemini API pattern response received: ${responseText?.take(100)}...")

                if (responseText.isNullOrBlank()) {
                    Log.w(TAG, "Empty response from Gemini API, using fallback patterns")
                    return@withContext fallbackPatterns
                }

                // Process response into a list of patterns
                val patterns = responseText.split('\n')
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                    .take(3) // Ensure we only return 3 patterns max

                Log.d(TAG, "Parsed ${patterns.size} patterns from response")

                if (patterns.isNotEmpty()) patterns else fallbackPatterns
            } catch (e: Exception) {
                Log.e(TAG, "Error generating data patterns", e)
                Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${e.message}")
                e.printStackTrace()
                fallbackPatterns
            }
        }
    }
}
package com.wei.id34114505.nutritrack

import android.app.Application
import android.util.Log
import com.wei.id34114505.nutritrack.data.DatabaseInitializer
import com.wei.id34114505.nutritrack.data.NutriTrackDatabase
import com.wei.id34114505.nutritrack.data.SessionManager
import com.wei.id34114505.nutritrack.data.repositories.FoodScoreRepository
import com.wei.id34114505.nutritrack.data.repositories.PatientRepository
import com.wei.id34114505.nutritrack.utils.DataMigrationUtil
import com.wei.id34114505.nutritrack.utils.GenAIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Application class for NutriTrack
 */
class NutriTrackApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        Log.d("NutriTrackApplication", "Application onCreate() called")

        // Initialize SessionManager first
        SessionManager.init(this)
        Log.d("NutriTrackApplication", "SessionManager initialized")

        // Initialize GenAI utility with API key - CRITICAL FIX
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank()) {
                GenAIUtil.initialize(apiKey)
                Log.d("NutriTrackApplication", "GenAI initialized successfully with key")
            } else {
                Log.e("NutriTrackApplication", "GEMINI_API_KEY is blank!")
            }
        } catch (e: Exception) {
            Log.e("NutriTrackApplication", "Failed to initialize GenAI", e)
        }

        // Initialize database in background
        applicationScope.launch {
            try {
                Log.d("NutriTrackApplication", "Starting database initialization")

                val database = NutriTrackDatabase.getDatabase(this@NutriTrackApplication)
                val initializer = DatabaseInitializer(this@NutriTrackApplication)
                initializer.initializeDatabase()

                // Create repositories
                val patientRepository = PatientRepository(database.patientDao())
                val foodScoreRepository = FoodScoreRepository(database.foodScoreDao())

                // Create migration utility
                val migrationUtil = DataMigrationUtil(
                    context = this@NutriTrackApplication,
                    patientRepository = patientRepository,
                    foodScoreRepository = foodScoreRepository
                )

                // Perform migration
                val migrationPerformed = migrationUtil.migrateDataIfNeeded()
                Log.d("NutriTrackApplication", "Data migration performed: $migrationPerformed")

            } catch (e: Exception) {
                Log.e("NutriTrackApplication", "Error during database initialization", e)
            }
        }
    }
}
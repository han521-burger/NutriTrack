package com.wei.id34114505.nutritrack.utils

import android.content.Context
import android.util.Log
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import com.wei.id34114505.nutritrack.data.entities.Patient
import com.wei.id34114505.nutritrack.data.repositories.FoodScoreRepository
import com.wei.id34114505.nutritrack.data.repositories.PatientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class for migrating data from CSV to Room database
 */
class DataMigrationUtil(
    private val context: Context,
    private val patientRepository: PatientRepository,
    private val foodScoreRepository: FoodScoreRepository
) {
    companion object {
        private const val TAG = "DataMigrationUtil"
        private const val CSV_FILENAME = "nutritrack_data.csv"
    }

    /**
     * Migrate data from CSV to database if needed
     * @return true if migration was performed, false if database already had data
     */
    suspend fun migrateDataIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        // Check if we need to migrate (database is empty)
        val patientCount = patientRepository.getPatientCount()
        if (patientCount > 0) {
            Log.d(TAG, "Database already has $patientCount patients, skipping migration")
            return@withContext false
        }

        // Migrate data from CSV
        try {
            val inputStream = context.assets.open(CSV_FILENAME)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Read header row
            val headerLine = reader.readLine() ?: return@withContext false
            val headers = headerLine.split(",")

            // Find column indices (keeping User_ID from CSV but mapping to patientId)
            val userIdIndex = headers.indexOf("User_ID")
            val phoneNumberIndex = headers.indexOf("PhoneNumber")
            val sexIndex = headers.indexOf("Sex")
            val maleScoreIndex = headers.indexOf("HEIFAtotalscoreMale")
            val femaleScoreIndex = headers.indexOf("HEIFAtotalscoreFemale")

            // Map all other HEIFA score columns
            val columnIndices = mapOf(
                "discretionaryHEIFAscoreMale" to headers.indexOf("DiscretionaryHEIFAscoreMale"),
                "discretionaryHEIFAscoreFemale" to headers.indexOf("DiscretionaryHEIFAscoreFemale"),
                "vegetablesHEIFAscoreMale" to headers.indexOf("VegetablesHEIFAscoreMale"),
                "vegetablesHEIFAscoreFemale" to headers.indexOf("VegetablesHEIFAscoreFemale"),
                "fruitHEIFAscoreMale" to headers.indexOf("FruitHEIFAscoreMale"),
                "fruitHEIFAscoreFemale" to headers.indexOf("FruitHEIFAscoreFemale"),
                "grainsAndCerealsHEIFAscoreMale" to headers.indexOf("GrainsandcerealsHEIFAscoreMale"),
                "grainsAndCerealsHEIFAscoreFemale" to headers.indexOf("GrainsandcerealsHEIFAscoreFemale"),
                "wholeGrainsHEIFAscoreMale" to headers.indexOf("WholegrainsHEIFAscoreMale"),
                "wholeGrainsHEIFAscoreFemale" to headers.indexOf("WholegrainsHEIFAscoreFemale"),
                "meatAndAlternativesHEIFAscoreMale" to headers.indexOf("MeatandalternativesHEIFAscoreMale"),
                "meatAndAlternativesHEIFAscoreFemale" to headers.indexOf("MeatandalternativesHEIFAscoreFemale"),
                "dairyAndAlternativesHEIFAscoreMale" to headers.indexOf("DairyandalternativesHEIFAscoreMale"),
                "dairyAndAlternativesHEIFAscoreFemale" to headers.indexOf("DairyandalternativesHEIFAscoreFemale"),
                "sodiumHEIFAscoreMale" to headers.indexOf("SodiumHEIFAscoreMale"),
                "sodiumHEIFAscoreFemale" to headers.indexOf("SodiumHEIFAscoreFemale"),
                "alcoholHEIFAscoreMale" to headers.indexOf("AlcoholHEIFAscoreMale"),
                "alcoholHEIFAscoreFemale" to headers.indexOf("AlcoholHEIFAscoreFemale"),
                "waterHEIFAscoreMale" to headers.indexOf("WaterHEIFAscoreMale"),
                "waterHEIFAscoreFemale" to headers.indexOf("WaterHEIFAscoreFemale"),
                "sugarHEIFAscoreMale" to headers.indexOf("SugarHEIFAscoreMale"),
                "sugarHEIFAscoreFemale" to headers.indexOf("SugarHEIFAscoreFemale"),
                "saturatedFatHEIFAscoreMale" to headers.indexOf("SaturatedFatHEIFAscoreMale"),
                "saturatedFatHEIFAscoreFemale" to headers.indexOf("SaturatedFatHEIFAscoreFemale"),
                "unsaturatedFatHEIFAscoreMale" to headers.indexOf("UnsaturatedFatHEIFAscoreMale"),
                "unsaturatedFatHEIFAscoreFemale" to headers.indexOf("UnsaturatedFatHEIFAscoreFemale")
            )

            // Validate required columns
            if (userIdIndex == -1 || phoneNumberIndex == -1 || sexIndex == -1 ||
                maleScoreIndex == -1 || femaleScoreIndex == -1) {
                Log.e(TAG, "Required columns not found in CSV: userId=$userIdIndex, " +
                        "phoneNumber=$phoneNumberIndex, sex=$sexIndex, " +
                        "maleScore=$maleScoreIndex, femaleScore=$femaleScoreIndex")
                return@withContext false
            }

            // Process each data row
            var line: String?
            var migrationCount = 0

            while (reader.readLine().also { line = it } != null) {
                val values = line!!.split(",")

                if (values.size <= userIdIndex) continue

                // Extract patient data (map User_ID to patientId)
                val patientId = values[userIdIndex].trim()
                val phoneNumber = if (values.size > phoneNumberIndex) values[phoneNumberIndex].trim() else ""
                val sex = if (values.size > sexIndex) values[sexIndex].trim() else "Male"

                // Create patient
                val patient = Patient(
                    patientId = patientId,
                    phoneNumber = phoneNumber,
                    patientName = "Patient $patientId", // Default name
                    sex = sex,
                    password = "", // Empty password initially
                    isClaimed = false // Not claimed yet
                )

                // Insert patient
                patientRepository.insert(patient)

                // Extract score data
                val totalScoreMale = if (values.size > maleScoreIndex)
                    values[maleScoreIndex].trim().toFloatOrNull() else null
                val totalScoreFemale = if (values.size > femaleScoreIndex)
                    values[femaleScoreIndex].trim().toFloatOrNull() else null

                // Create food score
                val foodScore = FoodScore(
                    patientId = patientId,
                    hEIFAtotalscoreMale = totalScoreMale,
                    hEIFAtotalscoreFemale = totalScoreFemale
                )

                // Set individual category scores
                val foodScoreWithCategories = foodScore.copy().let { score ->
                    var updatedScore = score

                    columnIndices.forEach { (fieldName, index) ->
                        if (index != -1 && values.size > index) {
                            val value = values[index].trim().toFloatOrNull()

                            // Use reflection to set the value dynamically
                            try {
                                val field = FoodScore::class.java.getDeclaredField(fieldName)
                                field.isAccessible = true
                                field.set(updatedScore, value)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error setting field $fieldName: ${e.message}")
                            }
                        }
                    }

                    updatedScore
                }

                // Insert food score
                foodScoreRepository.insert(foodScoreWithCategories)

                migrationCount++
            }

            reader.close()

            Log.d(TAG, "Successfully migrated $migrationCount records from CSV to database")

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating data: ${e.message}", e)
            return@withContext false
        }
    }
}
package com.wei.id34114505.nutritrack.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import com.wei.id34114505.nutritrack.data.entities.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Handles database initialization and data migration from CSV
 */
class DatabaseInitializer(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "nutritrack_prefs", Context.MODE_PRIVATE
    )

    companion object {
        private const val TAG = "DatabaseInitializer"
        private const val PREF_DB_INITIALIZED = "db_initialized"
        private const val CSV_FILENAME = "nutritrack_data.csv"
    }

    /**
     * Check if database has been initialized already
     */
    private fun isDatabaseInitialized(): Boolean {
        return sharedPreferences.getBoolean(PREF_DB_INITIALIZED, false)
    }

    /**
     * Mark database as initialized
     */
    private fun markDatabaseInitialized() {
        sharedPreferences.edit().putBoolean(PREF_DB_INITIALIZED, true).apply()
    }

    /**
     * Safe float parser with error handling
     */
    private fun safeParseFloat(value: String?): Float? {
        return try {
            value?.trim()?.toFloatOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing float value: $value", e)
            null
        }
    }

    /**
     * Safe value getter from CSV row
     */
    private fun getValueSafe(values: List<String>, index: Int): String? {
        return if (index >= 0 && index < values.size) {
            values[index].trim().takeIf { it.isNotEmpty() }
        } else {
            null
        }
    }

    /**
     * Initialize database by migrating data from CSV if needed
     */
    suspend fun initializeDatabase() {
        // Check if already initialized
        if (isDatabaseInitialized()) {
            Log.d(TAG, "Database already initialized, skipping migration")
            return
        }

        // Perform migration
        withContext(Dispatchers.IO) {
            try {
                val database = NutriTrackDatabase.getDatabase(context)
                val patientDao = database.patientDao()
                val foodScoreDao = database.foodScoreDao()

                // Get patient count to double-check if database is empty
                val patientCount = patientDao.getPatientCount()
                if (patientCount > 0) {
                    Log.d(TAG, "Database already has data, skipping migration")
                    markDatabaseInitialized()
                    return@withContext
                }

                // Open CSV file
                val inputStream = context.assets.open(CSV_FILENAME)
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Read header row
                val headerLine = reader.readLine()
                if (headerLine == null) {
                    Log.e(TAG, "CSV file is empty or invalid")
                    return@withContext
                }

                val headers = headerLine.split(",").map { it.trim() }
                Log.d(TAG, "CSV Headers: ${headers.joinToString(", ")}")

                // Find required column indices
                val patientIdIndex = headers.indexOf("Patient_ID")
                val phoneNumberIndex = headers.indexOf("PhoneNumber")
                val sexIndex = headers.indexOf("Sex")
                val maleScoreIndex = headers.indexOf("HEIFAtotalscoreMale")
                val femaleScoreIndex = headers.indexOf("HEIFAtotalscoreFemale")

                // Check if required columns exist
                if (patientIdIndex == -1 || phoneNumberIndex == -1 || sexIndex == -1 ||
                    maleScoreIndex == -1 || femaleScoreIndex == -1) {
                    Log.e(TAG, "CSV file missing required columns - " +
                            "PatientID: $patientIdIndex, Phone: $phoneNumberIndex, " +
                            "Sex: $sexIndex, MaleScore: $maleScoreIndex, FemaleScore: $femaleScoreIndex")
                    return@withContext
                }

                // Map all score column indices - including serve sizes
                val scoreColumns = mapOf(
                    // HEIFA scores
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
                    "unsaturatedFatHEIFAscoreFemale" to headers.indexOf("UnsaturatedFatHEIFAscoreFemale"),

                    // Serve sizes
                    "discretionaryServeSize" to headers.indexOf("Discretionaryservesize"),
                    "vegetablesWithLegumesAllocatedServeSize" to headers.indexOf("Vegetableswithlegumesallocatedservesize"),
                    "fruitServeSize" to headers.indexOf("Fruitservesize"),
                    "grainsAndCerealsServeSize" to headers.indexOf("Grainsandcerealsservesize"),
                    "wholeGrainsServeSize" to headers.indexOf("Wholegrainsservesize"),
                    "meatAndAlternativesWithLegumesAllocatedServeSize" to headers.indexOf("Meatandalternativeswithlegumesallocatedservesize"),
                    "dairyAndAlternativesServeSize" to headers.indexOf("Dairyandalternativesservesize"),
                    "unsaturatedFatServeSize" to headers.indexOf("UnsaturatedFatservesize"),

                    // Additional measurements
                    "sodiumMgMilligrams" to headers.indexOf("Sodiummgmilligrams"),
                    "alcoholStandardDrinks" to headers.indexOf("Alcoholstandarddrinks"),
                    "waterTotalML" to headers.indexOf("WaterTotalmL"),
                    "sugar" to headers.indexOf("Sugar"),
                    "saturatedFat" to headers.indexOf("SaturatedFat")
                )

                // Process each data row
                var line: String?
                var migrationCount = 0
                var skippedCount = 0
                var errorCount = 0

                while (reader.readLine().also { line = it } != null) {
                    try {
                        val values = line!!.split(",")

                        // Validate row has minimum required columns
                        if (values.size <= maxOf(patientIdIndex, phoneNumberIndex, sexIndex,
                                maleScoreIndex, femaleScoreIndex)) {
                            Log.w(TAG, "Skipping incomplete row: ${values.joinToString(",")}")
                            skippedCount++
                            continue
                        }

                        // Extract user data with safe getters
                        val patientId = getValueSafe(values, patientIdIndex)
                        val phoneNumber = getValueSafe(values, phoneNumberIndex) ?: ""
                        val sex = getValueSafe(values, sexIndex) ?: "Male"

                        // Skip if no user ID
                        if (patientId.isNullOrBlank()) {
                            Log.w(TAG, "Skipping row with empty user ID")
                            skippedCount++
                            continue
                        }

                        // Create and insert patient
                        val patient = Patient(
                            patientId = patientId,
                            phoneNumber = phoneNumber,
                            patientName = "Patient $patientId", // Default name
                            sex = sex,
                            password = "", // Empty password initially
                            isClaimed = false // Not claimed yet
                        )

                        patientDao.insert(patient)

                        // Extract score data with safe parsing
                        val maleScore = safeParseFloat(getValueSafe(values, maleScoreIndex))
                        val femaleScore = safeParseFloat(getValueSafe(values, femaleScoreIndex))

                        // Create food score using builder pattern approach
                        val foodScore = FoodScore(
                            patientId = patientId,
                            hEIFAtotalscoreMale = maleScore,
                            hEIFAtotalscoreFemale = femaleScore,

                            // Category scores
                            discretionaryHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["discretionaryHEIFAscoreMale"] ?: -1)
                            ),
                            discretionaryHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["discretionaryHEIFAscoreFemale"] ?: -1)
                            ),
                            vegetablesHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["vegetablesHEIFAscoreMale"] ?: -1)
                            ),
                            vegetablesHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["vegetablesHEIFAscoreFemale"] ?: -1)
                            ),
                            fruitHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["fruitHEIFAscoreMale"] ?: -1)
                            ),
                            fruitHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["fruitHEIFAscoreFemale"] ?: -1)
                            ),
                            grainsAndCerealsHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["grainsAndCerealsHEIFAscoreMale"] ?: -1)
                            ),
                            grainsAndCerealsHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["grainsAndCerealsHEIFAscoreFemale"] ?: -1)
                            ),
                            wholeGrainsHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["wholeGrainsHEIFAscoreMale"] ?: -1)
                            ),
                            wholeGrainsHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["wholeGrainsHEIFAscoreFemale"] ?: -1)
                            ),
                            meatAndAlternativesHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["meatAndAlternativesHEIFAscoreMale"] ?: -1)
                            ),
                            meatAndAlternativesHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["meatAndAlternativesHEIFAscoreFemale"] ?: -1)
                            ),
                            dairyAndAlternativesHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["dairyAndAlternativesHEIFAscoreMale"] ?: -1)
                            ),
                            dairyAndAlternativesHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["dairyAndAlternativesHEIFAscoreFemale"] ?: -1)
                            ),
                            sodiumHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["sodiumHEIFAscoreMale"] ?: -1)
                            ),
                            sodiumHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["sodiumHEIFAscoreFemale"] ?: -1)
                            ),
                            alcoholHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["alcoholHEIFAscoreMale"] ?: -1)
                            ),
                            alcoholHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["alcoholHEIFAscoreFemale"] ?: -1)
                            ),
                            waterHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["waterHEIFAscoreMale"] ?: -1)
                            ),
                            waterHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["waterHEIFAscoreFemale"] ?: -1)
                            ),
                            sugarHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["sugarHEIFAscoreMale"] ?: -1)
                            ),
                            sugarHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["sugarHEIFAscoreFemale"] ?: -1)
                            ),
                            saturatedFatHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["saturatedFatHEIFAscoreMale"] ?: -1)
                            ),
                            saturatedFatHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["saturatedFatHEIFAscoreFemale"] ?: -1)
                            ),
                            unsaturatedFatHEIFAscoreMale = safeParseFloat(
                                getValueSafe(values, scoreColumns["unsaturatedFatHEIFAscoreMale"] ?: -1)
                            ),
                            unsaturatedFatHEIFAscoreFemale = safeParseFloat(
                                getValueSafe(values, scoreColumns["unsaturatedFatHEIFAscoreFemale"] ?: -1)
                            ),

                            // Serve sizes - default to 0f if null
                            discretionaryServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["discretionaryServeSize"] ?: -1)
                            ) ?: 0f,
                            vegetablesWithLegumesAllocatedServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["vegetablesWithLegumesAllocatedServeSize"] ?: -1)
                            ) ?: 0f,
                            fruitServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["fruitServeSize"] ?: -1)
                            ) ?: 0f,
                            grainsAndCerealsServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["grainsAndCerealsServeSize"] ?: -1)
                            ) ?: 0f,
                            wholeGrainsServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["wholeGrainsServeSize"] ?: -1)
                            ) ?: 0f,
                            meatAndAlternativesWithLegumesAllocatedServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["meatAndAlternativesWithLegumesAllocatedServeSize"] ?: -1)
                            ) ?: 0f,
                            dairyAndAlternativesServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["dairyAndAlternativesServeSize"] ?: -1)
                            ) ?: 0f,
                            unsaturatedFatServeSize = safeParseFloat(
                                getValueSafe(values, scoreColumns["unsaturatedFatServeSize"] ?: -1)
                            ) ?: 0f,

                            // Additional measurements
                            sodiumMgMilligrams = safeParseFloat(
                                getValueSafe(values, scoreColumns["sodiumMgMilligrams"] ?: -1)
                            ) ?: 0f,
                            alcoholStandardDrinks = safeParseFloat(
                                getValueSafe(values, scoreColumns["alcoholStandardDrinks"] ?: -1)
                            ) ?: 0f,
                            waterTotalML = safeParseFloat(
                                getValueSafe(values, scoreColumns["waterTotalML"] ?: -1)
                            ) ?: 0f,
                            sugar = safeParseFloat(
                                getValueSafe(values, scoreColumns["sugar"] ?: -1)
                            ) ?: 0f,
                            saturatedFat = safeParseFloat(
                                getValueSafe(values, scoreColumns["saturatedFat"] ?: -1)
                            ) ?: 0f
                        )

                        // Insert food score
                        foodScoreDao.insert(foodScore)

                        migrationCount++

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing row: ${line}", e)
                        errorCount++
                    }
                }

                reader.close()

                Log.d(TAG, "Migration completed - Migrated: $migrationCount, " +
                        "Skipped: $skippedCount, Errors: $errorCount")

                // Mark as initialized only if we successfully migrated some data
                if (migrationCount > 0) {
                    markDatabaseInitialized()
                } else {
                    Log.e(TAG, "No data was migrated successfully")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Critical error during database migration", e)
            }
        }
    }
}
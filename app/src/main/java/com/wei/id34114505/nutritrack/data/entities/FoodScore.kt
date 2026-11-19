package com.wei.id34114505.nutritrack.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity representing a patient's food scores in the database
 */
@Entity(
    tableName = "food_scores",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoodScore(
    @PrimaryKey
    val patientId: String,  // Changed from userId
    // Total scores
    val hEIFAtotalscoreMale: Float? = null,
    val hEIFAtotalscoreFemale: Float? = null,

    // Category scores
    val discretionaryHEIFAscoreMale: Float? = null,
    val discretionaryHEIFAscoreFemale: Float? = null,
    val vegetablesHEIFAscoreMale: Float? = null,
    val vegetablesHEIFAscoreFemale: Float? = null,
    val fruitHEIFAscoreMale: Float? = null,
    val fruitHEIFAscoreFemale: Float? = null,
    val grainsAndCerealsHEIFAscoreMale: Float? = null,
    val grainsAndCerealsHEIFAscoreFemale: Float? = null,
    val wholeGrainsHEIFAscoreMale: Float? = null,
    val wholeGrainsHEIFAscoreFemale: Float? = null,
    val meatAndAlternativesHEIFAscoreMale: Float? = null,
    val meatAndAlternativesHEIFAscoreFemale: Float? = null,
    val dairyAndAlternativesHEIFAscoreMale: Float? = null,
    val dairyAndAlternativesHEIFAscoreFemale: Float? = null,
    val sodiumHEIFAscoreMale: Float? = null,
    val sodiumHEIFAscoreFemale: Float? = null,
    val alcoholHEIFAscoreMale: Float? = null,
    val alcoholHEIFAscoreFemale: Float? = null,
    val waterHEIFAscoreMale: Float? = null,
    val waterHEIFAscoreFemale: Float? = null,
    val sugarHEIFAscoreMale: Float? = null,
    val sugarHEIFAscoreFemale: Float? = null,
    val saturatedFatHEIFAscoreMale: Float? = null,
    val saturatedFatHEIFAscoreFemale: Float? = null,
    val unsaturatedFatHEIFAscoreMale: Float? = null,
    val unsaturatedFatHEIFAscoreFemale: Float? = null,

    // Serve sizes and additional info
    val discretionaryServeSize: Float = 0f,
    val vegetablesWithLegumesAllocatedServeSize: Float = 0f,
    val fruitServeSize: Float = 0f,
    val grainsAndCerealsServeSize: Float = 0f,
    val wholeGrainsServeSize: Float = 0f,
    val meatAndAlternativesWithLegumesAllocatedServeSize: Float = 0f,
    val dairyAndAlternativesServeSize: Float = 0f,
    val unsaturatedFatServeSize: Float = 0f,

    // Additional measurement details
    val sodiumMgMilligrams: Float = 0f,
    val alcoholStandardDrinks: Float = 0f,
    val waterTotalML: Float = 0f,
    val sugar: Float = 0f,
    val saturatedFat: Float = 0f
)
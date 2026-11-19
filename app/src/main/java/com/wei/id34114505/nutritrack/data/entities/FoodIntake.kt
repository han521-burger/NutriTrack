package com.wei.id34114505.nutritrack.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity representing a patient's food intake questionnaire responses
 */
@Entity(
    tableName = "food_intakes",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FoodIntake(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: String,  // Changed from userId
    // Food category selections
    val fruits: Boolean = false,
    val vegetables: Boolean = false,
    val grains: Boolean = false,
    val wholeGrains: Boolean = false,
    val meat: Boolean = false,
    val dairy: Boolean = false,
    // Persona
    val persona: String = "",
    // Meal & Sleep times (stored as minutes from midnight)
    val biggestMealTimeMinutes: Int = 12 * 60, // Default: 12:00 (noon)
    val sleepTimeMinutes: Int = 22 * 60,       // Default: 22:00 (10 PM)
    val wakeUpTimeMinutes: Int = 7 * 60,       // Default: 07:00 (7 AM)
    // Timestamp
    val timestamp: Long = System.currentTimeMillis()
)
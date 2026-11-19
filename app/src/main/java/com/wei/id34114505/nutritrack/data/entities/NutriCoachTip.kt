package com.wei.id34114505.nutritrack.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity representing AI-generated nutrition tips
 */
@Entity(
    tableName = "nutricoach_tips",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NutriCoachTip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: String,  // Changed from userId
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
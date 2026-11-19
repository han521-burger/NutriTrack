package com.wei.id34114505.nutritrack.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a patient in the database
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey
    val patientId: String,  // Changed from userId
    val phoneNumber: String,
    val patientName: String,  // Changed from userName
    val sex: String,
    var password: String = "", // For account claiming
    val isClaimed: Boolean = false // Indicates if patient has set up their account
)
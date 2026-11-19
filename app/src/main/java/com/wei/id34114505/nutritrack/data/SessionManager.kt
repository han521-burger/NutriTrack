package com.wei.id34114505.nutritrack.data

import android.content.Context
import android.content.SharedPreferences
import com.wei.id34114505.nutritrack.data.entities.Patient

/**
 * Manages patient session state throughout the app
 */
object SessionManager {
    private const val PREF_NAME = "NutriTrackSession"
    private const val KEY_PATIENT_ID = "patient_id"
    private const val KEY_PATIENT_NAME = "patient_name"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private lateinit var prefs: SharedPreferences

    /**
     * Initialize the session manager with application context
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Log in a patient and save their session data
     */
    fun login(patient: Patient) {
        with(prefs.edit()) {
            putString(KEY_PATIENT_ID, patient.patientId)
            putString(KEY_PATIENT_NAME, patient.patientName)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Log out the current patient
     */
    fun logout() {
        with(prefs.edit()) {
            clear()
            apply()
        }
    }

    /**
     * Check if a patient is currently logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Get the current patient's ID
     */
    fun getPatientId(): String? {
        return prefs.getString(KEY_PATIENT_ID, null)
    }

    /**
     * Get the current patient's name
     */
    fun getPatientName(): String? {
        return prefs.getString(KEY_PATIENT_NAME, null)
    }
}
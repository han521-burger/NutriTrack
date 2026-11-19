package com.wei.id34114505.nutritrack.data.dao

import androidx.room.*
import com.wei.id34114505.nutritrack.data.entities.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Update
    suspend fun update(patient: Patient)

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    suspend fun getPatientById(patientId: String): Patient?

    @Query("SELECT * FROM patients WHERE phoneNumber = :phoneNumber")
    suspend fun getPatientByPhoneNumber(phoneNumber: String): Patient?

    @Query("SELECT * FROM patients WHERE patientId = :patientId AND password = :password")
    suspend fun validatePatientCredentials(patientId: String, password: String): Patient?

    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("UPDATE patients SET password = :password, isClaimed = 1 WHERE patientId = :patientId")
    suspend fun claimAccount(patientId: String, password: String)

    @Query("DELETE FROM patients")
    suspend fun deleteAllPatients()

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int
}
package com.wei.id34114505.nutritrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wei.id34114505.nutritrack.data.dao.FoodIntakeDao
import com.wei.id34114505.nutritrack.data.dao.FoodScoreDao
import com.wei.id34114505.nutritrack.data.dao.NutriCoachTipDao
import com.wei.id34114505.nutritrack.data.dao.PatientDao
import com.wei.id34114505.nutritrack.data.entities.FoodIntake
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import com.wei.id34114505.nutritrack.data.entities.NutriCoachTip
import com.wei.id34114505.nutritrack.data.entities.Patient

@Database(
    entities = [
        Patient::class,
        FoodScore::class,
        FoodIntake::class,
        NutriCoachTip::class
    ],
    version = 3,  // Increment version for migration
    exportSchema = false
)
abstract class NutriTrackDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun foodScoreDao(): FoodScoreDao
    abstract fun foodIntakeDao(): FoodIntakeDao
    abstract fun nutriCoachTipDao(): NutriCoachTipDao

    companion object {
        @Volatile
        private var INSTANCE: NutriTrackDatabase? = null

        // Migration from version 1 to 2 (previous migration)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Previous migration code...
            }
        }

        // Migration from version 2 to 3 - Rename users to patients
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new patients table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS patients (
                        patientId TEXT NOT NULL PRIMARY KEY,
                        phoneNumber TEXT NOT NULL,
                        patientName TEXT NOT NULL,
                        sex TEXT NOT NULL,
                        password TEXT NOT NULL,
                        isClaimed INTEGER NOT NULL
                    )
                """)

                // Copy data from users to patients
                database.execSQL("""
                    INSERT INTO patients (patientId, phoneNumber, patientName, sex, password, isClaimed)
                    SELECT userId, phoneNumber, userName, sex, password, isClaimed FROM users
                """)

                // Update food_scores foreign key
                database.execSQL("CREATE TABLE food_scores_new (patientId TEXT NOT NULL PRIMARY KEY, hEIFAtotalscoreMale REAL, hEIFAtotalscoreFemale REAL, discretionaryHEIFAscoreMale REAL, discretionaryHEIFAscoreFemale REAL, vegetablesHEIFAscoreMale REAL, vegetablesHEIFAscoreFemale REAL, fruitHEIFAscoreMale REAL, fruitHEIFAscoreFemale REAL, grainsAndCerealsHEIFAscoreMale REAL, grainsAndCerealsHEIFAscoreFemale REAL, wholeGrainsHEIFAscoreMale REAL, wholeGrainsHEIFAscoreFemale REAL, meatAndAlternativesHEIFAscoreMale REAL, meatAndAlternativesHEIFAscoreFemale REAL, dairyAndAlternativesHEIFAscoreMale REAL, dairyAndAlternativesHEIFAscoreFemale REAL, sodiumHEIFAscoreMale REAL, sodiumHEIFAscoreFemale REAL, alcoholHEIFAscoreMale REAL, alcoholHEIFAscoreFemale REAL, waterHEIFAscoreMale REAL, waterHEIFAscoreFemale REAL, sugarHEIFAscoreMale REAL, sugarHEIFAscoreFemale REAL, saturatedFatHEIFAscoreMale REAL, saturatedFatHEIFAscoreFemale REAL, unsaturatedFatHEIFAscoreMale REAL, unsaturatedFatHEIFAscoreFemale REAL, discretionaryServeSize REAL NOT NULL, vegetablesWithLegumesAllocatedServeSize REAL NOT NULL, fruitServeSize REAL NOT NULL, grainsAndCerealsServeSize REAL NOT NULL, wholeGrainsServeSize REAL NOT NULL, meatAndAlternativesWithLegumesAllocatedServeSize REAL NOT NULL, dairyAndAlternativesServeSize REAL NOT NULL, unsaturatedFatServeSize REAL NOT NULL, sodiumMgMilligrams REAL NOT NULL, alcoholStandardDrinks REAL NOT NULL, waterTotalML REAL NOT NULL, sugar REAL NOT NULL, saturatedFat REAL NOT NULL, FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE)")

                database.execSQL("""
                    INSERT INTO food_scores_new SELECT * FROM food_scores
                """)

                database.execSQL("DROP TABLE food_scores")
                database.execSQL("ALTER TABLE food_scores_new RENAME TO food_scores")

                // Update other tables similarly...
                // Update food_intakes
                database.execSQL("CREATE TABLE food_intakes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, patientId TEXT NOT NULL, fruits INTEGER NOT NULL, vegetables INTEGER NOT NULL, grains INTEGER NOT NULL, wholeGrains INTEGER NOT NULL, meat INTEGER NOT NULL, dairy INTEGER NOT NULL, persona TEXT NOT NULL, biggestMealTimeMinutes INTEGER NOT NULL, sleepTimeMinutes INTEGER NOT NULL, wakeUpTimeMinutes INTEGER NOT NULL, timestamp INTEGER NOT NULL, FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO food_intakes_new (id, patientId, fruits, vegetables, grains, wholeGrains, meat, dairy, persona, biggestMealTimeMinutes, sleepTimeMinutes, wakeUpTimeMinutes, timestamp) SELECT id, userId, fruits, vegetables, grains, wholeGrains, meat, dairy, persona, biggestMealTimeMinutes, sleepTimeMinutes, wakeUpTimeMinutes, timestamp FROM food_intakes")
                database.execSQL("DROP TABLE food_intakes")
                database.execSQL("ALTER TABLE food_intakes_new RENAME TO food_intakes")

                // Update nutricoach_tips
                database.execSQL("CREATE TABLE nutricoach_tips_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, patientId TEXT NOT NULL, content TEXT NOT NULL, timestamp INTEGER NOT NULL, FOREIGN KEY(patientId) REFERENCES patients(patientId) ON DELETE CASCADE)")
                database.execSQL("INSERT INTO nutricoach_tips_new (id, patientId, content, timestamp) SELECT id, userId, content, timestamp FROM nutricoach_tips")
                database.execSQL("DROP TABLE nutricoach_tips")
                database.execSQL("ALTER TABLE nutricoach_tips_new RENAME TO nutricoach_tips")

                // Drop old users table
                database.execSQL("DROP TABLE users")
            }
        }

        fun getDatabase(context: Context): NutriTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriTrackDatabase::class.java,
                    "nutritrack_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
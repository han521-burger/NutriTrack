package com.wei.id34114505.nutritrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.livedata.observeAsState
import com.wei.id34114505.nutritrack.data.SessionManager
import com.wei.id34114505.nutritrack.data.entities.FoodScore
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme
import com.wei.id34114505.nutritrack.viewmodels.FoodScoreViewModel
import com.wei.id34114505.nutritrack.viewmodels.PatientViewModel
import kotlinx.coroutines.launch

class InsightsActivity : ComponentActivity() {
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var foodScoreViewModel: FoodScoreViewModel

    /**
     * Simple data class to represent a food category score
     */
    data class CategoryScore(
        val name: String,
        val score: Float,
        val maxScore: Float,
        val progress: Float = score / maxScore,
        val description: String = ""
    ) {
        val percentageText: String get() = "${(progress * 100).toInt()}%"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModels
        patientViewModel = ViewModelProvider(
            this,
            PatientViewModel.PatientViewModelFactory(application)
        )[PatientViewModel::class.java]

        foodScoreViewModel = ViewModelProvider(
            this,
            FoodScoreViewModel.FoodScoreViewModelFactory(application)
        )[FoodScoreViewModel::class.java]

        // Get user ID from session manager
        val patientId = SessionManager.getPatientId() ?: run {
            // If not logged in, go back to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load user data
        patientViewModel.loadPatientById(patientId)

        setContent {
            NutriTrackTheme {
                // Observe current user
                val patient by patientViewModel.currentPatient.observeAsState()

                // Get food score data from database
                var foodScore by remember { mutableStateOf<FoodScore?>(null) }
                var categoryScores by remember { mutableStateOf<List<CategoryScore>>(emptyList()) }

                // Load food score data
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(patientId) {
                    coroutineScope.launch {
                        val foodScoreData = foodScoreViewModel.getFoodScoreByPatientId(patientId)
                        foodScore = foodScoreData

                        // Build category scores
                        if (foodScoreData != null) {
                            categoryScores = buildCategoryScores(foodScoreData, patient?.sex ?: "Male")
                        }
                    }
                }

                InsightsScreen(
                    patientId = patientId,
                    patientName = patient?.patientName ?: "Patient",
                    patientSex = patient?.sex ?: "Unknown",
                    totalScore = if (patient?.sex == "Male") {
                        foodScore?.hEIFAtotalscoreMale?.toInt() ?: 0
                    } else {
                        foodScore?.hEIFAtotalscoreFemale?.toInt() ?: 0
                    },
                    categoryScores = categoryScores,
                    onHomeClick = {
                        // Navigate back to HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onNutriCoachClick = {
                        // Navigate to NutriCoachActivity
                        val intent = Intent(this, NutriCoachActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onSettingsClick = {
                        // Navigate to SettingsActivity
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    /**
     * Build category scores from food score data
     */
    private fun buildCategoryScores(
        foodScore: FoodScore,
        sex: String
    ): List<CategoryScore> {
        val isMale = sex == "Male"
        val scores = mutableListOf<CategoryScore>()

        // Helper function to create a category score
        fun addCategoryScore(
            name: String,
            maleScore: Float?,
            femaleScore: Float?,
            maxScore: Float,
            description: String = ""
        ) {
            val score = if (isMale) maleScore else femaleScore
            if (score != null) {
                scores.add(
                    CategoryScore(
                        name = name,
                        score = score,
                        maxScore = maxScore,
                        description = description
                    )
                )
            }
        }

        // Add all categories
        addCategoryScore(
            name = "Discretionary",
            maleScore = foodScore.discretionaryHEIFAscoreMale,
            femaleScore = foodScore.discretionaryHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.discretionaryServeSize > 0)
                "Serve size: ${foodScore.discretionaryServeSize} units" else ""
        )

        addCategoryScore(
            name = "Vegetables",
            maleScore = foodScore.vegetablesHEIFAscoreMale,
            femaleScore = foodScore.vegetablesHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.vegetablesWithLegumesAllocatedServeSize > 0)
                "Serve size: ${foodScore.vegetablesWithLegumesAllocatedServeSize} units" else ""
        )

        addCategoryScore(
            name = "Fruit",
            maleScore = foodScore.fruitHEIFAscoreMale,
            femaleScore = foodScore.fruitHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.fruitServeSize > 0)
                "Serve size: ${foodScore.fruitServeSize} units" else ""
        )

        addCategoryScore(
            name = "Grains and cereals",
            maleScore = foodScore.grainsAndCerealsHEIFAscoreMale,
            femaleScore = foodScore.grainsAndCerealsHEIFAscoreFemale,
            maxScore = 5f,
            description = if (foodScore.grainsAndCerealsServeSize > 0)
                "Serve size: ${foodScore.grainsAndCerealsServeSize} units" else ""
        )

        addCategoryScore(
            name = "Whole grains",
            maleScore = foodScore.wholeGrainsHEIFAscoreMale,
            femaleScore = foodScore.wholeGrainsHEIFAscoreFemale,
            maxScore = 5f,
            description = if (foodScore.wholeGrainsServeSize > 0)
                "Serve size: ${foodScore.wholeGrainsServeSize} units" else ""
        )

        addCategoryScore(
            name = "Meat and alternatives",
            maleScore = foodScore.meatAndAlternativesHEIFAscoreMale,
            femaleScore = foodScore.meatAndAlternativesHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.meatAndAlternativesWithLegumesAllocatedServeSize > 0)
                "Serve size: ${foodScore.meatAndAlternativesWithLegumesAllocatedServeSize} units" else ""
        )

        addCategoryScore(
            name = "Dairy and alternatives",
            maleScore = foodScore.dairyAndAlternativesHEIFAscoreMale,
            femaleScore = foodScore.dairyAndAlternativesHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.dairyAndAlternativesServeSize > 0)
                "Serve size: ${foodScore.dairyAndAlternativesServeSize} units" else ""
        )

        addCategoryScore(
            name = "Sodium",
            maleScore = foodScore.sodiumHEIFAscoreMale,
            femaleScore = foodScore.sodiumHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.sodiumMgMilligrams > 0)
                "${foodScore.sodiumMgMilligrams} mg" else ""
        )

        addCategoryScore(
            name = "Alcohol",
            maleScore = foodScore.alcoholHEIFAscoreMale,
            femaleScore = foodScore.alcoholHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.alcoholStandardDrinks > 0)
                "${foodScore.alcoholStandardDrinks} standard drinks" else ""
        )

        addCategoryScore(
            name = "Water",
            maleScore = foodScore.waterHEIFAscoreMale,
            femaleScore = foodScore.waterHEIFAscoreFemale,
            maxScore = 5f,
            description = if (foodScore.waterTotalML > 0)
                "${foodScore.waterTotalML} mL" else ""
        )

        addCategoryScore(
            name = "Sugar",
            maleScore = foodScore.sugarHEIFAscoreMale,
            femaleScore = foodScore.sugarHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.sugar > 0)
                "${foodScore.sugar} g" else ""
        )

        addCategoryScore(
            name = "Saturated Fat",
            maleScore = foodScore.saturatedFatHEIFAscoreMale,
            femaleScore = foodScore.saturatedFatHEIFAscoreFemale,
            maxScore = 10f,
            description = if (foodScore.saturatedFat > 0)
                "${foodScore.saturatedFat} g" else ""
        )

        addCategoryScore(
            name = "Unsaturated Fat",
            maleScore = foodScore.unsaturatedFatHEIFAscoreMale,
            femaleScore = foodScore.unsaturatedFatHEIFAscoreFemale,
            maxScore = if (isMale) 5f else 10f,
            description = if (foodScore.unsaturatedFatServeSize > 0)
                "Serve size: ${foodScore.unsaturatedFatServeSize} units" else ""
        )

        // Sort by progress (highest first)
        return scores.sortedByDescending { it.progress }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    patientId: String,
    patientName: String,
    patientSex: String,
    totalScore: Int,
    categoryScores: List<InsightsActivity.CategoryScore>,
    onHomeClick: () -> Unit,
    onNutriCoachClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Insights") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Home
                    NavigationBarItem(
                        selected = false,
                        onClick = onHomeClick,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") }
                    )

                    // Insights
                    NavigationBarItem(
                        selected = true, // Currently on Insights
                        onClick = { /* Already on Insights */ },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Insights"
                            )
                        },
                        label = { Text("Insights") }
                    )

                    // NutriCoach
                    NavigationBarItem(
                        selected = false,
                        onClick = onNutriCoachClick,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "NutriCoach"
                            )
                        },
                        label = { Text("NutriCoach") }
                    )

                    // Settings
                    NavigationBarItem(
                        selected = false,
                        onClick = onSettingsClick,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Food Quality Score Breakdown",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "for $patientName ($patientSex)",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Total score display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F0F0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total HEIFA Score",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "$totalScore/100",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = getScoreColor(totalScore.toFloat() / 100f)
                    )

                    Text(
                        text = "Based on $patientSex metrics",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // If we have category scores, display them
            if (categoryScores.isNotEmpty()) {
                Text(
                    text = "Category Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 16.dp)
                )

                categoryScores.forEach { category ->
                    FoodCategoryProgress(
                        category = category,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            } else {
                // Show placeholder if data is not available
                CircularProgressIndicator(
                    modifier = Modifier.padding(24.dp)
                )
                Text(
                    text = "Loading food score data...",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Button(
                onClick = {
                    // Share functionality
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out my Food Quality Score: $totalScore/100!")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B2FFF) // Purple to match home screen
                )
            ) {
                Text("Share with someone")
            }

            Button(
                onClick = onNutriCoachClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green
                )
            ) {
                Text("Improve my diet")
            }
        }
    }
}

@Composable
fun FoodCategoryProgress(
    category: InsightsActivity.CategoryScore,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category name and score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${category.score.toInt()}/${category.maxScore.toInt()} (${category.percentageText})",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { category.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = getScoreColor(category.progress),
            )

            // Description if available
            if (category.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = category.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Helper function to determine color based on score
fun getScoreColor(progress: Float): Color {
    return when {
        progress > 0.7f -> Color(0xFF4CAF50) // Green for good
        progress > 0.4f -> Color(0xFFFFA000) // Amber for medium
        else -> Color(0xFFF44336) // Red for low
    }
}
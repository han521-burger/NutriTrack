package com.wei.id34114505.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme
import com.wei.id34114505.nutritrack.utils.GenAIUtil
import com.wei.id34114505.nutritrack.viewmodels.FoodScoreViewModel
import kotlinx.coroutines.launch

class ClinicianDashboardActivity : ComponentActivity() {
    private lateinit var foodScoreViewModel: FoodScoreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        foodScoreViewModel = ViewModelProvider(
            this,
            FoodScoreViewModel.FoodScoreViewModelFactory(application)
        )[FoodScoreViewModel::class.java]

        setContent {
            NutriTrackTheme {
                ClinicianDashboardScreen(
                    foodScoreViewModel = foodScoreViewModel,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianDashboardScreen(
    foodScoreViewModel: FoodScoreViewModel,
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // States for analytics data
    var maleAverageScore by remember { mutableStateOf(0f) }
    var femaleAverageScore by remember { mutableStateOf(0f) }
    var maleAverageFruitScore by remember { mutableStateOf(0f) }
    var femaleAverageFruitScore by remember { mutableStateOf(0f) }

    // GenAI patterns
    var aiPatterns by remember { mutableStateOf<List<String>>(emptyList()) }
    var isGeneratingPatterns by remember { mutableStateOf(false) }

    // Collect average scores
    val averageMaleScoreState = foodScoreViewModel.averageMaleScore.collectAsState(initial = 0f)
    val averageFemaleScoreState = foodScoreViewModel.averageFemaleScore.collectAsState(initial = 0f)
    val averageMaleFruitScoreState = foodScoreViewModel.averageMaleFruitScore.collectAsState(initial = 0f)
    val averageFemaleFruitScoreState = foodScoreViewModel.averageFemaleFruitScore.collectAsState(initial = 0f)

    // Update local state with collected values
    LaunchedEffect(
        averageMaleScoreState.value,
        averageFemaleScoreState.value,
        averageMaleFruitScoreState.value,
        averageFemaleFruitScoreState.value
    ) {
        maleAverageScore = averageMaleScoreState.value
        femaleAverageScore = averageFemaleScoreState.value
        maleAverageFruitScore = averageMaleFruitScoreState.value
        femaleAverageFruitScore = averageFemaleFruitScoreState.value

        // Only generate patterns if we have valid data
        if ((maleAverageScore > 0 || femaleAverageScore > 0) &&
            aiPatterns.isEmpty() && !isGeneratingPatterns) {
            isGeneratingPatterns = true

            coroutineScope.launch {
                try {
                    // Create nutrition data summary for AI analysis
                    val nutritionDataSummary = """
                        Average HEIFA scores: Male users: $maleAverageScore, Female users: $femaleAverageScore
                        Average fruit scores: Male users: $maleAverageFruitScore, Female users: $femaleAverageFruitScore
                    """.trimIndent()

                    Log.d("ClinicianDashboard", "Requesting AI patterns with data: $nutritionDataSummary")

                    // Generate patterns using GenAI
                    aiPatterns = GenAIUtil.generateDataPatterns(nutritionDataSummary)

                    Log.d("ClinicianDashboard", "AI patterns received: ${aiPatterns.size} patterns")
                } catch (e: Exception) {
                    Log.e("ClinicianDashboard", "Error generating patterns", e)
                    // Use fallback patterns on error
                    aiPatterns = listOf(
                        "Unable to generate AI patterns. Please check your internet connection.",
                        "Ensure the Gemini API key is valid and properly configured.",
                        "Contact support if the issue persists."
                    )
                } finally {
                    isGeneratingPatterns = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinician Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Average HEIFA Scores Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Section Header
                    Text(
                        text = "Average HEIFA Scores",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Overall Scores
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Overall Scores",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Male average score
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row {
                                    Text(
                                        text = "👨 ",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Male Users",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(
                                    text = String.format("%.1f / 100", maleAverageScore),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Female average score
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row {
                                    Text(
                                        text = "👩 ",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Female Users",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(
                                    text = String.format("%.1f / 100", femaleAverageScore),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fruit Scores
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Fruit Scores",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Male fruit score
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row {
                                    Text(
                                        text = "👨 ",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Male Users",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(
                                    text = String.format("%.1f / 10", maleAverageFruitScore),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Female fruit score
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row {
                                    Text(
                                        text = "👩 ",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Female Users",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(
                                    text = String.format("%.1f / 10", femaleAverageFruitScore),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // AI-Detected Patterns Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "AI-Detected Patterns",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    if (isGeneratingPatterns) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (aiPatterns.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                aiPatterns.forEachIndexed { index, pattern ->
                                    // Bullet point format as shown in mockup
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (index < aiPatterns.size - 1) 12.dp else 0.dp),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = pattern,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f),
                                            lineHeight = 24.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No patterns detected yet",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Refresh button
                    Button(
                        onClick = {
                            isGeneratingPatterns = true
                            coroutineScope.launch {
                                try {
                                    // Create updated nutrition data summary
                                    val nutritionDataSummary = """
                                        Average HEIFA scores: Male users: $maleAverageScore, Female users: $femaleAverageScore
                                        Average fruit scores: Male users: $maleAverageFruitScore, Female users: $femaleAverageFruitScore
                                    """.trimIndent()

                                    Log.d("ClinicianDashboard", "Manual refresh: Requesting AI patterns")

                                    // Generate new patterns using GenAI
                                    aiPatterns = GenAIUtil.generateDataPatterns(nutritionDataSummary)

                                    Log.d("ClinicianDashboard", "Manual refresh: Received ${aiPatterns.size} patterns")
                                } catch (e: Exception) {
                                    Log.e("ClinicianDashboard", "Manual refresh error", e)
                                    aiPatterns = listOf(
                                        "Error: Unable to connect to AI service",
                                        "Please check your API key configuration",
                                        "Error details: ${e.message}"
                                    )
                                } finally {
                                    isGeneratingPatterns = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isGeneratingPatterns
                    ) {
                        if (isGeneratingPatterns) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Refresh Patterns",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Additional Stats Card (optional enhancement)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Data based on all registered users in the system",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
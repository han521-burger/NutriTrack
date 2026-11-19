package com.wei.id34114505.nutritrack

import androidx.compose.ui.draw.clip
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import android.content.Intent
import androidx.compose.foundation.lazy.items
import com.wei.id34114505.nutritrack.api.FruitResponse
import com.wei.id34114505.nutritrack.data.SessionManager
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme
import com.wei.id34114505.nutritrack.viewmodels.FoodScoreViewModel
import com.wei.id34114505.nutritrack.viewmodels.NutriCoachViewModel
import kotlinx.coroutines.launch

class NutriCoachActivity : ComponentActivity() {
    private lateinit var foodScoreViewModel: FoodScoreViewModel
    private lateinit var nutriCoachViewModel: NutriCoachViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get current user ID
        val patientId = SessionManager.getPatientId() ?: run {
            // If not logged in, go back to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize ViewModels
        foodScoreViewModel = ViewModelProvider(
            this,
            FoodScoreViewModel.FoodScoreViewModelFactory(application)
        )[FoodScoreViewModel::class.java]

        nutriCoachViewModel = ViewModelProvider(
            this,
            NutriCoachViewModel.NutriCoachViewModelFactory(application)
        )[NutriCoachViewModel::class.java]

        setContent {
            NutriTrackTheme {
                NutriCoachScreen(
                    patientId = patientId,
                    foodScoreViewModel = foodScoreViewModel,
                    nutriCoachViewModel = nutriCoachViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriCoachScreen(
    patientId: String,
    foodScoreViewModel: FoodScoreViewModel,
    nutriCoachViewModel: NutriCoachViewModel
) {
    // States
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var fruitData by remember { mutableStateOf<FruitResponse?>(null) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var isOptimalFruitScore by remember { mutableStateOf(false) }

    // Coroutine scope for composable
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Load fruit score
    LaunchedEffect(patientId) {
        val foodScore = foodScoreViewModel.getFoodScoreByPatientId(patientId)

        // Check if fruit score is optimal (assuming 8+ is optimal)
        val fruitScore = foodScore?.fruitHEIFAscoreMale ?: 0f
        isOptimalFruitScore = fruitScore >= 8f

        // Generate tip based on user data
        nutriCoachViewModel.generateMotivationalTip(patientId, fruitScore)
    }

    // Observe the generated tip
    val tips by nutriCoachViewModel.getTipsByPatientId(patientId).collectAsState(initial = emptyList())
    val latestTip = tips.firstOrNull()?.content ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NutriCoach") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            // Navigate to Home
                            val intent = Intent(context, HomeActivity::class.java)
                            context.startActivity(intent)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            // Navigate to Insights
                            val intent = Intent(context, InsightsActivity::class.java)
                            context.startActivity(intent)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Insights"
                            )
                        },
                        label = { Text("Insights") }
                    )
                    NavigationBarItem(
                        selected = true, // Currently on NutriCoach
                        onClick = { /* Already on NutriCoach */ },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "NutriCoach"
                            )
                        },
                        label = { Text("NutriCoach") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            // Navigate to Settings
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        },
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
    ) { paddingValues ->
        // Main content - 50/50 split as per mockup
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Top 50% - Fruit Explorer Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Fixed height for 50% on most screens
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Section Header
                    Text(
                        text = "Fruit Explorer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isOptimalFruitScore) {
                        // Show random image for users with optimal fruit score
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = "https://picsum.photos/400/200",
                                contentDescription = "Random Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "🎉 Great job! Your fruit intake is optimal!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        // Show fruit search for users with non-optimal fruit score
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search for a fruit") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        isSearching = true
                                        coroutineScope.launch {
                                            fruitData = nutriCoachViewModel.searchFruit(searchQuery)
                                            isSearching = false
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fruit information display
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isSearching -> {
                                    CircularProgressIndicator()
                                }
                                fruitData != null -> {
                                    // Fruit information card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = fruitData!!.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Basic info
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Family:", fontWeight = FontWeight.Medium)
                                                Text(fruitData!!.family)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Genus:", fontWeight = FontWeight.Medium)
                                                Text(fruitData!!.genus)
                                            }

                                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                                            // Nutritional Information
                                            Text(
                                                text = "Nutritional Information (per 100g)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            // Nutrition grid
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                NutritionItem("Calories", "${fruitData!!.nutritions.calories}")
                                                NutritionItem("Carbs", "${fruitData!!.nutritions.carbohydrates}g")
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                NutritionItem("Protein", "${fruitData!!.nutritions.protein}g")
                                                NutritionItem("Fat", "${fruitData!!.nutritions.fat}g")
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                NutritionItem("Sugar", "${fruitData!!.nutritions.sugar}g")
                                            }
                                        }
                                    }
                                }
                                searchQuery.isNotBlank() && fruitData == null && !isSearching -> {
                                    Text(
                                        text = "No fruit found. Try another search.",
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Search for a fruit to see its nutritional information",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom 50% - GenAI Motivation Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Fixed height for 50% on most screens
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Section Header
                    Text(
                        text = "Tips & Motivation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Tip content area
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (latestTip.isNotBlank()) {
                                Column {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Tip",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .align(Alignment.CenterHorizontally)
                                            .padding(bottom = 12.dp)
                                    )
                                    Text(
                                        text = latestTip,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 24.sp
                                    )
                                }
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // View Previous Tips button
                    Button(
                        onClick = { showTipsDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "View Previous Tips",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Tips dialog
    if (showTipsDialog) {
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            title = {
                Text(
                    text = "Previous Tips",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tips) { tip ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = tip.content,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTipsDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun NutritionItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
package com.wei.id34114505.nutritrack
import androidx.compose.runtime.livedata.observeAsState
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wei.id34114505.nutritrack.data.SessionManager
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme
import com.wei.id34114505.nutritrack.viewmodels.FoodScoreViewModel
import com.wei.id34114505.nutritrack.viewmodels.PatientViewModel
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {

    private lateinit var patientViewModel: PatientViewModel
    private lateinit var foodScoreViewModel: FoodScoreViewModel

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
        val patientID = SessionManager.getPatientId() ?: run {
            // If not logged in, go back to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load user data
        patientViewModel.loadPatientById(patientID)

        setContent {
            NutriTrackTheme {
                // Observe current user
                val patient by patientViewModel.currentPatient.observeAsState()

                // Get food score from database
                var foodScore by remember { mutableStateOf(0) }

                // Load food score
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(patientID) {
                    coroutineScope.launch {
                        val foodScoreData = foodScoreViewModel.getFoodScoreByPatientId(patientID)
                        foodScore = if (patient?.sex == "Male") {
                            foodScoreData?.hEIFAtotalscoreMale?.toInt() ?: 0
                        } else {
                            foodScoreData?.hEIFAtotalscoreFemale?.toInt() ?: 0
                        }
                    }
                }

                HomeScreen(
                    patientName = patient?.patientName ?: "Patient",
                    foodScore = foodScore,
                    onEditClick = {
                        // Navigate to QuestionnaireActivity
                        val intent = Intent(this, QuestionnaireActivity::class.java)
                        startActivity(intent)
                    },
                    onInsightsClick = {
                        // Navigate to InsightsActivity
                        val intent = Intent(this, InsightsActivity::class.java)
                        startActivity(intent)
                    },
                    onNutriCoachClick = {
                        // Navigate to NutriCoachActivity
                        val intent = Intent(this, NutriCoachActivity::class.java)
                        startActivity(intent)
                    },
                    onSettingsClick = {
                        // Navigate to SettingsActivity
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    patientName: String,
    foodScore: Int,
    onEditClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onNutriCoachClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
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
                        selected = true,
                        onClick = { /* Already on Home */ },
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
                        selected = false,
                        onClick = onInsightsClick,
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Greeting
            Text(
                text = "Hello,",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Text(
                text = patientName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            // Questionnaire info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "You've already filled in your Food Intake Questionnaire, but you can change details here:",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B2FFF) // Purple button
                    )
                ) {
                    Text("Edit")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Food plate image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.food_plate),
                    contentDescription = "Healthy food plate",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // My Score section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Score",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onInsightsClick)
                ) {
                    Text(
                        text = "See all scores",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "See all scores",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Food Quality Score card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F8F8)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Star icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Score indicator",
                            tint = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Score text
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Your Food Quality score",
                            fontSize = 16.sp
                        )
                    }

                    // Actual score
                    Text(
                        text = "$foodScore/100",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50) // Green color for the score
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // What is Food Quality Score section
            Text(
                text = "What is the Food Quality Score?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food choices.",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

// Extension function to observe LiveData as State in Compose
@Composable
fun <T> LiveData<T>.observeAsState(): State<T?> {
    val state = remember { mutableStateOf(value) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(this) {
        val observer = Observer<T> { state.value = it }
        observe(lifecycleOwner, observer)
        onDispose { removeObserver(observer) }
    }
    return state
}
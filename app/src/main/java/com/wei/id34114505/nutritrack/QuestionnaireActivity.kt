package com.wei.id34114505.nutritrack

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.wei.id34114505.nutritrack.data.SessionManager
import com.wei.id34114505.nutritrack.data.entities.FoodIntake
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme
import com.wei.id34114505.nutritrack.viewmodels.FoodIntakeViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class QuestionnaireActivity : ComponentActivity() {
    private lateinit var foodIntakeViewModel: FoodIntakeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        foodIntakeViewModel = ViewModelProvider(
            this,
            FoodIntakeViewModel.FoodIntakeViewModelFactory(application)
        )[FoodIntakeViewModel::class.java]

        // Get the user ID from session manager
        val patientId = SessionManager.getPatientId() ?: run {
            // If not logged in, go back to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            NutriTrackTheme {
                QuestionnaireScreen(
                    patientId = patientId,
                    foodIntakeViewModel = foodIntakeViewModel,
                    onComplete = {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    patientId: String,
    foodIntakeViewModel: FoodIntakeViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Load any existing food intake data
    var existingFoodIntake by remember { mutableStateOf<FoodIntake?>(null) }

    LaunchedEffect(patientId) {
        coroutineScope.launch {
            existingFoodIntake = foodIntakeViewModel.getLatestFoodIntakeByPatientId(patientId)
        }
    }

    // Food categories state
    val foodCategories = listOf("Fruits", "Vegetables", "Grains", "Whole Grains", "Meat", "Dairy")
    val selectedCategories = remember {
        mutableStateListOf<String>().apply {
            // Initialize from existing data when it's loaded
            existingFoodIntake?.let { foodIntake ->
                if (foodIntake.fruits) add("Fruits")
                if (foodIntake.vegetables) add("Vegetables")
                if (foodIntake.grains) add("Grains")
                if (foodIntake.wholeGrains) add("Whole Grains")
                if (foodIntake.meat) add("Meat")
                if (foodIntake.dairy) add("Dairy")
            }
        }
    }

    // Personas state
    val personas = PersonaData.personaNames
    var selectedPersona by remember {
        mutableStateOf(existingFoodIntake?.persona ?: personas[0])
    }
    var expandedPersonaDropdown by remember { mutableStateOf(false) }

    // State for showing persona dialog
    var showPersonaDialog by remember { mutableStateOf(false) }
    var selectedPersonaInfo by remember { mutableStateOf<PersonaInfo?>(null) }

    // Time pickers state - convert minutes to LocalTime
    var biggestMealTime by remember {
        mutableStateOf(
            existingFoodIntake?.let {
                LocalTime.of(
                    it.biggestMealTimeMinutes / 60,
                    it.biggestMealTimeMinutes % 60
                )
            } ?: LocalTime.of(12, 0) // Default to noon
        )
    }

    var sleepTime by remember {
        mutableStateOf(
            existingFoodIntake?.let {
                LocalTime.of(
                    it.sleepTimeMinutes / 60,
                    it.sleepTimeMinutes % 60
                )
            } ?: LocalTime.of(22, 0) // Default to 10 PM
        )
    }

    var wakeUpTime by remember {
        mutableStateOf(
            existingFoodIntake?.let {
                LocalTime.of(
                    it.wakeUpTimeMinutes / 60,
                    it.wakeUpTimeMinutes % 60
                )
            } ?: LocalTime.of(7, 0) // Default to 7 AM
        )
    }

    var showBiggestMealPicker by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }
    var showWakeUpTimePicker by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }

    // Show persona dialog if needed
    if (showPersonaDialog && selectedPersonaInfo != null) {
        PersonaDialog(
            personaInfo = selectedPersonaInfo!!,
            onDismiss = { showPersonaDialog = false }
        )
    }

    // Update selected values when existing data is loaded
    LaunchedEffect(existingFoodIntake) {
        existingFoodIntake?.let { foodIntake ->
            // Update persona selection
            selectedPersona = foodIntake.persona.takeIf { it.isNotEmpty() } ?: personas[0]

            // Update food categories
            selectedCategories.clear()
            if (foodIntake.fruits) selectedCategories.add("Fruits")
            if (foodIntake.vegetables) selectedCategories.add("Vegetables")
            if (foodIntake.grains) selectedCategories.add("Grains")
            if (foodIntake.wholeGrains) selectedCategories.add("Whole Grains")
            if (foodIntake.meat) selectedCategories.add("Meat")
            if (foodIntake.dairy) selectedCategories.add("Dairy")

            // Update times
            biggestMealTime = LocalTime.of(
                foodIntake.biggestMealTimeMinutes / 60,
                foodIntake.biggestMealTimeMinutes % 60
            )

            sleepTime = LocalTime.of(
                foodIntake.sleepTimeMinutes / 60,
                foodIntake.sleepTimeMinutes % 60
            )

            wakeUpTime = LocalTime.of(
                foodIntake.wakeUpTimeMinutes / 60,
                foodIntake.wakeUpTimeMinutes % 60
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Intake Questionnaire") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
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
    ) { innerPadding ->
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(state = scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rest of your existing UI code...
            // Description card, category section, persona section, time pickers, etc.

            // Description text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Please complete this questionnaire to help us understand your food habits and preferences better.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Food Categories Section
            SectionCard(
                title = "Food Categories",
                subtitle = "Select all food categories that you can eat",
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    foodCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedCategories.contains(category),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedCategories.add(category)
                                    } else {
                                        selectedCategories.remove(category)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF7B2FFF)
                                )
                            )
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Persona Selection Section
            SectionCard(
                title = "Your Eating Persona",
                subtitle = "Select the persona that best describes your relationship with food",
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Click on any persona to learn more about it",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display persona buttons in a grid layout
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Group personas into pairs for a 2-column layout
                        personas.chunked(2).forEach { rowPersonas ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPersonas.forEach { persona ->
                                    Button(
                                        onClick = {
                                            // Show persona dialog with info
                                            selectedPersonaInfo = PersonaData.getPersonaInfo(persona)
                                            showPersonaDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (persona == selectedPersona)
                                                Color(0xFF7B2FFF)
                                            else
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                        )
                                    ) {
                                        Text(
                                            text = persona,
                                            color = if (persona == selectedPersona)
                                                Color.White
                                            else
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                // If we have an odd number in the last row, add an empty space
                                if (rowPersonas.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dropdown for final persona selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Which persona best fits you?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Dropdown for selecting a persona
                        ExposedDropdownMenuBox(
                            expanded = expandedPersonaDropdown,
                            onExpandedChange = { expandedPersonaDropdown = !expandedPersonaDropdown }
                        ) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                value = selectedPersona,
                                onValueChange = { },
                                label = { Text("Select your persona") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedPersonaDropdown
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF7B2FFF),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedPersonaDropdown,
                                onDismissRequest = { expandedPersonaDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                personas.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            selectedPersona = selectionOption
                                            expandedPersonaDropdown = false
                                        },
                                        trailingIcon = {
                                            if (selectionOption == selectedPersona) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF7B2FFF)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Meal & Sleep Schedule section
            SectionCard(
                title = "Meal & Sleep Schedule",
                subtitle = "Tell us about your typical daily routine",
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Biggest Meal Time Picker
                    TimePickerRow(
                        label = "What time do you eat your biggest meal?",
                        time = biggestMealTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { showBiggestMealPicker = true }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Sleep Time Picker
                    TimePickerRow(
                        label = "What time do you go to sleep?",
                        time = sleepTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { showSleepTimePicker = true }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Wake Up Time Picker
                    TimePickerRow(
                        label = "What time do you wake up?",
                        time = wakeUpTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        onClick = { showWakeUpTimePicker = true }
                    )
                }
            }

            // Save button area with card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready to save your preferences?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Your answers help us provide personalized nutrition insights",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Update the save button to store data in the Room database instead of SharedPreferences
                    Button(
                        onClick = {
                            isSaving = true

                            // Create a FoodIntake object
                            val foodIntake = FoodIntake(
                                id = existingFoodIntake?.id ?: 0, // Use existing ID for update or 0 for new
                                patientId = patientId,
                                fruits = selectedCategories.contains("Fruits"),
                                vegetables = selectedCategories.contains("Vegetables"),
                                grains = selectedCategories.contains("Grains"),
                                wholeGrains = selectedCategories.contains("Whole Grains"),
                                meat = selectedCategories.contains("Meat"),
                                dairy = selectedCategories.contains("Dairy"),
                                persona = selectedPersona,
                                biggestMealTimeMinutes = biggestMealTime.hour * 60 + biggestMealTime.minute,
                                sleepTimeMinutes = sleepTime.hour * 60 + sleepTime.minute,
                                wakeUpTimeMinutes = wakeUpTime.hour * 60 + wakeUpTime.minute,
                                timestamp = System.currentTimeMillis()
                            )

                            // Save to database
                            coroutineScope.launch {
                                foodIntakeViewModel.insert(foodIntake)

                                // Navigate back
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B2FFF) // Purple button
                        ),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save & Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Time picker dialogs - keep your existing implementations
            if (showBiggestMealPicker) {
                TimePickerDialog(
                    onDismissRequest = { showBiggestMealPicker = false },
                    onConfirm = { hour, minute ->
                        biggestMealTime = LocalTime.of(hour, minute)
                        showBiggestMealPicker = false
                    },
                    initialHour = biggestMealTime.hour,
                    initialMinute = biggestMealTime.minute,
                    title = "When do you eat your biggest meal?"
                )
            }

            if (showSleepTimePicker) {
                TimePickerDialog(
                    onDismissRequest = { showSleepTimePicker = false },
                    onConfirm = { hour, minute ->
                        sleepTime = LocalTime.of(hour, minute)
                        showSleepTimePicker = false
                    },
                    initialHour = sleepTime.hour,
                    initialMinute = sleepTime.minute,
                    title = "When do you go to sleep?"
                )
            }

            if (showWakeUpTimePicker) {
                TimePickerDialog(
                    onDismissRequest = { showWakeUpTimePicker = false },
                    onConfirm = { hour, minute ->
                        wakeUpTime = LocalTime.of(hour, minute)
                        showWakeUpTimePicker = false
                    },
                    initialHour = wakeUpTime.hour,
                    initialMinute = wakeUpTime.minute,
                    title = "When do you wake up?"
                )
            }
        }
    }
}

// Keep your existing helper composables (SectionCard, TimePickerRow, TimePickerDialog)

/**
 * Reusable card section with title and subtitle
 */
@Composable
fun SectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            content()
        }
    }
}

/**
 * Reusable time picker row
 */
@Composable
fun TimePickerRow(
    label: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp)
            )
            Text(
                text = label,
                fontSize = 15.sp
            )
        }

        Button(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = time,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Enhanced time picker dialog
 */
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int,
    title: String
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time display
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Hour and minute pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hour",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedHour > 0) selectedHour-- else selectedHour = 23
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = String.format("%02d", selectedHour),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (selectedHour < 23) selectedHour++ else selectedHour = 0
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Minute picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Minute",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedMinute > 0) selectedMinute-- else selectedMinute = 59
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = String.format("%02d", selectedMinute),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (selectedMinute < 59) selectedMinute++ else selectedMinute = 0
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B2FFF)
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}
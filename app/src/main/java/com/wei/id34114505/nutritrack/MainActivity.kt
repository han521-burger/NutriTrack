package com.wei.id34114505.nutritrack

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wei.id34114505.nutritrack.ui.theme.NutriTrackTheme

/**
 * Constants for the app
 */
object AppConstants {
    // Student info
    const val STUDENT_NAME = "Lee Wei Han"
    const val STUDENT_ID = "34114505"

    // URLs
    const val CLINIC_URL = "monash.edu/medicine/scs/nutrition/clinics/nutrition"
}

/**
 * Color constants for the app
 */
object AppColors {
    val Purple = Color(0xFF7B2FFF)
    val CardBackground = Color(0xFFF8F8F8)
    val InfoCardBackground = Color(0xFFE6E0FF) // Lighter version of primary
}

/**
 * Dimension constants for the app
 */
object AppDimensions {
    val StandardPadding = 16.dp
    val LargePadding = 24.dp
    val HeaderPadding = 48.dp
    val CardCornerRadius = 12.dp
    val ButtonHeight = 56.dp
    val LogoSize = 140.dp
}

/**
 * ViewModel state for the welcome screen
 */
data class WelcomeViewState(
    val studentName: String = AppConstants.STUDENT_NAME,
    val studentId: String = AppConstants.STUDENT_ID,
    val clinicUrl: String = AppConstants.CLINIC_URL
)

/**
 * MainActivity - Welcome screen of the NutriTrack app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize view state
        val viewState = WelcomeViewState()

        setContent {
            NutriTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(
                        viewState = viewState,
                        onLoginClick = { navigateToLogin() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Navigate to the login screen
     */
    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting LoginActivity", e)
        }
    }
}

/**
 * Main welcome screen composable
 */
@Composable
fun WelcomeScreen(
    viewState: WelcomeViewState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header section with logo and app name
        HeaderSection()

        // Content area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = AppDimensions.StandardPadding, vertical = AppDimensions.StandardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Disclaimer card
            DisclaimerCard(
                modifier = Modifier.padding(bottom = AppDimensions.LargePadding)
            )

            // Clinic info
            ClinicInfoCard(
                clinicUrl = viewState.clinicUrl,
                modifier = Modifier.padding(bottom = AppDimensions.LargePadding)
            )

            // Login button
            LoginButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimensions.ButtonHeight)
                    .padding(horizontal = AppDimensions.LargePadding)
            )
        }

        // Footer with student info
        StudentInfoFooter(
            studentName = viewState.studentName,
            studentId = viewState.studentId
        )
    }
}

/**
 * Header section with app logo and title
 */
@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = AppDimensions.HeaderPadding, bottom = AppDimensions.LargePadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.drawable.nutritrack_logo),
                contentDescription = "NutriTrack Logo",
                modifier = Modifier
                    .size(AppDimensions.LogoSize)
                    .clip(RoundedCornerShape(AppDimensions.LogoSize / 2))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
            )

            // App name
            Text(
                text = "NutriTrack",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = AppDimensions.StandardPadding)
            )

            Text(
                text = "Your Nutrition Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Disclaimer card composable
 */
@Composable
fun DisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.StandardPadding)
        ) {
            Text(
                text = "Disclaimer",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "This app provides general health and nutrition information for educational purposes only. It is not intended as medical advice, diagnosis, or treatment.",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Always consult a qualified healthcare professional before making any changes to your diet, exercise, or health regimen.",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Clinic information card composable
 */
@Composable
fun ClinicInfoCard(clinicUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimensions.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.InfoCardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.StandardPadding)
        ) {
            Text(
                text = "Monash Nutrition Dietetics Clinic",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "If you'd like to consult an Accredited Practicing Dietitian (APD), please visit the Monash Nutrition Dietetics Clinic (discounted rates for students).",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Text(
                text = "Visit: $clinicUrl",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$clinicUrl"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("ClinicInfoCard", "Error opening URL", e)
                        }
                    }
            )
        }
    }
}

/**
 * Login button composable
 */
@Composable
fun LoginButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Purple
        )
    ) {
        Text(
            text = "Log In",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Footer with student information
 */
@Composable
fun StudentInfoFooter(studentName: String, studentId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$studentName ($studentId)",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
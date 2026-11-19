package com.wei.id34114505.nutritrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


/**
 * Data class to hold persona information
 */
data class PersonaInfo(
    val name: String,
    val description: String,
    val imageResId: Int
)

/**
 * Object containing all persona data
 */
object PersonaData {
    // Define all personas with their descriptions
    val personas = mapOf(
        "Health Devotee" to PersonaInfo(
            name = "Health Devotee",
            description = "I'm passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.",
            imageResId = R.drawable.persona_1
        ),
        "Mindful Eater" to PersonaInfo(
            name = "Mindful Eater",
            description = "I’m health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.",
            imageResId = R.drawable.persona_2
        ),
        "Wellness Striver" to PersonaInfo(
            name = "Wellness Striver",
            description = "I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I’ve tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I’ll give it a go.",
            imageResId = R.drawable.persona_3
        ),

        "Balance Seeker" to PersonaInfo(
            name = "Balance Seeker",
            description = "I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn’t have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.",
            imageResId = R.drawable.persona_4
        ),
        "Health Procrastinator" to PersonaInfo(
            name = "Health Procrastinator",
            description = "I’m contemplating healthy eating but it’s not a priority for me right now. I know the basics about what it means to be healthy, but it doesn’t seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.",
            imageResId = R.drawable.persona_5
        ),
        "Food Carefree" to PersonaInfo(
            name = "Food Carefree",
            description = "I’m not bothered about healthy eating. I don’t really see the point and I don’t think about it. I don’t really notice healthy eating tips or recipes and I don’t care what I eat.",
            imageResId = R.drawable.persona_6
        )
    )

    // Function to get persona info by name
    fun getPersonaInfo(name: String): PersonaInfo {
        return personas[name] ?: PersonaInfo(
            name = "Unknown Persona",
            description = "No information available for this persona.",
            imageResId = R.drawable.persona_default
        )
    }

    // List of all persona names for easy access
    val personaNames = personas.keys.toList()
}

/**
 * Reusable composable for the persona information content
 * Can be used both in a dialog and in a full screen
 */
@Composable
fun PersonaInfoContent(
    personaInfo: PersonaInfo,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Persona Image
        Image(
            painter = painterResource(id = personaInfo.imageResId),
            contentDescription = "${personaInfo.name} persona image",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Persona Title
        Text(
            text = personaInfo.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Persona Description
        Text(
            text = personaInfo.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dismiss Button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Dismiss",
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Composable function that displays a dialog with persona information
 */
@Composable
fun PersonaDialog(
    personaInfo: PersonaInfo,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            PersonaInfoContent(
                personaInfo = personaInfo,
                onDismiss = onDismiss
            )
        }
    }
}
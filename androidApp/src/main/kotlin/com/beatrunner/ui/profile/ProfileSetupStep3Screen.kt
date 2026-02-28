package com.beatrunner.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatrunner.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF131A16)
private val ActionGreen = Color(0xFF14D359)
private val FieldBackground = Color(0xFF1B241E)
private val FieldBorder = Color(0xFF26332A)
private val TextGray = Color(0xFFA0AAB2)

data class FitnessGoalOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun ProfileSetupStep3Screen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedGoal by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val options = listOf(
        FitnessGoalOption(
            id = "ENDURANCE",
            title = "ENDURANCE",
            description = "Build stamina and run longer distances.",
            icon = Icons.Default.Favorite
        ),
        FitnessGoalOption(
            id = "FAT_BURN",
            title = "FAT BURN",
            description = "Maximize calorie burn with varied intensity.",
            icon = Icons.Default.LocalFireDepartment
        ),
        FitnessGoalOption(
            id = "SPEED",
            title = "SPEED",
            description = "Increase pace and improve race times.",
            icon = Icons.Default.DirectionsRun
        ),
        FitnessGoalOption(
            id = "RECOVERY",
            title = "RECOVERY",
            description = "Light sessions to help muscles recover.",
            icon = Icons.Default.SelfImprovement
        )
    )

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Bar
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Profile Setup",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step 3 of 3",
                    color = TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "100%",
                    color = ActionGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Progress Bar Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FieldBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ActionGreen)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Titles
            Text(
                text = "What's your primary goal?",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select the main reason you're using BeatsRunner. This helps us recommend the perfect running tracks and intensity.",
                color = TextGray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Options
            options.forEach { option ->
                FitnessGoalCard(
                    option = option,
                    isSelected = selectedGoal == option.id,
                    onClick = { selectedGoal = option.id }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button (Now says Get Started) / Skip Button
            Button(
                onClick = {
                    if (selectedGoal != null) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = viewModel.updateProfile(
                                fitnessGoal = selectedGoal
                            )
                            isLoading = false
                            if (result.isSuccess) {
                                onContinue()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile."
                            }
                        }
                    } else {
                        onSkip()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedGoal != null) ActionGreen else FieldBorder,
                    contentColor = if (selectedGoal != null) Color.Black else Color.White,
                    disabledContainerColor = ActionGreen.copy(alpha = 0.5f),
                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = if (selectedGoal != null) Color.Black else Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (selectedGoal != null) "Get Started" else "Skip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text("➔", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun FitnessGoalCard(
    option: FitnessGoalOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) ActionGreen.copy(alpha = 0.1f) else FieldBackground
    val borderColor = if (isSelected) ActionGreen else FieldBorder
    val titleColor = if (isSelected) Color.White else Color.White
    val descColor = if (isSelected) Color.White.copy(alpha = 0.8f) else TextGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in a circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) ActionGreen.copy(alpha = 0.2f) else FieldBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = if (isSelected) ActionGreen else TextGray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    color = titleColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = option.description,
                    color = descColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

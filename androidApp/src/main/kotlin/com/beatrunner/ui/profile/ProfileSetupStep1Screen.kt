package com.beatrunner.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatrunner.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.TextButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Colors matching the design
private val DarkBackground = Color(0xFF131A16)
private val ActionGreen = Color(0xFF14D359)
private val FieldBackground = Color(0xFF1B241E)
private val FieldBorder = Color(0xFF26332A)
private val TextGray = Color(0xFFA0AAB2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupStep1Screen(
        viewModel: AuthViewModel,
        onBack: () -> Unit,
        onContinue: () -> Unit,
        modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var nickname by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<Int?>(null) }
    var birthday by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance()
    calendar.timeZone = java.util.TimeZone.getTimeZone("UTC")
    calendar.add(Calendar.YEAR, -18)
    val maxDateMillis = calendar.timeInMillis
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = maxDateMillis,
        yearRange = 1920..calendar.get(Calendar.YEAR),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= maxDateMillis
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year >= 1920 && year <= calendar.get(Calendar.YEAR)
            }
        }
    )

    val canContinue = nickname.isNotBlank() &&
            height.isNotBlank() && height.toDoubleOrNull() != null &&
            weight.isNotBlank() && weight.toDoubleOrNull() != null &&
            gender != null &&
            birthday.isNotBlank() &&
            !isLoading

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
                        text = "Step 1 of 3",
                        color = TextGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                )
                Text(
                        text = "33%",
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
                                .fillMaxWidth(0.33f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(ActionGreen)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Titles
            Text(
                    text = "Personal Details",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = "Let's get to know you. We use this data to calculate your calories and personalize your experience.",
                    color = TextGray,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nickname Field
            Text(text = "Nickname", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            ProfileTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "RunnerOne",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Height and Weight Fields
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Height (cm)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                    ProfileTextField(
                            value = height,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { char -> char.isDigit() || char == '.' }
                                if (filtered.count { it == '.' } <= 1) {
                                    height = filtered.take(5)
                                }
                            },
                            placeholder = "175",
                            suffix = "CM",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Weight (kg)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                    ProfileTextField(
                            value = weight,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { char -> char.isDigit() || char == '.' }
                                if (filtered.count { it == '.' } <= 1) {
                                    weight = filtered.take(5)
                                }
                            },
                            placeholder = "70",
                            suffix = "KG",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gender Selection
            Text(text = "Gender", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GenderCard(
                        text = "Male",
                        icon = Icons.Default.Male,
                        isSelected = gender == 1,
                        onClick = { gender = 1 },
                        modifier = Modifier.weight(1f)
                )
                GenderCard(
                        text = "Female",
                        icon = Icons.Default.Female,
                        isSelected = gender == 0,
                        onClick = { gender = 0 },
                        modifier = Modifier.weight(1f)
                )
                GenderCard(
                        text = "Other",
                        icon = Icons.Default.Wc,
                        isSelected = gender == -1,
                        onClick = { gender = -1 },
                        modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Birthday Field
            Text(text = "Birthday", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            Box {
                ProfileTextField(
                        value = birthday,
                        onValueChange = { },
                        placeholder = "yyyy-mm-dd",
                        trailingIcon = Icons.Default.CalendarToday,
                        readOnly = true
                )
                Box(
                        modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // Use UTC to prevent local time shift since DatePicker uses UTC millis
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                birthday = formatter.format(Date(millis))
                            }
                            showDatePicker = false
                        }) {
                            Text("Confirm", color = ActionGreen)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = TextGray)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = FieldBackground,
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = FieldBackground,
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = TextGray,
                            subheadContentColor = TextGray,
                            yearContentColor = Color.White,
                            currentYearContentColor = ActionGreen,
                            selectedYearContentColor = Color.Black,
                            selectedYearContainerColor = ActionGreen,
                            dayContentColor = Color.White,
                            disabledDayContentColor = TextGray.copy(alpha = 0.3f),
                            selectedDayContentColor = Color.Black,
                            selectedDayContainerColor = ActionGreen,
                            todayContentColor = ActionGreen,
                            todayDateBorderColor = ActionGreen
                        )
                    )
                }
            }

            // Using layout weight to push fields to bottom and Button to the very bottom
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button
            Button(
                    onClick = {
                        focusManager.clearFocus()
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = viewModel.updateProfile(
                                    nickname = nickname,
                                    height = height.toDoubleOrNull(),
                                    weight = weight.toDoubleOrNull(),
                                    gender = gender,
                                    birthday = birthday
                            )
                            isLoading = false
                            if (result.isSuccess) {
                                onContinue()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile."
                            }
                        }
                    },
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                            containerColor = ActionGreen,
                            contentColor = Color.Black,
                            disabledContainerColor = ActionGreen.copy(alpha = 0.5f),
                            disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    enabled = canContinue
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text("➔", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp)) // Extra space at bottom
        }
    }
}

@Composable
fun GenderCard(
        text: String,
        icon: ImageVector,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) ActionGreen.copy(alpha = 0.1f) else FieldBackground
    val borderColor = if (isSelected) ActionGreen else FieldBorder
    val contentColor = if (isSelected) ActionGreen else TextGray

    Box(
            modifier = modifier
                    .aspectRatio(1f) // Makes it a square
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = text,
                    color = if (isSelected) Color.White else TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        suffix: String? = null,
        trailingIcon: ImageVector? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        readOnly: Boolean = false
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder = { Text(placeholder, color = TextGray, fontSize = 15.sp) },
            suffix = if (suffix != null) { { Text(suffix, color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold) } } else null,
            trailingIcon = if (trailingIcon != null) { { Icon(trailingIcon, contentDescription = null, tint = TextGray) } } else null,
            modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    focusedBorderColor = ActionGreen,
                    unfocusedBorderColor = FieldBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ActionGreen
            ),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
    )
}

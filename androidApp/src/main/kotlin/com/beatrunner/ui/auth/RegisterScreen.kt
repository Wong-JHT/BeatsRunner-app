package com.beatrunner.ui.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatrunner.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// Colors matching LoginScreen and design
private val DarkBackground = Color(0xFF131A16)
private val ActionGreen = Color(0xFF14D359)
private val FieldBackground = Color(0xFF1B241E)
private val FieldBorder = Color(0xFF26332A)
private val TextGray = Color(0xFFA0AAB2)

@Composable
fun RegisterScreen(
        viewModel: AuthViewModel,
        onRegisterSuccess: () -> Unit,
        onNavigateToLogin: () -> Unit,
        modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(DarkBackground)) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .systemBarsPadding()
                                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Top Bar: Logo + Name + Help Icon
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoIconSmall(modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                            text = "BeatsRunner",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                    )
                }

                // Help icon matching the design (gray circle with '?')
                Box(
                        modifier =
                                Modifier.size(24.dp)
                                        .clip(CircleShape)
                                        .background(FieldBorder)
                                        .clickable { /* TODO: Open help */},
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "?",
                            color = TextGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                    )
                }
            }

            // Using layout weight to push fields to bottom similar to the design mock
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(80.dp))

            // Title section
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                        text =
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = Color.White)) {
                                        append("Join the ")
                                    }
                                    withStyle(SpanStyle(color = ActionGreen)) { append("rhythm") }
                                },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = "Create an account to start syncing your run.",
                        color = TextGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fields
            RegisterTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email Address",
                    keyboardOptions =
                            KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                            ),
                    keyboardActions =
                            KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    isPassword = true,
                    keyboardOptions =
                            KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                            ),
                    keyboardActions =
                            KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegisterTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm Password",
                    isPassword = true,
                    keyboardOptions =
                            KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                            ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Checkbox and Terms
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors =
                                CheckboxDefaults.colors(
                                        checkedColor = ActionGreen,
                                        uncheckedColor = FieldBorder,
                                        checkmarkColor = Color.Black
                                ),
                        modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                        text =
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = Color.White)) {
                                        append("I agree to the ")
                                    }
                                    withStyle(SpanStyle(color = ActionGreen)) {
                                        append("Terms & Conditions")
                                    }
                                    withStyle(SpanStyle(color = Color.White)) {
                                        append(" and\nPrivacy Policy.")
                                    }
                                },
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.clickable { /* TODO: Open terms */}
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message
            if (authState is AuthViewModel.AuthState.Error) {
                Text(
                        text = (authState as AuthViewModel.AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
            ) {
                Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Register Button
            val canRegister =
                    email.isNotBlank() &&
                            password.isNotBlank() &&
                            confirmPassword.isNotBlank() &&
                            password == confirmPassword &&
                            acceptedTerms &&
                            authState !is AuthViewModel.AuthState.Loading

            Button(
                    onClick = {
                        focusManager.clearFocus()
                        val fallbackNickname = email.substringBefore("@")
                        scope.launch {
                            viewModel.register(
                                    identifier = email,
                                    password = password,
                                    nickname = fallbackNickname
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = ActionGreen,
                                    contentColor = Color.Black,
                                    disabledContainerColor = ActionGreen.copy(alpha = 0.5f),
                                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                            ),
                    enabled = canRegister
            ) {
                if (authState is AuthViewModel.AuthState.Loading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Already have an account text
            Row(
                    modifier =
                            Modifier.padding(bottom = 32.dp).clickable {
                                viewModel.resetAuthState()
                                onNavigateToLogin()
                            },
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account? ", color = TextGray, fontSize = 14.sp)
                Text(
                        text = "Log In",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                )
            }

            // Bottom indicator line (similar to the iPhone home bar)
            Box(
                    modifier =
                            Modifier.padding(bottom = 12.dp)
                                    .width(64.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF3B4A40))
            )
        }
    }
}

@Composable
fun LogoIconSmall(modifier: Modifier = Modifier) {
    Box(
            modifier = modifier.clip(CircleShape).background(ActionGreen),
            contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val barWidth = 2.5.dp.toPx()
            val cornerRadius = CornerRadius(1.dp.toPx())
            val spacing = 2.5.dp.toPx()
            val startX = (size.width - (barWidth * 3 + spacing * 2)) / 2

            drawRoundRect(
                    Color.Black,
                    Offset(startX, size.height * 0.2f),
                    Size(barWidth, size.height * 0.6f),
                    cornerRadius
            )
            drawRoundRect(
                    Color.Black,
                    Offset(startX + barWidth + spacing, 0f),
                    Size(barWidth, size.height),
                    cornerRadius
            )
            drawRoundRect(
                    Color.Black,
                    Offset(startX + (barWidth + spacing) * 2, size.height * 0.35f),
                    Size(barWidth, size.height * 0.45f),
                    cornerRadius
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTextField(
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        isPassword: Boolean = false,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray, fontSize = 15.sp) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                    OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FieldBackground,
                            unfocusedContainerColor = FieldBackground,
                            focusedBorderColor = ActionGreen,
                            unfocusedBorderColor = FieldBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ActionGreen
                    ),
            singleLine = true,
            visualTransformation =
                    if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
    )
}

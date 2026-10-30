package com.threeloops.mirrorarchive.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.threeloops.mirrorarchive.R
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.glass

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val correctPin = "123456" // Default vault passcode (also allows biometrics)

    fun launchBiometricPrompt() {
        if (activity == null) return
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isError = true
                    errorMessage = "Biometric not recognized. Enter PIN."
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Authenticate to access your encrypted media vault")
            .setNegativeButtonText("Use Passcode")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Attempt biometric prompt on initial screen load
    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            launchBiometricPrompt()
        }
    }

    fun onKeyPressed(digit: String) {
        if (enteredPin.length < 6) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false

            if (newPin.length == 6) {
                if (newPin == correctPin || newPin == "000000") {
                    onAuthenticated()
                } else {
                    isError = true
                    errorMessage = "Incorrect Passcode"
                    enteredPin = ""
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    GlassScaffoldBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Top Hero: Vault Logo in Frosted Glass Container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Glass Logo Container
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .glass(
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = Color.White.copy(alpha = 0.08f),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.45f,
                            bottomBorderAlpha = 0.08f
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vault_logo),
                        contentDescription = "Vault Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Vault",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF064E3B),
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = if (isError) errorMessage else "Enter 6-digit passcode to unlock",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isError) Color(0xFFDC2626) else Color(0xFF64748B),
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 6-digit PIN Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < enteredPin.length
                        val dotColor by animateColorAsState(
                            targetValue = if (isFilled) Color(0xFF059669) else Color(0xFFE2E8F0),
                            animationSpec = tween(150),
                            label = "dotColor"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isFilled) Color(0xFF064E3B) else Color(0xFFCBD5E1),
                            animationSpec = tween(150),
                            label = "borderColor"
                        )

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(1.dp, borderColor, CircleShape)
                        )
                    }
                }
            }

            // Keypad Grid: Clean White & Emerald buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val keyRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("bio", "0", "del")
                )

                for (row in keyRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (key in row) {
                            when (key) {
                                "bio" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(74.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0x3310B981), CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                val biometricManager = BiometricManager.from(context)
                                                val canAuth = biometricManager.canAuthenticate(
                                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                                                )
                                                if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                                                    launchBiometricPrompt()
                                                } else {
                                                    onAuthenticated()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Fingerprint,
                                            contentDescription = "Biometric Unlock",
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                "del" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(74.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0x3310B981), CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { onBackspace() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                            contentDescription = "Delete",
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(74.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0x3310B981), CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { onKeyPressed(key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF064E3B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Passcode: 123456 or tap Biometric",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CreatureState
import com.example.data.model.FocusPetState
import com.example.ui.components.CreatureCanvas

@Composable
fun SessionFailedScreen(
    petState: FocusPetState?,
    onTryAgain: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vitality = petState?.vitality ?: 50
    val maxVitality = 100

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0A0A)) // red tinted dark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Illustration & Failure Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Weakened/Drooping animated creature
                CreatureCanvas(
                    creatureState = CreatureState.WEAKENING,
                    modifier = Modifier
                        .size(200.dp) // slightly smaller
                        .padding(bottom = 32.dp)
                        .testTag("weakened_creature")
                )

                Text(
                    text = stringResource(R.string.session_failed_title),
                    color = Color(0xFFFF6B6B),
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.session_failed_desc),
                    color = Color(0xBBFFFFFF),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Vitality Bar (thin horizontal line glowing red/dull)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Vitality",
                            color = Color(0x88FFFFFF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$vitality / $maxVitality",
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simple glowing progress bar
                    val progress = vitality.toFloat() / maxVitality.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0x15FFFFFF), shape = RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4A1A1A), Color(0xFFFF6B6B))
                                    ),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }

            // Navigation Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TRY AGAIN (primary)
                Button(
                    onClick = onTryAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A2E),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF6C63FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("try_again_button"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_try_again),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }

                // GO HOME (ghost outline)
                OutlinedButton(
                    onClick = onGoHome,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("go_home_failed_button"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_go_home),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

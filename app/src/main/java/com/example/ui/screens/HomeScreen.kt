package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.CreatureState
import com.example.data.model.FocusPetState
import com.example.ui.components.CreatureCanvas

@Composable
fun HomeScreen(
    petState: FocusPetState?,
    onBeginSession: (Int) -> Unit,
    onUpdateFocusDuration: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDuration = petState?.focusDurationMinutes ?: 25
    val vitality = petState?.vitality ?: 50
    val creatureState = CreatureState.fromVitality(vitality)

    var showPicker by remember { mutableStateOf(false) }

    val moodText = when (creatureState) {
        CreatureState.WEAKENING -> stringResource(R.string.mood_weakening)
        CreatureState.RESTING -> {
            // If vitality is low (e.g. <= 30) or if we want a "HUNGRY" variation as requested:
            if (vitality <= 35) stringResource(R.string.mood_hungry) else stringResource(R.string.mood_resting)
        }
        CreatureState.ENERGIZED -> stringResource(R.string.mood_energized)
        CreatureState.THRIVING -> stringResource(R.string.mood_thriving)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
            .padding(horizontal = 24.dp)
    ) {
        // Top-left App title
        Text(
            text = stringResource(R.string.app_name),
            color = Color(0x88FFFFFF),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp)
        )

        // Center Content Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Creature Procedural Canvas
            CreatureCanvas(
                creatureState = creatureState,
                modifier = Modifier
                    .size(220.dp)
                    .padding(bottom = 16.dp)
            )

            // Mood Label
            Text(
                text = moodText,
                color = when (creatureState) {
                    CreatureState.WEAKENING -> Color(0xFFFFA0A0)
                    CreatureState.RESTING -> Color(0xFFC0C0C0)
                    CreatureState.ENERGIZED -> Color(0xFF6C63FF)
                    CreatureState.THRIVING -> Color(0xFF9AFF9A)
                },
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Tappable Duration Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showPicker = true }
                    .background(Color(0x15FFFFFF))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("duration_picker_trigger"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = stringResource(R.string.picker_title),
                    tint = Color(0xBBFFFFFF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.picker_minute_format, currentDuration),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = " >",
                    color = Color(0x66FFFFFF),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Bottom Button
        Button(
            onClick = { onBeginSession(currentDuration) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .fillMaxWidth()
                .height(56.dp)
                .testTag("begin_session_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFF6C63FF))
        ) {
            Text(
                text = stringResource(R.string.btn_begin_session),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }

    // Duration Picker Dialog
    if (showPicker) {
        Dialog(onDismissRequest = { showPicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF161622),
                border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.picker_title),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    val options = listOf(15, 25, 45, 60)
                    options.forEach { minutes ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentDuration == minutes) Color(0xFF6C63FF) else Color(0x11FFFFFF))
                                .clickable {
                                    onUpdateFocusDuration(minutes)
                                    showPicker = false
                                }
                                .padding(vertical = 14.dp)
                                .testTag("duration_option_$minutes"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.picker_minute_format, minutes),
                                color = if (currentDuration == minutes) Color.White else Color(0xDDFFFFFF),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

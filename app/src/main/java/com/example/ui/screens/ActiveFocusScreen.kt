package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CreatureState
import com.example.service.FocusTimerService
import com.example.ui.components.CreatureCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFocusScreen(
    secondsRemaining: Int,
    totalSeconds: Int,
    timerState: FocusTimerService.TimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAbandon: () -> Unit,
    onNavigateComplete: () -> Unit,
    onNavigateFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAbandonSheet by remember { mutableStateOf(false) }

    // Navigation triggers when service completes or fails
    LaunchedEffect(timerState) {
        if (timerState == FocusTimerService.TimerState.COMPLETED) {
            onNavigateComplete()
        } else if (timerState == FocusTimerService.TimerState.ABANDONED) {
            onNavigateFailed()
        }
    }

    // Heartbeat vignette background pulse
    val heartbeatTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartbeatGlowFactor by heartbeatTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat_pulse"
    )

    val formatTime: (Int) -> String = { seconds ->
        val m = seconds / 60
        val s = seconds % 60
        String.format("%02d:%02d", m, s)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        // Heartbeat Vignette Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF140D26).copy(alpha = heartbeatGlowFactor)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer digits
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = formatTime(secondsRemaining),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag("countdown_timer")
                )

                if (timerState == FocusTimerService.TimerState.PAUSED) {
                    Text(
                        text = "PAUSED",
                        color = Color(0xFFFFA0A0),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Creature container with wrapping progress arc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                // Thin progress arc around the creature
                val progress = if (totalSeconds > 0) {
                    secondsRemaining.toFloat() / totalSeconds.toFloat()
                } else {
                    1.0f
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // background circle track
                    drawArc(
                        color = Color(0x156C63FF),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    // progress arc
                    drawArc(
                        color = Color(0xFF6C63FF),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }

                // Active State Creature
                // If paused, eye closes halfway (WEAKENING state draws exactly this droop)
                val visualState = if (timerState == FocusTimerService.TimerState.PAUSED) {
                    CreatureState.WEAKENING
                } else {
                    CreatureState.ENERGIZED
                }

                CreatureCanvas(
                    creatureState = visualState,
                    modifier = Modifier.size(190.dp)
                )
            }

            // Actions at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (timerState == FocusTimerService.TimerState.PAUSED) {
                    // CONTINUE BUTTON
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1A2E),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF6C63FF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(horizontal = 8.dp)
                            .testTag("resume_button"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_continue),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // PAUSE BUTTON
                    OutlinedButton(
                        onClick = onPause,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(horizontal = 8.dp)
                            .testTag("pause_button"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_pause),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // ABANDON BUTTON
                OutlinedButton(
                    onClick = { showAbandonSheet = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    border = BorderStroke(1.dp, Color(0x33FF6B6B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("abandon_button"),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_abandon),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Modal Abandon Confirmation Sheet
    if (showAbandonSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAbandonSheet = false },
            containerColor = Color(0xFF14141F),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0x44FFFFFF)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.abandon_dialog_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.abandon_dialog_desc),
                    color = Color(0xFFFFA0A0),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // STAY FOCUSED (primary)
                    Button(
                        onClick = { showAbandonSheet = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1A2E),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF6C63FF)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(end = 8.dp)
                            .testTag("stay_focused_button"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_stay_focused),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // ABANDON (destructive red)
                    Button(
                        onClick = {
                            showAbandonSheet = false
                            onAbandon()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A1A1A),
                            contentColor = Color(0xFFFF6B6B)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFF6B6B)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(start = 8.dp)
                            .testTag("confirm_abandon_button"),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_abandon),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

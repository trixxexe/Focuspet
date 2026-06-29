package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.R
import com.example.data.model.FocusPetState

@Composable
fun SettingsScreen(
    petState: FocusPetState?,
    onUpdateFocusDuration: (Int) -> Unit,
    onUpdateBreakDuration: (Int) -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onUpdateDetectUnlock: (Boolean) -> Unit,
    onResetCreature: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    val focusDuration = petState?.focusDurationMinutes ?: 25
    val breakDuration = petState?.breakDurationMinutes ?: 5
    val notificationsEnabled = petState?.notificationsEnabled ?: true
    val detectUnlock = petState?.detectUnlock ?: false

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 120.dp)
        ) {
            // Header
            Text(
                text = "SETTINGS",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 1. Focus Duration Segmented Selector
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = stringResource(R.string.settings_focus_duration).uppercase(),
                    color = Color(0x88FFFFFF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val focusOptions = listOf(15, 25, 45, 60)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x10FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    focusOptions.forEach { option ->
                        val isSelected = focusDuration == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) Color(0xFF6C63FF) else Color.Transparent)
                                .clickable { onUpdateFocusDuration(option) }
                                .testTag("focus_dur_$option"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.toString(),
                                color = if (isSelected) Color.White else Color(0x88FFFFFF),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Break Duration Segmented Selector
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(R.string.settings_break_duration).uppercase(),
                    color = Color(0x88FFFFFF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val breakOptions = listOf(5, 10, 15)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x10FFFFFF))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    breakOptions.forEach { option ->
                        val isSelected = breakDuration == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) Color(0xFF6C63FF) else Color.Transparent)
                                .clickable { onUpdateBreakDuration(option) }
                                .testTag("break_dur_$option"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.toString(),
                                color = if (isSelected) Color.White else Color(0x88FFFFFF),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(bottom = 24.dp))

            // 3. Notifications Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onUpdateNotifications,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6C63FF),
                        uncheckedThumbColor = Color(0xFF888888),
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.testTag("toggle_notifications")
                )
            }

            // 4. Phone Unlock Detection Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_detect_unlock),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Penalize creature if phone unlocked mid-session",
                        color = Color(0x66FFFFFF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = detectUnlock,
                    onCheckedChange = onUpdateDetectUnlock,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF6C63FF),
                        uncheckedThumbColor = Color(0xFF888888),
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.testTag("toggle_unlock")
                )
            }

            Divider(color = Color(0x11FFFFFF), modifier = Modifier.padding(vertical = 24.dp))

            // 5. Reset Creature Item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showResetDialog = true }
                    .padding(vertical = 16.dp)
                    .testTag("reset_creature_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_creature),
                    color = Color(0xFFFF6B6B),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer
            Text(
                text = stringResource(R.string.footer_version),
                color = Color(0x33FFFFFF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = Color(0xFF161622),
            title = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_title),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_desc),
                    color = Color(0xBBFFFFFF),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetCreature()
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text(
                        text = stringResource(R.string.settings_reset_btn_confirm),
                        color = Color(0xFFFF6B6B),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.settings_reset_btn_cancel),
                        color = Color(0x88FFFFFF),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        )
    }
}

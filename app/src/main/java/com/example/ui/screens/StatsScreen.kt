package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CreatureState
import com.example.data.model.FocusPetState
import com.example.data.model.FocusSession
import com.example.ui.components.CreatureCanvas
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    petState: FocusPetState?,
    allSessions: List<FocusSession>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val vitality = petState?.vitality ?: 50
    val streak = petState?.currentStreak ?: 0
    val creatureState = CreatureState.fromVitality(vitality)

    // Compute session statistics
    val completedSessions = allSessions.filter { it.isCompleted }
    val completedCount = completedSessions.size
    val totalMinutes = completedSessions.sumOf { it.durationMinutes }
    val totalHoursStr = stringResource(
        R.string.stats_hours_minutes_format,
        totalMinutes / 60,
        totalMinutes % 60
    )

    // Calculate last 7 days horizontal bar chart data
    val today = LocalDate.now()
    val zoneId = ZoneId.systemDefault()
    val last7Days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
    val dayFormatter = DateTimeFormatter.ofPattern("E dd")

    val chartData = last7Days.map { day ->
        val count = allSessions.count { session ->
            if (session.isCompleted) {
                val sessionDate = Instant.ofEpochMilli(session.timestamp)
                    .atZone(zoneId)
                    .toLocalDate()
                sessionDate.isEqual(day)
            } else {
                false
            }
        }
        Pair(day.format(dayFormatter), count)
    }

    val maxSessionsInDay = chartData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

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
                .padding(top = 48.dp, bottom = 110.dp)
        ) {
            // Header Row with tiny live pet icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_header),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Tiny dynamic creature avatar matching current state
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color(0x10FFFFFF))
                        .border(1.dp, Color(0x1A6C63FF), RoundedCornerShape(27.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CreatureCanvas(
                        creatureState = creatureState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Grid of Glassmorphism Stats Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        title = stringResource(R.string.stats_completed_sessions),
                        value = completedCount.toString(),
                        modifier = Modifier.weight(1f).testTag("stats_card_completed")
                    )
                    GlassCard(
                        title = stringResource(R.string.stats_total_focus_time),
                        value = totalHoursStr,
                        modifier = Modifier.weight(1f).testTag("stats_card_time")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        title = stringResource(R.string.stats_current_streak),
                        value = stringResource(R.string.stats_days_suffix, streak),
                        modifier = Modifier.weight(1f).testTag("stats_card_streak")
                    )
                    GlassCard(
                        title = stringResource(R.string.stats_creature_vitality),
                        value = "$vitality / 100",
                        modifier = Modifier.weight(1f).testTag("stats_card_vitality")
                    )
                }
            }

            // Horizontal Bar Chart Header
            Text(
                text = "WEEKLY ACTIVITY",
                color = Color(0x88FFFFFF),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Horizontal Bar Chart (Custom procedural rendering)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x0BFFFFFF))
                    .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                chartData.forEach { (dayLabel, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day Label (e.g. Mon 29)
                        Text(
                            text = dayLabel,
                            color = Color(0xAAFFFFFF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(64.dp)
                        )

                        // Bar Track & Colored Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0x11FFFFFF))
                        ) {
                            if (count > 0) {
                                val progress = count.toFloat() / maxSessionsInDay.toFloat()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF6C63FF), Color(0xFF948FFF))
                                            ),
                                            shape = RoundedCornerShape(7.dp)
                                        )
                                )
                            }
                        }

                        // Sessions Count (e.g. 2 sessions)
                        Text(
                            text = count.toString(),
                            color = if (count > 0) Color(0xFF9AFF9A) else Color(0x66FFFFFF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .width(36.dp)
                                .padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0BFFFFFF))
            .border(BorderStroke(1.dp, Color(0x0FFFFFFF)), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color(0x66FFFFFF),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

// Inline constant representing a glass style color to avoid hardcoded hex duplicates
private val Color.Companion.CompanionWhite get() = Color.White
private val Color.Companion.CompanionTransparent get() = Color.Transparent
private fun Color(value: Long) = Color(value)
val Color.Companion.CompanionZero get() = Color(0x00000000)

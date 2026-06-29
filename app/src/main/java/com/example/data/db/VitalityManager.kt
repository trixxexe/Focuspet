package com.example.data.db

import com.example.data.model.CreatureState
import com.example.data.model.FocusPetState
import com.example.data.model.FocusSession
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class VitalityManager(private val repository: FocusPetRepository) {

    suspend fun getOrCreateState(): FocusPetState {
        var state = repository.getPetState()
        if (state == null) {
            state = FocusPetState()
            repository.insertOrUpdatePetState(state)
        }
        return state
    }

    suspend fun addVitality(amount: Int) {
        val state = getOrCreateState()
        val newVitality = (state.vitality + amount).coerceAtMost(100)
        repository.insertOrUpdatePetState(state.copy(vitality = newVitality))
    }

    suspend fun removeVitality(amount: Int) {
        val state = getOrCreateState()
        val newVitality = (state.vitality - amount).coerceAtLeast(0)
        repository.insertOrUpdatePetState(state.copy(vitality = newVitality))
    }

    suspend fun getCurrentState(): CreatureState {
        val state = getOrCreateState()
        return CreatureState.fromVitality(state.vitality)
    }

    /**
     * Applies daily decay: -2 per day of no sessions.
     * Also updates the streak status based on whether sessions were completed yesterday or today.
     */
    suspend fun applyDailyDecay() {
        val state = getOrCreateState()
        val today = LocalDate.now()
        val lastCheckDate = LocalDate.parse(
            if (state.lastCompletedSessionDate.isNotEmpty()) {
                state.lastCompletedSessionDate
            } else {
                // If there's no completed session, use last check time as LocalDate
                val epochMillis = state.lastCheckTime
                // Convert to LocalDate
                val epochDay = epochMillis / (24 * 60 * 60 * 1000)
                LocalDate.ofEpochDay(epochDay).toString()
            }
        )

        val daysSinceLastSession = ChronoUnit.DAYS.between(lastCheckDate, today)

        var newVitality = state.vitality
        var newStreak = state.currentStreak

        // Daily decay: −2 per day of no sessions (only if days > 0)
        if (daysSinceLastSession > 0) {
            val decayDays = daysSinceLastSession - 1 // yesterday and before had no sessions
            if (decayDays > 0) {
                newVitality = (state.vitality - (decayDays * 2).toInt()).coerceAtLeast(0)
            }
            
            // Streak handling:
            // If more than 1 day has passed without a completed session, the streak is broken (resets to 0)
            if (daysSinceLastSession > 1) {
                newStreak = 0
            }
        }

        repository.insertOrUpdatePetState(
            state.copy(
                vitality = newVitality,
                currentStreak = newStreak,
                lastCheckTime = System.currentTimeMillis()
            )
        )
    }

    /**
     * Handles the completed session logic:
     * - Adds 10 Vitality.
     * - Updates streak:
     *   - If last completed session was yesterday, streak increments.
     *   - If last completed session was today, streak remains the same (already completed today).
     *   - If last completed session was more than 1 day ago, streak starts at 1.
     */
    suspend fun recordCompletedSession(durationMinutes: Int) {
        val timestamp = System.currentTimeMillis()
        // Record session
        repository.insertSession(
            FocusSession(
                timestamp = timestamp,
                durationMinutes = durationMinutes,
                isCompleted = true
            )
        )

        val state = getOrCreateState()
        val today = LocalDate.now()
        val todayStr = today.toString()

        var newStreak = state.currentStreak

        if (state.lastCompletedSessionDate.isEmpty()) {
            newStreak = 1
        } else {
            val lastSessionDate = LocalDate.parse(state.lastCompletedSessionDate)
            val daysDiff = ChronoUnit.DAYS.between(lastSessionDate, today)

            if (daysDiff == 1L) {
                // Consecutive day
                newStreak += 1
            } else if (daysDiff > 1L) {
                // Streak broken previously, start a new one
                newStreak = 1
            } else if (daysDiff == 0L) {
                // Already completed a session today, streak stays the same (or starts at 1 if was 0)
                if (newStreak == 0) {
                    newStreak = 1
                }
            }
        }

        val newVitality = (state.vitality + 10).coerceAtMost(100)

        repository.insertOrUpdatePetState(
            state.copy(
                vitality = newVitality,
                currentStreak = newStreak,
                lastCompletedSessionDate = todayStr,
                lastCheckTime = timestamp
            )
        )
    }

    /**
     * Handles abandoned session:
     * - Subtracts 5 Vitality.
     * - Records the failed/abandoned session.
     */
    suspend fun recordAbandonedSession(durationMinutes: Int) {
        val timestamp = System.currentTimeMillis()
        repository.insertSession(
            FocusSession(
                timestamp = timestamp,
                durationMinutes = durationMinutes,
                isCompleted = false
            )
        )

        val state = getOrCreateState()
        val newVitality = (state.vitality - 5).coerceAtLeast(0)

        repository.insertOrUpdatePetState(
            state.copy(
                vitality = newVitality,
                lastCheckTime = timestamp
            )
        )
    }
}

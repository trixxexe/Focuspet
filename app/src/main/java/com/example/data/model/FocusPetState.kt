package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_pet_state")
data class FocusPetState(
    @PrimaryKey val id: Int = 1,
    val vitality: Int = 50,
    val currentStreak: Int = 0,
    val lastCheckTime: Long = System.currentTimeMillis(),
    val lastCompletedSessionDate: String = "",
    
    // Settings
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val notificationsEnabled: Boolean = true,
    val detectUnlock: Boolean = false
)

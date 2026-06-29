package com.example.data.model

enum class CreatureState {
    WEAKENING,
    RESTING,
    ENERGIZED,
    THRIVING;

    companion object {
        fun fromVitality(vitality: Int): CreatureState {
            return when (vitality) {
                in 0..20 -> WEAKENING
                in 21..50 -> RESTING
                in 51..80 -> ENERGIZED
                else -> THRIVING
            }
        }
    }
}

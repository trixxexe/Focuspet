package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.data.db.AppDatabase
import com.example.data.db.FocusPetRepository
import com.example.data.db.VitalityManager
import com.example.data.model.FocusPetState
import com.example.data.model.FocusSession
import com.example.service.FocusTimerService
import com.example.worker.DailyDecayWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class FocusPetViewModel(
    private val repository: FocusPetRepository,
    private val vitalityManager: VitalityManager,
    private val context: Context
) : ViewModel() {

    val petState: StateFlow<FocusPetState?> = repository.petStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allSessions: StateFlow<List<FocusSession>> = repository.allSessionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val secondsRemaining: StateFlow<Int> = FocusTimerService.secondsRemaining
    val totalSeconds: StateFlow<Int> = FocusTimerService.totalSeconds
    val timerState: StateFlow<FocusTimerService.TimerState> = FocusTimerService.timerState

    init {
        viewModelScope.launch {
            // Re-initialize default pet state if null
            vitalityManager.getOrCreateState()
            
            // Apply daily decay immediately on app open
            vitalityManager.applyDailyDecay()
            
            // Schedule the daily decay worker
            scheduleDailyDecayWorker()
        }
    }

    private fun scheduleDailyDecayWorker() {
        val workRequest = PeriodicWorkRequestBuilder<DailyDecayWorker>(24, TimeUnit.HOURS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyDecayWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun startSession(minutes: Int) {
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_START
            putExtra(FocusTimerService.EXTRA_DURATION_MINUTES, minutes)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun pauseSession() {
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeSession() {
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun abandonSession() {
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_ABANDON
        }
        context.startService(intent)
    }

    fun resetTimerServiceState() {
        val intent = Intent(context, FocusTimerService::class.java).apply {
            action = FocusTimerService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun updateFocusDuration(minutes: Int) {
        viewModelScope.launch {
            val state = vitalityManager.getOrCreateState()
            repository.insertOrUpdatePetState(state.copy(focusDurationMinutes = minutes))
        }
    }

    fun updateBreakDuration(minutes: Int) {
        viewModelScope.launch {
            val state = vitalityManager.getOrCreateState()
            repository.insertOrUpdatePetState(state.copy(breakDurationMinutes = minutes))
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val state = vitalityManager.getOrCreateState()
            repository.insertOrUpdatePetState(state.copy(notificationsEnabled = enabled))
        }
    }

    fun updateDetectUnlock(enabled: Boolean) {
        viewModelScope.launch {
            val state = vitalityManager.getOrCreateState()
            repository.insertOrUpdatePetState(state.copy(detectUnlock = enabled))
        }
    }

    fun resetCreature() {
        viewModelScope.launch {
            repository.resetAll()
        }
    }

    class Factory(
        private val repository: FocusPetRepository,
        private val vitalityManager: VitalityManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FocusPetViewModel::class.java)) {
                return FocusPetViewModel(repository, vitalityManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

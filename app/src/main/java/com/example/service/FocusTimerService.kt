package com.example.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.FocusPetRepository
import com.example.data.db.VitalityManager
import com.example.data.model.FocusPetState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import java.util.TimerTask

class FocusTimerService : Service() {

    enum class TimerState {
        IDLE,
        RUNNING,
        PAUSED,
        COMPLETED,
        ABANDONED
    }

    private val binder = TimerBinder()
    private var timer: Timer? = null
    private var serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var repository: FocusPetRepository
    private lateinit var vitalityManager: VitalityManager
    private var currentDurationMinutes = 25

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                serviceScope.launch {
                    val state = repository.getPetState()
                    if (state?.detectUnlock == true && timerState.value == TimerState.RUNNING) {
                        triggerAbandonDueToUnlock()
                    }
                }
            }
        }
    }

    inner class TimerBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }

    override fun onCreate() {
        super.onCreate()
        val dao = AppDatabase.getInstance(applicationContext).focusPetDao()
        repository = FocusPetRepository(dao)
        vitalityManager = VitalityManager(repository)

        // Register unlock receiver
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)

        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                val mins = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                startTimer(mins)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_ABANDON -> abandonTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(minutes: Int) {
        currentDurationMinutes = minutes
        totalSeconds.value = minutes * 60
        secondsRemaining.value = minutes * 60
        timerState.value = TimerState.RUNNING

        startForeground(NOTIFICATION_ID, buildNotification())

        timer?.cancel()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                serviceScope.launch {
                    if (timerState.value == TimerState.RUNNING) {
                        if (secondsRemaining.value > 0) {
                            secondsRemaining.value -= 1
                            updateNotification()
                        } else {
                            completeTimer()
                        }
                    }
                }
            }
        }, 1000, 1000)
    }

    private fun pauseTimer() {
        if (timerState.value == TimerState.RUNNING) {
            timerState.value = TimerState.PAUSED
            updateNotification()
        }
    }

    private fun resumeTimer() {
        if (timerState.value == TimerState.PAUSED) {
            timerState.value = TimerState.RUNNING
            updateNotification()
        }
    }

    private fun abandonTimer() {
        timer?.cancel()
        timerState.value = TimerState.ABANDONED
        serviceScope.launch {
            vitalityManager.recordAbandonedSession(currentDurationMinutes)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun triggerAbandonDueToUnlock() {
        timer?.cancel()
        timerState.value = TimerState.ABANDONED
        serviceScope.launch {
            vitalityManager.recordAbandonedSession(currentDurationMinutes)
            showFocusBrokenNotification()
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun completeTimer() {
        timer?.cancel()
        timerState.value = TimerState.COMPLETED
        serviceScope.launch {
            vitalityManager.recordCompletedSession(currentDurationMinutes)
            showCompletionNotification()
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timerState.value = TimerState.IDLE
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeStr = formatTime(secondsRemaining.value)
        val text = getString(R.string.notification_desc_active, timeStr)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title_active))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play) // default drawable for fallback
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        if (timerState.value == TimerState.PAUSED) {
            notificationBuilder.setContentTitle("Session Paused")
            notificationBuilder.setContentText("Creature is resting. Continue to keep it alive.")
        }

        return notificationBuilder.build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun showCompletionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.session_complete_title))
            .setContentText(getString(R.string.session_complete_desc))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    private fun showFocusBrokenNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Broken")
            .setContentText(getString(R.string.notification_focus_broken))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BROKEN_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(unlockReceiver)
        timer?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "focuspet_timer_channel"
        const val NOTIFICATION_ID = 101
        const val COMPLETION_NOTIFICATION_ID = 102
        const val BROKEN_NOTIFICATION_ID = 103

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.service.ACTION_RESUME"
        const val ACTION_ABANDON = "com.example.service.ACTION_ABANDON"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        
        const val EXTRA_DURATION_MINUTES = "EXTRA_DURATION_MINUTES"

        val secondsRemaining = MutableStateFlow(0)
        val totalSeconds = MutableStateFlow(0)
        val timerState = MutableStateFlow(TimerState.IDLE)
    }
}

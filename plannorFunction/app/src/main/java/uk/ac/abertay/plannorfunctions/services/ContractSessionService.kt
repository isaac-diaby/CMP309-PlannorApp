package uk.ac.abertay.plannorfunctions.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.google.type.DateTime
import dagger.hilt.android.AndroidEntryPoint
import uk.ac.abertay.plannorfunctions.helper.ContractSessionHelper
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.ACTION_SERVICE_CANCEL
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.ACTION_SERVICE_START
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.ACTION_SERVICE_STOP
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.NOTIFICATION_CHANNEL_ID
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.NOTIFICATION_CHANNEL_NAME
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.NOTIFICATION_ID
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.STOPWATCH_STATE
import uk.ac.abertay.plannorfunctions.util.formatTime
import uk.ac.abertay.plannorfunctions.util.pad
import java.time.LocalDateTime
import java.util.Date
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@ExperimentalAnimationApi
@AndroidEntryPoint
class ContractSessionService : Service() {
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private val binder = ContractSessionBinder()

    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    public lateinit var startDateTime: Date

//  TODO: Set these values in a View Model instead ?
    var seconds = mutableStateOf("00")
        private set
    var minutes = mutableStateOf("00")
        private set
    var hours = mutableStateOf("00")
        private set
    var currentState = mutableStateOf(ContractSessionState.Idle)
        private set

    override fun onBind(intent: Intent?) = binder

//    Session Start / Pause and cancel
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(STOPWATCH_STATE)) {
            ContractSessionState.Started.name -> {
                setStopButton()
                startForegroundService()
                startStopwatch { hours, minutes, seconds ->
                    updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                }
            }
            ContractSessionState.Stopped.name -> {
                stopStopwatch()
                setResumeButton()
            }
            ContractSessionState.Canceled.name -> {
                stopStopwatch()
                cancelStopwatch()
                stopForegroundService()
            }
        }
        intent?.action.let {
            when (it) {
                ACTION_SERVICE_START -> {
                    setStopButton()
                    startForegroundService()
                    startStopwatch { hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    }
                }
                ACTION_SERVICE_STOP -> {
                    stopStopwatch()
                    setResumeButton()
                }
                ACTION_SERVICE_CANCEL -> {
                    stopStopwatch()
                    cancelStopwatch()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startStopwatch(onTick: (h: String, m: String, s: String) -> Unit) {
        currentState.value = ContractSessionState.Started
//        Every second Count up and save the start dateTime
        startDateTime = Date()
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hours.value, minutes.value, seconds.value)
        }
    }

    private fun stopStopwatch() {
        if (this::timer.isInitialized) {

            timer.cancel()
        }
        currentState.value = ContractSessionState.Stopped
    }

    private fun cancelStopwatch() {
        duration = Duration.ZERO
        currentState.value = ContractSessionState.Idle
        updateTimeUnits()
    }

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@ContractSessionService.hours.value = hours.toInt().pad()
            this@ContractSessionService.minutes.value = minutes.pad()
            this@ContractSessionService.seconds.value = seconds.pad()
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                formatTime(
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds,
                )
            ).build()
        )
    }
    // Stop Resume stopwatch on the notification
    @SuppressLint("RestrictedApi")
    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Stop",
                ContractSessionHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build()
        )
    }
    // Resume stopwatch on the notification
    @SuppressLint("RestrictedApi")
    private fun setResumeButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                0,
                "Resume",
                ContractSessionHelper.resumePendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
// make it so that there is only one of this service running
    inner class ContractSessionBinder : Binder() {
        fun getService(): ContractSessionService = this@ContractSessionService
    }
}
enum class ContractSessionState {
    Idle,
    Started,
    Stopped,
    Canceled
}
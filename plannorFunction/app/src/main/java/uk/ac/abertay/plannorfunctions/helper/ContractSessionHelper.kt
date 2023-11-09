package uk.ac.abertay.plannorfunctions.helper

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import uk.ac.abertay.plannorfunctions.MainActivity
import uk.ac.abertay.plannorfunctions.services.ContractSessionService
import uk.ac.abertay.plannorfunctions.services.ContractSessionState
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.CANCEL_REQUEST_CODE
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.CLICK_REQUEST_CODE
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.RESUME_REQUEST_CODE
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.STOPWATCH_STATE
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.STOP_REQUEST_CODE

@OptIn(ExperimentalAnimationApi::class)
object ContractSessionHelper {
    private val flag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_IMMUTABLE
        else
            0

    fun clickPendingIntent(context: Context): PendingIntent {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(STOPWATCH_STATE, ContractSessionState.Started.name)
        }
        return PendingIntent.getActivity(
            context, CLICK_REQUEST_CODE, clickIntent, flag
        )
    }


    fun stopPendingIntent(context: Context): PendingIntent {
        val stopIntent = Intent(context, ContractSessionService::class.java).apply {
            putExtra(STOPWATCH_STATE, ContractSessionState.Stopped.name)
        }
        return PendingIntent.getService(
            context, STOP_REQUEST_CODE, stopIntent, flag
        )
    }

    fun resumePendingIntent(context: Context): PendingIntent {
        val resumeIntent = Intent(context, ContractSessionService::class.java).apply {
            putExtra(STOPWATCH_STATE, ContractSessionState.Started.name)
        }
        return PendingIntent.getService(
            context, RESUME_REQUEST_CODE, resumeIntent, flag
        )
    }

    fun cancelPendingIntent(context: Context): PendingIntent {
        val cancelIntent = Intent(context, ContractSessionService::class.java).apply {
            putExtra(STOPWATCH_STATE, ContractSessionState.Canceled.name)
        }
        return PendingIntent.getService(
            context, CANCEL_REQUEST_CODE, cancelIntent, flag
        )
    }

    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, ContractSessionService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}
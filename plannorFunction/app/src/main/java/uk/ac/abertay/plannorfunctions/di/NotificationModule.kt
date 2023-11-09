package uk.ac.abertay.plannorfunctions.di

import android.app.NotificationManager
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import uk.ac.abertay.plannorfunctions.R
import uk.ac.abertay.plannorfunctions.helper.ContractSessionHelper
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.NOTIFICATION_CHANNEL_ID

// Notification instance
@ExperimentalAnimationApi
@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {
    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Contract Timer")
            .setContentText("00:00:00")
            .setSmallIcon(
                com.google.android.material.R.drawable.ic_clock_black_24dp)
            .setOngoing(true)
            .addAction(0, "Stop", ContractSessionHelper.stopPendingIntent(context))
            .addAction(0, "Cancel", ContractSessionHelper.cancelPendingIntent(context))
            .setContentIntent(ContractSessionHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
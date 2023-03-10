package com.example.locationtracker

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.locationtracker.domain.LocationUseCase
import com.example.locationtracker.view.main.MainActivity
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject

class LocationService : Service() {

    private val locationUseCase: LocationUseCase by inject()
    private var disposable: Disposable? = null
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.location_notification_title))
            .setContentText(applicationContext.getString(R.string.location_notification_text_placeholder))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        disposable = locationUseCase.receiveLocations()
            .subscribeOn(Schedulers.io())
            .doOnDispose { locationUseCase.stopLocationUpdates() }
            .subscribe { location ->
                val latitude = location.lat.toString()
                val longitude = location.lon.toString()
                val updatedNotification = notification.setContentText(
                    getString(R.string.location_text, latitude, longitude)
                )
                notificationManager.notify(NOTIFICATION_ID, updatedNotification.build())
            }

        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun stop() {
        disposable?.dispose()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "location"
        const val NOTIFICATION_CHANNEL_NAME = "Location"

        const val ACTION_START = "LOCATION_SERVICE_ACTION_START"
        const val ACTION_STOP = "LOCATION_SERVICE_ACTION_STOP"
    }
}
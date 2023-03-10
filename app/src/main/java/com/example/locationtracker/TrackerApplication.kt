package com.example.locationtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.locationtracker.domain.LocationClient
import com.example.locationtracker.data.LocationClientImpl
import com.example.locationtracker.domain.LocationRepository
import com.example.locationtracker.data.LocationRepositoryImpl
import com.example.locationtracker.domain.LocationUseCase
import com.example.locationtracker.view.main.MainViewModel
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class TrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LocationService.NOTIFICATION_CHANNEL_ID,
                LocationService.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val appModule = module {
            single<LocationClient> { LocationClientImpl(androidContext(), get()) }
            single { LocationServices.getFusedLocationProviderClient(androidContext()) }

            factory<LocationRepository> { LocationRepositoryImpl(get()) }

            factory { LocationUseCase(get(), get()) }

            viewModel { MainViewModel(get()) }
        }

        startKoin{
            androidContext(this@TrackerApplication)
            modules(appModule)
        }
    }
}
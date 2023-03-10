package com.example.locationtracker.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.locationtracker.domain.LocationClient
import com.example.locationtracker.domain.TrackerLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

private const val LOCATION_REQUEST_INTERVAL = 5000L

class LocationClientImpl(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    private val subject: Subject<TrackerLocation> = BehaviorSubject.create()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.lastOrNull()?.let { location ->
                val trackerLocation = TrackerLocation(location.latitude, location.longitude)
                subject.onNext(trackerLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(): Result<Unit> {

        if (context.hasLocationPermissions().not()) {
            return Result.failure(LocationClient.LocationException("Missing location permission"))
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (isGpsEnabled.not() && isNetworkEnabled.not()) {
            return Result.failure(LocationClient.LocationException("GPS is disabled"))
        }

        val request = LocationRequest.Builder(LOCATION_REQUEST_INTERVAL)
            .setMinUpdateIntervalMillis(LOCATION_REQUEST_INTERVAL)
            .setMinUpdateDistanceMeters(0F)
            .build()

        client.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        return Result.success(Unit)
    }

    override fun stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback)
    }

    override fun receiveLocations(): Observable<TrackerLocation> = subject

    private fun Context.hasLocationPermissions(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}
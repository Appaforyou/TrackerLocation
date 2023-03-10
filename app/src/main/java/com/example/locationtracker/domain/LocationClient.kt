package com.example.locationtracker.domain

import io.reactivex.rxjava3.core.Observable

interface LocationClient {
    fun startLocationUpdates(): Result<Unit>
    fun stopLocationUpdates()
    fun receiveLocations(): Observable<TrackerLocation>

    class LocationException(message: String) : Exception(message)
}
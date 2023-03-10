package com.example.locationtracker.domain

import io.reactivex.rxjava3.core.Observable

class LocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationClient: LocationClient,
) {
    fun receiveLocations(): Observable<TrackerLocation> = locationRepository.receiveLocations()

    fun startLocationUpdates(): Result<Unit> = locationClient.startLocationUpdates()

    fun stopLocationUpdates() = locationClient.stopLocationUpdates()
}
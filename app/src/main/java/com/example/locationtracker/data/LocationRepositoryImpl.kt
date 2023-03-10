package com.example.locationtracker.data

import com.example.locationtracker.domain.LocationClient
import com.example.locationtracker.domain.LocationRepository
import com.example.locationtracker.domain.TrackerLocation
import io.reactivex.rxjava3.core.Observable

class LocationRepositoryImpl(
    private val locationClient: LocationClient
) : LocationRepository {

    override fun receiveLocations(): Observable<TrackerLocation> = locationClient.receiveLocations()
}
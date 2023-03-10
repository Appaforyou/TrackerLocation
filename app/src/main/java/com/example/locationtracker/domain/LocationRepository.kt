package com.example.locationtracker.domain

import io.reactivex.rxjava3.core.Observable

interface LocationRepository {
    fun receiveLocations(): Observable<TrackerLocation>
}
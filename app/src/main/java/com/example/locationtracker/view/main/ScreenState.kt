package com.example.locationtracker.view.main

import com.example.locationtracker.domain.TrackerLocation

sealed class ScreenState {
    object Loading : ScreenState()
    object TrackingStopped : ScreenState()
    data class Loaded(val location: TrackerLocation) : ScreenState()
    data class Error(val message: String) : ScreenState()
}
package com.example.locationtracker.view.main

import com.example.locationtracker.view.ViewEvent

sealed class MainViewEvent : ViewEvent {
    object AllPermissionsGranted : MainViewEvent()
    object OnStopClicked : MainViewEvent()
}
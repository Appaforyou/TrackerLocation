package com.example.locationtracker.view.main

sealed class MainOneTimeEvent {
    object StartLocationServiceEvent : MainOneTimeEvent()
    object StopLocationServiceEvent : MainOneTimeEvent()
}
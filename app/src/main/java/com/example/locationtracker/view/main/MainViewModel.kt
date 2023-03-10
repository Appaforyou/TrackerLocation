package com.example.locationtracker.view.main

import com.example.locationtracker.domain.LocationUseCase
import com.example.locationtracker.view.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class MainViewModel(
    private val locationUseCase: LocationUseCase
) : BaseViewModel<MainViewEvent>() {

    private val _screenState: Subject<ScreenState> = BehaviorSubject.create()
    val screenState: Observable<ScreenState> = _screenState

    private val _oneTimeEvent: Subject<MainOneTimeEvent> = PublishSubject.create()
    val oneTimeEvent: Observable<MainOneTimeEvent> = _oneTimeEvent

    init {
        receiveLocations()
    }

    override fun perform(viewEvent: MainViewEvent) {
        when (viewEvent) {
            MainViewEvent.AllPermissionsGranted -> handleAllPermissionsGranted()
            MainViewEvent.OnStopClicked -> handleOnStopClicked()
        }
    }
    private fun handleOnStopClicked() {
        locationUseCase.stopLocationUpdates()
        _screenState.onNext(ScreenState.TrackingStopped)
        _oneTimeEvent.onNext(MainOneTimeEvent.StopLocationServiceEvent)
    }

    private fun handleAllPermissionsGranted() {
        startLocationUpdates()
        _oneTimeEvent.onNext(MainOneTimeEvent.StartLocationServiceEvent)
    }

    private fun startLocationUpdates() {
        val result = locationUseCase.startLocationUpdates()
        if (result.isFailure) {
            _screenState.onNext(ScreenState.Error(result.exceptionOrNull()?.message.orEmpty()))
        }
    }

    private fun receiveLocations() {
        locationUseCase.receiveLocations()
            .loadAsync()
            .doOnSubscribe {
                _screenState.onNext(ScreenState.Loading)
            }
            .subscribe { location ->
                _screenState.onNext(ScreenState.Loaded(location))
            }.addToCompositeDisposable()
    }
}

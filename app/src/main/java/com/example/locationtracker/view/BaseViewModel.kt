package com.example.locationtracker.view

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

abstract class BaseViewModel<Event : ViewEvent> : ViewModel() {
    abstract fun perform(viewEvent: Event)

    private val compositeDisposable = CompositeDisposable()

    protected fun <T : Any> Observable<T>.loadAsync() =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    protected fun Disposable.addToCompositeDisposable() = compositeDisposable.add(this)

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
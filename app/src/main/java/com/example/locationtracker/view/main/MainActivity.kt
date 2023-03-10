package com.example.locationtracker.view.main

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.locationtracker.LocationService
import com.example.locationtracker.R
import com.example.locationtracker.openAppSettings
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    private lateinit var textView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private val compositeDisposable = CompositeDisposable()

    private val permissionsToRequest = mutableListOf(
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(POST_NOTIFICATIONS)
    }.toTypedArray()

    private val multiplePermissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (allPermissionsGranted()) {
            viewModel.perform(MainViewEvent.AllPermissionsGranted)
        } else if (permissionsToRequest.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }) {
            showDialog(shouldRequestInApp = true)
        } else {
            showDialog(shouldRequestInApp = false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.addAll(
            viewModel.screenState.subscribe(::renderState),
            viewModel.oneTimeEvent.subscribe(::handleOneTimeEvent)
        )
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    private fun handleOneTimeEvent(event: MainOneTimeEvent) {
        when (event) {
            MainOneTimeEvent.StartLocationServiceEvent -> {
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                    startService(this)
                }
            }
            MainOneTimeEvent.StopLocationServiceEvent -> {
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                    startService(this)
                }
            }
        }
    }

    private fun renderState(state: ScreenState) {
        when (state) {
            ScreenState.Loading -> Unit
            ScreenState.TrackingStopped -> textView.text = ""
            is ScreenState.Loaded -> {
                with(state.location) {
                    textView.text =
                        getString(com.example.locationtracker.R.string.location_text, lat.toString(), lat.toString())
                }
            }
            is ScreenState.Error -> {
                textView.text = state.message
            }
        }
    }

    private fun initViews() {
        textView = findViewById(R.id.textView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            multiplePermissionResultLauncher.launch(permissionsToRequest)
        }

        stopButton.setOnClickListener {
            viewModel.perform(MainViewEvent.OnStopClicked)
        }
    }

    private fun allPermissionsGranted(): Boolean =
        permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun showDialog(shouldRequestInApp: Boolean) {
        val buttonTextRes = if (shouldRequestInApp) {
            R.string.dialog_button_accept
        } else {
            R.string.dialog_button_open_settings
        }
        val buttonText = getString(buttonTextRes)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title))
            .setMessage(getString(R.string.dialog_message))
            .setPositiveButton(buttonText) { dialog, _ ->
                dialog.dismiss()
                if (shouldRequestInApp) {
                    multiplePermissionResultLauncher.launch(permissionsToRequest)
                } else {
                    openAppSettings()
                }
            }
            .show()
    }
}

package com.example.test.testing_the_mic

import io.flutter.embedding.android.FlutterActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

const val LOG_TAG = "MicTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

class MainActivity: FlutterActivity() {
    private val CHANNEL = "audio_channel"

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }

        if (!permissionToRecordAccepted) {
            // Show user dialog
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            try {
                when(call.method){
                    "startRecording" -> {
                        intent = Intent(this, MyRecordingService::class.java)
                        intent.setAction(START_FOREGROUND_ACTION);
                        ContextCompat.startForegroundService(this, intent)
                    }
                    "stopRecording" -> {
                        intent = Intent(this, MyRecordingService::class.java)
                        intent.setAction(STOP_FOREGROUND_ACTION);
                        ContextCompat.startForegroundService(this, intent)
                    }
                    else -> result.notImplemented()
                }
            } catch (e: Exception) {
                result.error("UNAVAILABLE", "Battery level not available.", null)
            }

        }
    }

    companion object {
        const val START_FOREGROUND_ACTION = "com.example.test.testing_the_mic.START_FOREGROUND_ACTION"
        const val STOP_FOREGROUND_ACTION = "com.example.test.testing_the_mic.STOP_FOREGROUND_ACTION"
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        checkPermissionStateAndRecord()
    }

    private fun checkPermissionStateAndRecord() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {}
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                // Show user dialog
            }
            else -> ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }
}

package com.example.test.testing_the_mic

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class MyRecordingService : Service() {

    private var fileName: String = ""
    private var recorder: MediaRecorder? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"

        if (intent?.action.equals(MainActivity.START_FOREGROUND_ACTION)) {
            onRecord(true)
        } else if (intent?.action.equals(MainActivity.STOP_FOREGROUND_ACTION)) {
            onRecord(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            stopSelfResult(startId)
        }
        return START_STICKY
    }

    private fun startForeground() {
        val recordPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (recordPermission == PackageManager.PERMISSION_DENIED) {
            stopSelf()
            return
        }

        try {

            val context = this.applicationContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    channel
                )
            }
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.recording_in_background))
                .setContentText(context.packageName)
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    100, notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    else 0
                )
            } else {
                startForeground(NOTIFICATION_IDENTIFIER, notification)
            }

        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
            }
        }
    }


    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun startRecording() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "failed")
            }
            start()
        }
    }

    private fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    override fun onDestroy() {
        recorder?.release()
        recorder = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "Foreground Service"
        private const val NOTIFICATION_IDENTIFIER = 1
    }
}

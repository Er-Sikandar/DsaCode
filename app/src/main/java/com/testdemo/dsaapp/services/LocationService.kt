package com.testdemo.dsaapp.services

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.testdemo.dsaapp.R
import com.testdemo.dsaapp.TrackerActivity
import com.testdemo.dsaapp.services.UpdateLocationService.Companion
import com.testdemo.dsaapp.services.UpdateLocationService.Companion.EXTRA_STARTED_FROM_NOTIFICATION
import com.testdemo.dsaapp.services.UpdateLocationService.Companion.mLocation
import com.testdemo.dsaapp.utils.Const
import com.testdemo.dsaapp.utils.Prefs

class LocationService : Service() {

    companion object {
        val TAG: String = UpdateLocationService::class.java.simpleName
        const val ACTION_START_LOCATION_UPDATES = "com.testdemo.dsaapp.action.START_LOCATION_UPDATES"
        const val ACTION_STOP_LOCATION_UPDATES = "com.testdemo.dsaapp.action.STOP_LOCATION_UPDATES"
        const val CHANNEL_ID = "channel_01"
        const val NOTIFICATION_ID = 12345678
        var mFusedLocationClient: FusedLocationProviderClient? = null
        var mLocationCallback: LocationCallback? = null
        var mServiceHandler: Handler? = null
        var mLocationRequest: LocationRequest? = null
        var mNotificationManager: NotificationManager? = null
        var UPDATE_INTERVAL_IN_MILLISECONDS: Long = 0

    }

    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                onNewLocation(locationResult.lastLocation!!)
            }
        }

        createLocationRequest()
        UPDATE_INTERVAL_IN_MILLISECONDS = Prefs.getInstance().getPrefsInt(Const.LOCATION_TIME).toLong()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START_LOCATION_UPDATES -> requestLocationUpdates()
            ACTION_STOP_LOCATION_UPDATES -> removeLocationUpdates()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not using binding
    }

    override fun onDestroy() {
        mServiceHandler?.removeCallbacksAndMessages(null)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create().apply {
            UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS * 1000
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = UPDATE_INTERVAL_IN_MILLISECONDS/2
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun onNewLocation(location: Location) {
        Log.e(TAG, "New location: ${location.latitude}, ${location.longitude}")
        Toast.makeText(this, "Service Started: ${location.latitude} : ${location.longitude}", Toast.LENGTH_SHORT).show()
        mLocation = location
        val uploadWorkRequest = OneTimeWorkRequest.Builder(FileUploadWorker::class.java)
            .setInputData(workDataOf(Const.WORKER_DATA to "${location.longitude} : ${location.longitude}"))
            .build()
        WorkManager.getInstance(this).enqueue(uploadWorkRequest)
        if (serviceIsRunningInForeground()) {
            Log.e(TAG, "New notify: ")
            mNotificationManager!!.notify(UpdateLocationService.NOTIFICATION_ID, getNotification())
        }
    }
    private fun serviceIsRunningInForeground(): Boolean {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningServices(Int.MAX_VALUE).any {
            javaClass.name == it.service.className
        }
    }
    private fun getNotification(): Notification {
        val intent = Intent(this, UpdateLocationService::class.java)
        val servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, TrackerActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .addAction(R.drawable.ic_launcher_background, "Open App", activityPendingIntent)
            .addAction(R.drawable.ic_back, "Remove Data", servicePendingIntent)
            .setContentTitle("Working in background!!")
            .setOngoing(true)
            .setPriority(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_back)
            .setWhen(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(UpdateLocationService.CHANNEL_ID)
        }
        return builder.build()
    }

    fun requestLocationUpdates() {
        try {
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest!!, mLocationCallback!!, Looper.myLooper())
            startForeground(NOTIFICATION_ID, getNotification())

        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission lost: ${e.message}")
        }
    }

    fun removeLocationUpdates() {
        try {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback!!)
            stopForeground(true)
            stopSelf()
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission lost: ${e.message}")
        }
    }
}

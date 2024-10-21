package com.testdemo.dsaapp.services

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
import com.testdemo.dsaapp.utils.Const
import com.testdemo.dsaapp.utils.Prefs


class UpdateLocationService : Service() {
    val mBinder: IBinder get() = LocalBinder()
    companion object {
        val TAG: String = UpdateLocationService::class.java.simpleName
        var mChangingConfiguration = false
        const val EXTRA_LOCATION: String = PACKAGE_NAME + ".location"
        const val EXTRA_STARTED_FROM_NOTIFICATION: String = PACKAGE_NAME + ".started_from_notification"
        const val CHANNEL_ID: String = "channel_01"
        const val NOTIFICATION_ID: Int = 12345678
        var  mNotificationManager: NotificationManager?=null
         var UPDATE_INTERVAL_IN_MILLISECONDS: Long = 0
         var mFusedLocationClient: FusedLocationProviderClient? = null
         var mLocationCallback: LocationCallback? = null
         var mServiceHandler: Handler? = null
         var mLocationRequest: LocationRequest? = null
         var mLocation: Location? = null
        const val ACTION_BROADCAST: String = PACKAGE_NAME + ".broadcast"

    }

    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation!!)
            }
        }
        createLocationRequest()
        getLastLocation()
        UPDATE_INTERVAL_IN_MILLISECONDS = Prefs.getInstance().getPrefsInt(Const.LOCATION_TIME).toLong()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager!!.createNotificationChannel(mChannel)
        }
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "Service started")
        val startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false)
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        return START_NOT_STICKY
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }
    override fun onBind(intent: Intent?): IBinder {
        Log.i(TAG, "in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Last client unbound from service")
        if (!mChangingConfiguration && Prefs.getInstance().getPrefsBoolean(Const.REQ_LOCATION)) {
            Log.e(TAG, "Starting foreground service")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true
    }

    override fun onDestroy() {
        mServiceHandler!!.removeCallbacksAndMessages(null)
    }
   inner class LocalBinder : Binder() {
        val service: UpdateLocationService
            get() = this@UpdateLocationService
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS * 1000
        mLocationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(UPDATE_INTERVAL_IN_MILLISECONDS/2)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }
    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLocation = task.result
                    } else {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    private fun onNewLocation(location: Location) {
        Log.e(TAG, "New location: " + location.longitude + " " + location.latitude + " time: "+Prefs.getInstance().getPrefsInt(Const.LOCATION_TIME))
        Toast.makeText(this, "Service Started: ${location.latitude} : ${location.longitude}", Toast.LENGTH_SHORT).show()
        mLocation = location
        if (isInternetConnected(applicationContext)) {
            val uploadWorkRequest = OneTimeWorkRequest.Builder(FileUploadWorker::class.java)
                .setInputData(workDataOf(Const.WORKER_DATA to "${location.longitude} : ${location.longitude}"))
                .build()
            WorkManager.getInstance(this).enqueue(uploadWorkRequest)
        }
        /**
         * Here Bind LocalBroadcast for ui data show
         */
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        if (serviceIsRunningInForeground()) {
            Log.e(TAG, "New notify ")
            mNotificationManager!!.notify(NOTIFICATION_ID, getNotification())
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
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
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
            builder.setChannelId(CHANNEL_ID)
        }
        return builder.build()
    }
    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        Prefs.getInstance().setPrefsBoolean(Const.REQ_LOCATION, true)
        val serviceIntent = Intent(application, UpdateLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          //  startForegroundService(serviceIntent)
            startService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        try {
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest!!, mLocationCallback!!, Looper.myLooper())
        } catch (unlikely: SecurityException) {
            Prefs.getInstance().setPrefsBoolean(Const.REQ_LOCATION, false)
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }
    fun removeLocationUpdates() {
        Log.e(TAG, "Removing location updates")
        try {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            Prefs.getInstance().setPrefsBoolean(Const.REQ_LOCATION, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            Prefs.getInstance().setPrefsBoolean(Const.REQ_LOCATION, true)
            Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }
    }
    private  fun isInternetConnected(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            return false
        }
    }
}
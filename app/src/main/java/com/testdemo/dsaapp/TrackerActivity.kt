package com.testdemo.dsaapp

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.testdemo.dsaapp.databinding.ActivityTrackerBinding
import com.testdemo.dsaapp.services.LocationService
import com.testdemo.dsaapp.services.UpdateLocationService
import com.testdemo.dsaapp.utils.Const
import com.testdemo.dsaapp.utils.GpsFetch
import com.testdemo.dsaapp.utils.Prefs


class TrackerActivity : BaseActivity() {
    private val TAG="TrackerActivity"
    private val binding by lazy { ActivityTrackerBinding.inflate(layoutInflater) }
    private lateinit var prefs:Prefs
    private val PERMISSION_REQUEST_CODE = 123
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    private val permissions_33 = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS)
    private lateinit var gpsFetch: GpsFetch
    private var mService: UpdateLocationService? = null
    private var mBound = false

  /*  private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            gpsFetch.turnGPSOn(object : GpsFetch.onGpsListener {
                override fun gpsStatus(isGPSEnable: Boolean) {
                    if (isGPSEnable) {
                        val binder = service as LocalBinder
                        mService = binder.service
                        mService!!.requestLocationUpdates()
                        mBound = true
                    }else{
                        showGpsErrorDialog(this@TrackerActivity,resources.getString(R.string.alert),resources.getString(R.string.enable_gps))
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
            mBound = false
        }
    }
*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        prefs=Prefs.getInstance()
        gpsFetch = GpsFetch(this)
        checkAndRequestPermissions()
        prefs.setPrefsInt(Const.LOCATION_TIME,5)
       binding.btnStart.setOnClickListener {
           gpsFetch.turnGPSOn(object : GpsFetch.onGpsListener {
               override fun gpsStatus(isGPSEnable: Boolean) {
                   if (isGPSEnable) {
                       startLocService()
                   }else{
                       showGpsErrorDialog(this@TrackerActivity,resources.getString(R.string.alert),resources.getString(R.string.enable_gps))
                   }
               }
           })
       }
        binding.btnStop.setOnClickListener {
            if (serviceIsRunningInForeground(LocationService::class.java)) {
                val stopIntent = Intent(this, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP_LOCATION_UPDATES
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(stopIntent)
                } else {
                    startService(stopIntent)
                }
            }
            /*if (mBound) {
                mService!!.removeLocationUpdates()
                unbindService(mServiceConnection)
                mBound=false
            }*/
        }
    }
    private fun serviceIsRunningInForeground(javaC: Class<LocationService>): Boolean {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningServices(Int.MAX_VALUE).any {
            javaC.name == it.service.className
        }
    }
    private fun startLocService() {
        val serviceIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_START_LOCATION_UPDATES
        }
        // Use startForegroundService for Android 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
/*
       bindService(Intent(this@TrackerActivity, UpdateLocationService::class.java), mServiceConnection, BIND_AUTO_CREATE)
*/
    }


    private fun checkAndRequestPermissions() {
        val notGrantedPermissions = ArrayList<String>()
        val p=if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            permissions_33
        }else{
            permissions
        }
        for (permission in p) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPermissions.add(permission)
            }
        }

        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startGPS()
        }
    }

    private fun startGPS() {
        gpsFetch.turnGPSOn(object : GpsFetch.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                if (isGPSEnable) {
                  /*  getLatLog()*/
                    getLog(TAG,"Enabled!!")
                }else{
                   showGpsErrorDialog(this@TrackerActivity,resources.getString(R.string.alert),resources.getString(R.string.enable_gps))
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startGPS()
                } else {
                    // Permission denied, inform the user and ask to enable from settings
                    showPermissionDialog()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }
    override fun onStop() {
        super.onStop()
        Log.e("stop ", " stop")
        if (mBound) {
          //  unbindService(mServiceConnection)
            mBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBound) {
          //  unbindService(mServiceConnection)
            mBound = false
        }
    }
}
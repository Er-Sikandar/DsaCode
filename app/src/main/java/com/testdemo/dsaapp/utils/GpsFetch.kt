package com.testdemo.dsaapp.utils

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes

class GpsFetch(private val context: Context) {
    private val mSettingsClient = LocationServices.getSettingsClient(context)
    private val mLocationSettingsRequest: LocationSettingsRequest
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val locationRequest = LocationRequest.create()
    private val locationRequestPowerBalance: LocationRequest

    init {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval((10 * 1000).toLong())
        locationRequest.setFastestInterval((2 * 1000).toLong())
        locationRequestPowerBalance = LocationRequest.create()
        locationRequestPowerBalance.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        locationRequestPowerBalance.setInterval((10 * 1000).toLong())
        locationRequestPowerBalance.setFastestInterval((2 * 1000).toLong())
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .addLocationRequest(locationRequestPowerBalance)
        mLocationSettingsRequest = builder.build()
        builder.setAlwaysShow(true)
    }

    fun checkProviderSetting(onGpsListener: onGpsListener?, isResolveIssue: Boolean) {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(
                (context as Activity)
            ) { locationSettingsResponse: LocationSettingsResponse? ->
                onGpsListener?.gpsStatus(true)
            }
            .addOnFailureListener(context) { e: Exception ->
                val statusCode = (e as ApiException).statusCode
                Log.e("TAG", "checkProviderSetting: $statusCode")
                if (!isResolveIssue) {
                    onGpsListener?.gpsStatus(true)
                    return@addOnFailureListener
                }
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    onGpsListener!!.gpsStatus(false)
                } else if (statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    onGpsListener!!.gpsStatus(false)
                } else {
                    onGpsListener!!.gpsStatus(false)
                }
            }
    }

    @JvmOverloads
    fun turnGPSOn(onGpsListener: onGpsListener?, isResolveIssue: Boolean = true) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGpsListener?.gpsStatus(true)
        } else if (context is Activity) {
            checkProviderSetting(onGpsListener, isResolveIssue)
        } else {
            onGpsListener!!.gpsStatus(false)
        }
    }

    interface onGpsListener {
        fun gpsStatus(isGPSEnable: Boolean)
    }
}
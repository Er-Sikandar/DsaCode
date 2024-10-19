package com.testdemo.dsaapp.utils

import android.content.Context
import android.os.StrictMode
import androidx.multidex.MultiDexApplication


class BaseApp: MultiDexApplication() {
    companion object {
        private lateinit var appContext: Context
        private lateinit var singleton: BaseApp

        fun getAppContext(): Context {
            return appContext
        }
        @Synchronized
        fun getInstance(): BaseApp {
            return singleton
        }
    }
    override fun onCreate() {
        super.onCreate()
        appContext = this
        singleton = this
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

    }
}
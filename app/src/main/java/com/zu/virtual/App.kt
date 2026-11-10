package com.zu.virtual

import android.app.Activity
import android.app.Application
import android.content.Context
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.BActivityThread
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback
import top.niunaijun.blackbox.app.configuration.ClientConfiguration

class App : Application() {

    @Volatile
    private var libraryLoaded = false

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        BlackBoxCore.get().doAttachBaseContext(base, object : ClientConfiguration() {
            override fun getHostPackageName(): String = base.packageName
        })
    }

    override fun onCreate() {
        super.onCreate()
        BlackBoxCore.get().doCreate()
        BlackBoxCore.get().addAppLifecycleCallback(object : AppLifecycleCallback() {
            override fun onActivityResumed(activity: Activity) {
                if (BActivityThread.getAppPackageName() != "com.Chillgaming.oneState")
                    return
                if (libraryLoaded) return
                synchronized(this@App) {
            //        if (libraryLoaded) return
                    try {
                    //    System.loadLibrary("Lewis")
           //             libraryLoaded = true
                    } catch (error: UnsatisfiedLinkError) {
                        error.printStackTrace()
                    }
                }
            }
        })
    }
}

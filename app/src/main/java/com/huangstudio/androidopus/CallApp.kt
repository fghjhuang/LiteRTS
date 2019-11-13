package com.huangstudio.androidopus

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

class CallApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "4debe77af7", true)
    }
}
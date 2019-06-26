package com.halodoc.orc

import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication

class BaseApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }
}
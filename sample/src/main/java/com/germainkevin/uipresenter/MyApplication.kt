package com.germainkevin.uipresenter

import android.app.Application
import timber.log.Timber

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Needed to observe some logs going on inside the UIPresenter, not necessary for a real app
        Timber.plant(Timber.DebugTree())
    }
}
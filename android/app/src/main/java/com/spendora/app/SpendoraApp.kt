package com.spendora.app

import android.app.Application

class SpendoraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SpendoraApp
            private set
    }
}

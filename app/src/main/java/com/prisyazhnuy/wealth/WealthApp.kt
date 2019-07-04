package com.prisyazhnuy.wealth

import android.app.Application

class WealthApp : Application() {

    companion object {
        lateinit var instance: WealthApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
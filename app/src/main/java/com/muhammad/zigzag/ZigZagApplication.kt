package com.muhammad.zigzag

import android.app.Application
import com.muhammad.zigzag.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ZigZagApplication : Application() {
    companion object{
        lateinit var INSTANCE : ZigZagApplication
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        startKoin {
            androidContext(this@ZigZagApplication)
            androidLogger()
            modules(appModule)
        }
    }
}
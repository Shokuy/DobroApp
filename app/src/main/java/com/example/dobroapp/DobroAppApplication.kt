package com.example.dobroapp

import android.app.Application
import com.example.dobroapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DobroAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DobroAppApplication)
            modules(appModule)
        }
    }
}

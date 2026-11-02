package com.skillet.tintes

import android.app.Application
import com.skillet.tintes.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TintesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TintesApplication)
            modules(appModules)
        }
    }
}
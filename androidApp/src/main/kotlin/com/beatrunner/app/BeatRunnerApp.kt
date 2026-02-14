package com.beatrunner.app

import android.app.Application
import appModule
import com.beatrunner.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BeatRunnerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@BeatRunnerApp)
            modules(
                appModule,
                androidModule(this@BeatRunnerApp)
            )
        }
    }
}

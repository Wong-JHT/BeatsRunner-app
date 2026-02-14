package com.beatrunner.di

import android.content.Context
import appModule
import com.beatrunner.domain.music.MusicObserver
import com.beatrunner.platform.AndroidMusicObserver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/** Android-specific Koin module */
fun androidModule(context: Context) = module {
    // Settings using SharedPreferences
    single<Settings> {
        SharedPreferencesSettings(
                context.getSharedPreferences("beat_runner_prefs", Context.MODE_PRIVATE)
        )
    }

    // Android MusicObserver
    single<MusicObserver> { AndroidMusicObserver(context) }

    // Bluetooth
    single { com.beatrunner.domain.bluetooth.BluetoothPermissionChecker(context) }
}

/** Initialize Koin for Android Call this from Application.onCreate() */
fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(appModule, androidModule(context))
    }
}

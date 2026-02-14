package com.beatrunner.di

import com.beatrunner.domain.music.MusicObserver
import com.beatrunner.platform.IosMusicObserver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/** iOS-specific Koin module */
val iosModule = module {
    // Settings using NSUserDefaults
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }

    // iOS MusicObserver
    single<MusicObserver> { IosMusicObserver() }

    // Bluetooth
    single { com.beatrunner.domain.bluetooth.BluetoothPermissionChecker() }
}

/** Initialize Koin for iOS Call this from Swift App initializer */
fun initKoin() {
    startKoin { modules(appModule, iosModule) }
}

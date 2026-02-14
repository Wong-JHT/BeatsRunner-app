package com.beatrunner.di

import com.beatrunner.viewmodel.AuthViewModel
import com.beatrunner.viewmodel.WorkoutHistoryViewModel
import com.beatrunner.viewmodel.WorkoutViewModel
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

@OptIn(BetaInteropApi::class)
fun Koin.get(objCClass: ObjCClass): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, null, null)
}

object KoinHelper {
    private lateinit var koinApp: KoinApplication

    fun start() {
        koinApp = startKoin { modules(appModule, iosModule) }
    }

    fun getKoin(): Koin = koinApp.koin

    // Helper methods to get ViewModels for iOS
    fun getAuthViewModel(): AuthViewModel = getKoin().get()

    fun getWorkoutHistoryViewModel(): WorkoutHistoryViewModel = getKoin().get()

    fun getWorkoutViewModel(): WorkoutViewModel = getKoin().get()

    fun getBluetoothViewModel(): com.beatrunner.viewmodel.BluetoothViewModel = getKoin().get()
}

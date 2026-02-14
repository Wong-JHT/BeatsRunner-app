import com.beatrunner.data.local.DeviceStorage
import com.beatrunner.data.local.TokenManager
import com.beatrunner.data.network.BackendApi
import com.beatrunner.data.network.KtorBackendApi
import com.beatrunner.domain.bluetooth.FtmsManager
import com.beatrunner.domain.bluetooth.KableFtmsManager
import com.beatrunner.domain.music.MusicObserver
import com.beatrunner.viewmodel.AuthViewModel
import com.beatrunner.viewmodel.BluetoothViewModel
import com.beatrunner.viewmodel.WorkoutHistoryViewModel
import com.beatrunner.viewmodel.WorkoutViewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module Note: Settings is provided platform-specifically in Android/iOS
 * modules
 */
val appModule = module {
    // Token Manager
    single { TokenManager(get()) }
    single { DeviceStorage(get()) }

    // Bluetooth - Real Kable implementation
    single<FtmsManager> { KableFtmsManager() }

    // Backend API
    single<BackendApi> {
        // TIPS:
        // By default use "http://10.0.2.2:8080" for Android Emulator
        // Use "http://192.168.3.183:8080" (or your specific LAN IP) for Physical Device
        // Ensure your Mac Firewall is OFF if using Physical Device
        val baseUrl = "http://192.168.3.183:8080" 
        // val baseUrl = "http://10.0.2.2:8080"
        
        KtorBackendApi(
                baseUrl = baseUrl,
                tokenManager = get()
        )
    }

    // ViewModels
    single { AuthViewModel(get(), get()) }

    // BluetoothViewModel
    factory {
        BluetoothViewModel(ftmsManager = get<FtmsManager>(), deviceStorage = get<DeviceStorage>())
    }

    // WorkoutHistoryViewModel
    single { WorkoutHistoryViewModel(get()) }

    // WorkoutViewModel - NOTE: MusicObserver must be provided platform-specifically
    // This is a factory to defer creation until MusicObserver is available
    factory {
        WorkoutViewModel(
                musicObserver = get<MusicObserver>(),
                ftmsManager = get(),
                backendApi = get()
        )
    }
}

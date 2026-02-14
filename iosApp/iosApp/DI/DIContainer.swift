import Foundation
import shared

/**
 * Simple DI Container for iOS
 * Provides access to ViewModels from the shared Koin module
 */
class DIContainer {
    static let shared = DIContainer()
    
    private init() {}
    
    func getWorkoutHistoryViewModel() -> WorkoutHistoryViewModel {
        return KoinHelper.shared.getWorkoutHistoryViewModel()
    }
    
    func getAuthViewModel() -> AuthViewModel {
        return KoinHelper.shared.getAuthViewModel()
    }
    
    func getWorkoutViewModel(): WorkoutViewModel {
        return KoinHelper.shared.getWorkoutViewModel()
    }

    func getBluetoothViewModel() -> BluetoothViewModel {
        return KoinHelper.shared.getBluetoothViewModel()
    }
}

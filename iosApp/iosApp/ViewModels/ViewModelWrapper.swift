import SwiftUI
import Shared
import Combine

/**
 * Wrapper for AuthViewModel to make it easier to use in SwiftUI
 */
class AuthViewModelWrapper: ObservableObject {
    private let viewModel: AuthViewModel
    private var cancellables = Set<AnyCancellable>()
    
    @Published var isLoggedIn: Bool = false
    @Published var authState: AuthViewModel.AuthState = AuthViewModel.AuthStateIdle()
    @Published var currentUsername: String? = nil
    
    init(viewModel: AuthViewModel) {
        self.viewModel = viewModel
        observeFlows()
    }
    
    private func observeFlows() {
        // Observe isLoggedIn
        createPublisher(for: viewModel.isLoggedIn)
            .compactMap { (value: Any?) -> Bool? in value as? Bool }
            .assign(to: &$isLoggedIn)
        
        // Observe authState
        createPublisher(for: viewModel.authState)
            .compactMap { (value: Any?) -> AuthViewModel.AuthState? in value as? AuthViewModel.AuthState }
            .assign(to: &$authState)
        
        // Observe currentUsername
        createPublisher(for: viewModel.currentUsername)
            .compactMap { (value: Any?) -> String? in value as? String }
            .assign(to: &$currentUsername)
    }
    
    func login(username: String, password: String) {
        Task {
            do {
                try await viewModel.login(identifier: username, password: password)
            } catch {
                print("Login error: \(error)")
            }
        }
    }
    
    func register(username: String, password: String, email: String?) {
        Task {
            do {
                try await viewModel.register(username: username, password: password, email: email)
            } catch {
                print("Register error: \(error)")
            }
        }
    }
    
    func logout() {
        Task {
            try? await viewModel.logout()
        }
    }
    
    func resetAuthState() {
        viewModel.resetAuthState()
    }
    
    // Helper to create Combine publisher from Kotlin StateFlow
    private func createPublisher(for stateFlow: Kotlinx_coroutines_coreStateFlow) -> AnyPublisher<Any?, Never> {
        return Deferred {
            Future<Any?, Never> { promise in
                stateFlow.collect(
                    collector: SimpleFlowCollector { value in
                        promise(.success(value))
                    },
                    completionHandler: { _ in }
                )
            }
        }
        .eraseToAnyPublisher()
    }
}

/**
 * Wrapper for Kotlin WorkoutViewModel
 */
class WorkoutViewModelWrapper: ObservableObject {
    private let viewModel: WorkoutViewModel
    
    @Published var bluetoothState: BluetoothModels.ConnectionState = BluetoothModels.ConnectionStateDisconnected(message: "Not connected")
    @Published var currentSong: SongInfo? = nil
    @Published var workoutData: BluetoothModels.WorkoutData? = nil
    @Published var coachMessages: [CoachMessage] = []
    @Published var isWorkoutActive: Bool = false
    @Published var connectedDevice: BluetoothModels.BluetoothDevice? = nil
    
    var isConnected: Bool {
        bluetoothState is BluetoothModels.ConnectionStateConnected
    }
    
    var currentSpeed: Double {
        Double(workoutData?.speed ?? 0.0)
    }
    
    var currentIncline: Double {
        Double(workoutData?.incline ?? 0.0)
    }
    
    init(viewModel: WorkoutViewModel) {
        self.viewModel = viewModel
        observeFlows()
    }
    
    private func observeFlows() {
        createPublisher(for: viewModel.bluetoothState)
            .compactMap { (value: Any?) -> BluetoothModels.ConnectionState? in value as? BluetoothModels.ConnectionState }
            .assign(to: &$bluetoothState)
        
        createPublisher(for: viewModel.currentSong)
            .compactMap { (value: Any?) -> SongInfo? in value as? SongInfo }
            .assign(to: &$currentSong)
        
        createPublisher(for: viewModel.workoutData)
            .compactMap { (value: Any?) -> BluetoothModels.WorkoutData? in value as? BluetoothModels.WorkoutData }
            .assign(to: &$workoutData)
        
        createPublisher(for: viewModel.connectedDevice)
            .compactMap { (value: Any?) -> BluetoothModels.BluetoothDevice? in value as? BluetoothModels.BluetoothDevice }
            .assign(to: &$connectedDevice)
        
        createPublisher(for: viewModel.coachMessages)
            .compactMap { (value: Any?) -> [CoachMessage]? in value as? [CoachMessage] }
            .assign(to: &$coachMessages)
        
        createPublisher(for: viewModel.isWorkoutActive)
            .compactMap { (value: Any?) -> Bool? in value as? Bool }
            .assign(to: &$isWorkoutActive)
    }
    
    func startWorkout(device: BluetoothModels.BluetoothDevice) {
        Task {
            do {
                try await viewModel.startWorkout(device: device)
            } catch {
                print("Start workout error: \(error)")
            }
        }
    }
    
    func stopWorkout() {
        Task {
            do {
                try await viewModel.stopWorkout()
            } catch {
                print("Stop workout error: \(error)")
            }
        }
    }
    
    func setSpeed(speed: Float) {
        viewModel.setSpeed(speed: speed)
    }
    
    func setIncline(incline: Float) {
        viewModel.setIncline(incline: incline)
    }
    
    private func createPublisher(for stateFlow: Kotlinx_coroutines_coreStateFlow) -> AnyPublisher<Any?, Never> {
        return Deferred {
            Future<Any?, Never> { promise in
                stateFlow.collect(
                    collector: SimpleFlowCollector { value in
                        promise(.success(value))
                    },
                    completionHandler: { _ in }
                )
            }
        }
        .eraseToAnyPublisher()
    }
}

/**
 * Simple Flow collector for Kotlin Flow observation
 */
class SimpleFlowCollector: Kotlinx_coroutines_coreFlowCollector {
    let callback: (Any?) -> Void
    
    init(callback: @escaping (Any?) -> Void) {
        self.callback = callback
    }
    
    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        callback(value)
        completionHandler(nil)
    }
}

/**
 * Wrapper for BluetoothViewModel
 */
class BluetoothViewModelWrapper: ObservableObject {
    private let viewModel: BluetoothViewModel
    
    @Published var knownDevices: [BluetoothViewModel.DeviceUiModel] = []
    @Published var availableDevices: [BluetoothViewModel.DeviceUiModel] = []
    
    init(viewModel: BluetoothViewModel) {
        self.viewModel = viewModel
        observeFlows()
    }
    
    private func observeFlows() {
        createPublisher(for: viewModel.uiState)
            .compactMap { (value: Any?) -> BluetoothViewModel.DeviceListState? in value as? BluetoothViewModel.DeviceListState }
            .sink { [weak self] state in
                guard let self = self, let state = state else { return }
                self.knownDevices = state.knownDevices
                self.availableDevices = state.availableDevices
            }
            .store(in: &cancellables)
    }
    
    private var cancellables = Set<AnyCancellable>()
    
    func startScanning() {
        viewModel.startScanning()
    }
    
    func stopScanning() {
        viewModel.stopScanning()
    }
    
    func connectToDevice(device: BluetoothModels.BluetoothDevice) {
        viewModel.connectToDevice(device: device)
    }
    
    func disconnect() {
        viewModel.disconnect()
    }

    private func createPublisher(for stateFlow: Kotlinx_coroutines_coreStateFlow) -> AnyPublisher<Any?, Never> {
        return Deferred {
            Future<Any?, Never> { promise in
                stateFlow.collect(
                    collector: SimpleFlowCollector { value in
                        promise(.success(value))
                    },
                    completionHandler: { _ in }
                )
            }
        }
        .eraseToAnyPublisher()
    }
}

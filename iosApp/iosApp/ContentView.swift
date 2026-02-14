import SwiftUI
import Shared

struct ContentView: View {
    @StateObject private var authVM: AuthViewModelWrapper
    @StateObject private var workoutVM: WorkoutViewModelWrapper
    @State private var showRegister = false
    
    init() {
        // Get ViewModels from Koin
        let koin = KoinHelper.shared.getKoin()
        let authViewModel = koin.get(objCClass: AuthViewModel.self) as! AuthViewModel
        let workoutViewModel = koin.get(objCClass: WorkoutViewModel.self) as! WorkoutViewModel
        
        _authVM = StateObject(wrappedValue: AuthViewModelWrapper(viewModel: authViewModel))
        _workoutVM = StateObject(wrappedValue: WorkoutViewModelWrapper(viewModel: workoutViewModel))
    }
    
    var body: some View {
        Group {
            if authVM.isLoggedIn {
                if workoutVM.isWorkoutActive {
                    WorkoutView(viewModelWrapper: workoutVM)
                } else {
                    MainView(workoutVM: workoutVM)
                }
            } else {
                if showRegister {
                    RegisterView(
                        viewModelWrapper: authVM,
                        onRegisterSuccess: { showRegister = false },
                        onNavigateToLogin: { 
                            authVM.resetAuthState()
                            showRegister = false 
                        }
                    )
                } else {
                    LoginView(
                        viewModelWrapper: authVM,
                        onLoginSuccess: {},
                        onNavigateToRegister: { 
                            authVM.resetAuthState()
                            showRegister = true 
                        }
                    )
                }
            }
        }
    }
}

#Preview {
    ContentView()
}

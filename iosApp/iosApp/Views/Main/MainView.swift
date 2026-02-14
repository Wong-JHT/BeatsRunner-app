import SwiftUI
import Shared

struct MainView: View {
    @ObservedObject var workoutVM: WorkoutViewModelWrapper
    @State private var selectedTab = 0
    
    var body: some View {
        ZStack(alignment: .bottom) {
            TabView(selection: $selectedTab) {
                HistoryView()
                    .tabItem {
                        Label("History", systemImage: "clock")
                    }
                    .tag(0)
                
                // Placeholder for spacing
                Color.clear
                    .tabItem {
                        Text("")
                    }
                    .tag(1)
                    .disabled(true)
                
                SettingsView()
                    .tabItem {
                        Label("Settings", systemImage: "gear")
                    }
                    .tag(2)
            }
            
            // Floating Action Button
            Button(action: {
                if let device = workoutVM.connectedDevice {
                    workoutVM.startWorkout(device: device)
                } else {
                    // TODO: Navigate to Bluetooth screen
                    print("No device connected. Please connect a device first.")
                }
            }) {
                Image(systemName: "play.fill")
                    .font(.title2)
                    .foregroundColor(.white)
                    .frame(width: 60, height: 60)
                    .background(Circle().fill(Color.blue))
                    .shadow(radius: 4)
            }
            .offset(y: -20) // Adjust position
        }
    }
}

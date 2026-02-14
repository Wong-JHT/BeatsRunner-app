import SwiftUI

struct SettingsView: View {
    var body: some View {
        NavigationView {
            List {
                Section {
                    NavigationLink(destination: Text("Profile")) {
                        Label("Profile", systemImage: "person")
                    }
                }
                
                Section {
                    NavigationLink(destination: BluetoothView()) {
                        Label("Bluetooth Devices", systemImage: "wave.3.right")
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}

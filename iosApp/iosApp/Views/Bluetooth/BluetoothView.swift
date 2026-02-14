import SwiftUI

    @StateObject private var viewModelWrapper = BluetoothViewModelWrapper(viewModel: DIContainer.shared.getBluetoothViewModel())

    var body: some View {
        List {
            if !viewModelWrapper.knownDevices.isEmpty {
                Section(header: Text("Known Devices")) {
                    ForEach(viewModelWrapper.knownDevices, id: \.device.address) { deviceUiModel in
                        HStack {
                            Text(deviceUiModel.device.name ?? "Unknown Device")
                            Spacer()
                            if deviceUiModel.isConnected {
                                Text("Connected").foregroundColor(.green)
                            } else {
                                Button("Connect") {
                                    viewModelWrapper.connectToDevice(device: deviceUiModel.device)
                                }
                            }
                        }
                    }
                }
            }
            
            if !viewModelWrapper.availableDevices.isEmpty {
                Section(header: Text("Available Devices")) {
                    ForEach(viewModelWrapper.availableDevices, id: \.device.address) { deviceUiModel in
                        HStack {
                            Text(deviceUiModel.device.name ?? "Unknown Device")
                            Spacer()
                            Button("Connect") {
                                viewModelWrapper.connectToDevice(device: deviceUiModel.device)
                            }
                        }
                    }
                }
            }
            
            if viewModelWrapper.knownDevices.isEmpty && viewModelWrapper.availableDevices.isEmpty {
                Text("Searching for devices...")
                    .foregroundColor(.gray)
                    .padding()
            }
        }
        .navigationTitle("Bluetooth")
        .toolbar {
            Button(action: { viewModelWrapper.startScanning() }) {
                Image(systemName: "arrow.clockwise")
            }
        }
        .onAppear {
            viewModelWrapper.startScanning()
        }
        .onDisappear {
            // Optional: stop scanning when leaving the screen
            viewModelWrapper.stopScanning()
        }
    }
}

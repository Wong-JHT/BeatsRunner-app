import SwiftUI
import Shared

@main
struct iosApp: App {
    
    init() {
        KoinHelper.shared.start()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

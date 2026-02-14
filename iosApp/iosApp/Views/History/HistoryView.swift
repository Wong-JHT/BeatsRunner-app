import SwiftUI
import shared

struct HistoryView: View {
    @StateObject private var viewModel: WorkoutHistoryViewModelWrapper
    
    init() {
        _viewModel = StateObject(wrappedValue: WorkoutHistoryViewModelWrapper())
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                // Background gradient
                LinearGradient(
                    colors: [Color(hex: "0A0E27"), Color(hex: "1A1F3A")],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                if viewModel.workoutSessions.isEmpty && !viewModel.isLoading {
                    // Empty state
                    EmptyStateView()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(viewModel.workoutSessions, id: \.id) { session in
                                NavigationLink(destination: WorkoutDetailView(session: session)) {
                                    WorkoutSessionCard(session: session)
                                }
                                .buttonStyle(PlainButtonStyle())
                                
                                // Trigger pagination when near bottom
                                if session.id == viewModel.workoutSessions.last?.id {
                                    Color.clear
                                        .frame(height: 1)
                                        .onAppear {
                                            viewModel.loadMoreSessions()
                                        }
                                }
                            }
                            
                            // Loading more indicator
                            if viewModel.isLoadingMore {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "4A90E2")))
                                    .padding()
                            }
                        }
                        .padding(16)
                    }
                    .refreshable {
                        await viewModel.refreshSessions()
                    }
                }
            }
            .navigationTitle("训练历史")
            .navigationBarTitleDisplayMode(.large)
            .alert("错误", isPresented: .constant(viewModel.error != nil)) {
                Button("关闭") {
                    viewModel.clearError()
                }
            } message: {
                if let error = viewModel.error {
                    Text(error)
                }
            }
        }
    }
}

struct WorkoutSessionCard: View {
    let session: WorkoutSessionSummary
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Date and duration
            HStack {
                Text(DateTimeUtilsHelper.formatRelativeTime(timestamp: session.startTime))
                    .font(.headline)
                    .foregroundColor(Color(hex: "E8E8E8"))
                
                Spacer()
                
                Text(DateTimeUtilsHelper.formatDuration(seconds: Int(session.duration)))
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: "4A90E2"))
            }
            
            // Metrics grid
            HStack(spacing: 20) {
                MetricItem(
                    icon: "🏃",
                    label: "距离",
                    value: DateTimeUtilsHelper.formatDistance(kilometers: session.distance)
                )
                
                MetricItem(
                    icon: "🔥",
                    label: "卡路里",
                    value: DateTimeUtilsHelper.formatCalories(calories: session.calories)
                )
                
                MetricItem(
                    icon: "⚡",
                    label: "平均速度",
                    value: session.avgSpeed != nil ? String(format: "%.1f", session.avgSpeed!) : "--"
                )
            }
            
            // Heart rate if available
            if let avgHr = session.avgHeartRate {
                HStack(spacing: 4) {
                    Text("❤️")
                    Text("平均心率: \(DateTimeUtilsHelper.formatHeartRate(bpm: avgHr))")
                        .font(.caption)
                        .foregroundColor(Color(hex: "A0A0A0"))
                }
            }
        }
        .padding(16)
        .background(Color(hex: "252A3E"))
        .cornerRadius(16)
    }
}

struct MetricItem: View {
    let icon: String
    let label: String
    let value: String
    
    var body: some View {
        VStack(spacing: 4) {
            HStack(spacing: 4) {
                Text(icon)
                    .font(.body)
                Text(value)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(Color(hex: "E8E8E8"))
            }
            Text(label)
                .font(.caption2)
                .foregroundColor(Color(hex: "A0A0A0"))
        }
    }
}

struct EmptyStateView: View {
    var body: some View {
        VStack(spacing: 16) {
            Text("🏃‍♂️")
                .font(.system(size: 72))
            
            Text("还没有训练记录")
                .font(.title2)
                .foregroundColor(Color(hex: "A0A0A0"))
            
            Text("开始你的第一次训练吧！")
                .font(.body)
                .foregroundColor(Color(hex: "A0A0A0"))
        }
    }
}

// ViewModel wrapper for SwiftUI
@MainActor
class WorkoutHistoryViewModelWrapper: ObservableObject {
    private let viewModel: WorkoutHistoryViewModel
    
    @Published var workoutSessions: [WorkoutSessionSummary] = []
    @Published var isLoading: Bool = false
    @Published var isLoadingMore: Bool = false
    @Published var error: String? = nil
    
    init() {
        self.viewModel = DIContainer.shared.getWorkoutHistoryViewModel()
        observeViewModel()
    }
    
    private func observeViewModel() {
        Task {
            for await sessions in viewModel.workoutSessions {
                self.workoutSessions = sessions
            }
        }
        
        Task {
            for await loading in viewModel.isLoading {
                self.isLoading = loading.boolValue
            }
        }
        
        Task {
            for await loadingMore in viewModel.isLoadingMore {
                self.isLoadingMore = loadingMore.boolValue
            }
        }
        
        Task {
            for await err in viewModel.error {
                self.error = err
            }
        }
    }
    
    func loadMoreSessions() {
        viewModel.loadMoreSessions()
    }
    
    func refreshSessions() async {
        viewModel.refreshSessions()
        // Wait a bit for the refresh to complete
        try? await Task.sleep(nanoseconds: 500_000_000)
    }
    
    func clearError() {
        viewModel.clearError()
    }
}

// Helper for DateTimeUtils
struct DateTimeUtilsHelper {
    static func formatDuration(seconds: Int) -> String {
        return DateTimeUtils.shared.formatDuration(seconds: Int32(seconds))
    }
    
    static func formatRelativeTime(timestamp: Int64) -> String {
        return DateTimeUtils.shared.formatRelativeTime(timestamp: timestamp)
    }
    
    static func formatDistance(kilometers: KotlinFloat?) -> String {
        return DateTimeUtils.shared.formatDistance(kilometers: kilometers)
    }
    
    static func formatCalories(calories: KotlinInt?) -> String {
        return DateTimeUtils.shared.formatCalories(calories: calories)
    }
    
    static func formatSpeed(kmh: KotlinFloat?) -> String {
        return DateTimeUtils.shared.formatSpeed(kmh: kmh)
    }
    
    static func formatIncline(percentage: KotlinFloat?) -> String {
        return DateTimeUtils.shared.formatIncline(percentage: percentage)
    }
    
    static func formatHeartRate(bpm: KotlinInt?) -> String {
        return DateTimeUtils.shared.formatHeartRate(bpm: bpm)
    }
    
    static func formatDateTime(timestamp: Int64) -> String {
        return DateTimeUtils.shared.formatDateTime(timestamp: timestamp)
    }
}

// Color extension for hex colors
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

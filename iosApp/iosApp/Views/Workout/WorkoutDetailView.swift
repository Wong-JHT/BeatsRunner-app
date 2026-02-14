import SwiftUI
import shared

struct WorkoutDetailView: View {
    let session: WorkoutSessionSummary
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [Color(hex: "0A0E27"), Color(hex: "1A1F3A")],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 16) {
                    // Date and time
                    Text(DateTimeUtilsHelper.formatDateTime(timestamp: session.startTime))
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(Color(hex: "E8E8E8"))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal)
                    
                    // Summary Card
                    VStack(alignment: .leading, spacing: 16) {
                        Text("训练概要")
                            .font(.headline)
                            .foregroundColor(Color(hex: "E8E8E8"))
                        
                        HStack(spacing: 20) {
                            DetailStatItem(
                                label: "时长",
                                value: DateTimeUtilsHelper.formatDuration(seconds: Int(session.duration)),
                                icon: "⏱️"
                            )
                            
                            DetailStatItem(
                                label: "距离",
                                value: DateTimeUtilsHelper.formatDistance(kilometers: session.distance),
                                icon: "🏃"
                            )
                            
                            DetailStatItem(
                                label: "卡路里",
                                value: DateTimeUtilsHelper.formatCalories(calories: session.calories),
                                icon: "🔥"
                            )
                        }
                    }
                    .padding(20)
                    .background(Color(hex: "252A3E"))
                    .cornerRadius(16)
                    .padding(.horizontal)
                    
                    // Speed and Incline Card
                    VStack(alignment: .leading, spacing: 16) {
                        Text("速度与坡度")
                            .font(.headline)
                            .foregroundColor(Color(hex: "E8E8E8"))
                        
                        DetailMetricRow(
                            label: "平均速度",
                            value: DateTimeUtilsHelper.formatSpeed(kmh: session.avgSpeed),
                            icon: "⚡"
                        )
                        
                        DetailMetricRow(
                            label: "平均坡度",
                            value: DateTimeUtilsHelper.formatIncline(percentage: session.avgIncline),
                            icon: "📈"
                        )
                    }
                    .padding(20)
                    .background(Color(hex: "252A3E"))
                    .cornerRadius(16)
                    .padding(.horizontal)
                    
                    // Heart Rate Card (if available)
                    if session.avgHeartRate != nil || session.maxHeartRate != nil {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("心率数据")
                                .font(.headline)
                                .foregroundColor(Color(hex: "E8E8E8"))
                            
                            if let avgHr = session.avgHeartRate {
                                DetailMetricRow(
                                    label: "平均心率",
                                    value: DateTimeUtilsHelper.formatHeartRate(bpm: avgHr),
                                    icon: "❤️"
                                )
                            }
                            
                            if let maxHr = session.maxHeartRate {
                                DetailMetricRow(
                                    label: "最大心率",
                                    value: DateTimeUtilsHelper.formatHeartRate(bpm: maxHr),
                                    icon: "💓"
                                )
                            }
                        }
                        .padding(20)
                        .background(Color(hex: "252A3E"))
                        .cornerRadius(16)
                        .padding(.horizontal)
                    }
                    
                    // Chart Placeholder
                    VStack(spacing: 8) {
                        Text("📊")
                            .font(.system(size: 48))
                        Text("速度/坡度图表")
                            .font(.body)
                            .foregroundColor(Color(hex: "A0A0A0"))
                        Text("即将推出")
                            .font(.caption)
                            .foregroundColor(Color(hex: "A0A0A0"))
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 200)
                    .background(Color(hex: "252A3E"))
                    .cornerRadius(16)
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
        }
        .navigationTitle("训练详情")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct DetailStatItem: View {
    let label: String
    let value: String
    let icon: String
    
    var body: some View {
        VStack(spacing: 8) {
            Text(icon)
                .font(.title2)
            
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(Color(hex: "4A90E2"))
            
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: "A0A0A0"))
        }
        .frame(maxWidth: .infinity)
    }
}

struct DetailMetricRow: View {
    let label: String
    let value: String
    let icon: String
    
    var body: some View {
        HStack {
            HStack(spacing: 12) {
                Text(icon)
                    .font(.title3)
                Text(label)
                    .font(.body)
                    .foregroundColor(Color(hex: "E8E8E8"))
            }
            
            Spacer()
            
            Text(value)
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(Color(hex: "6FCF97"))
        }
    }
}

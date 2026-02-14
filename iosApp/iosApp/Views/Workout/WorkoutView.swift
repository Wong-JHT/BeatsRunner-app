import SwiftUI
import Shared

struct WorkoutView: View {
    @ObservedObject var viewModelWrapper: WorkoutViewModelWrapper
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0.07, green: 0.07, blue: 0.07),
                    Color(red: 0.12, green: 0.12, blue: 0.12)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 24) {
                    // Connection status bar
                    HStack {
                        Circle()
                            .fill(viewModelWrapper.isConnected ? Color.green : Color.red)
                            .frame(width: 12, height: 12)
                        Text(viewModelWrapper.isConnected ? "已连接" : "未连接")
                            .font(.subheadline)
                            .foregroundColor(.white)
                        Spacer()
                    }
                    .padding(.horizontal)
                    .padding(.top, 20)
                    
                    // Speed display
                    VStack(spacing: 8) {
                        Text(String(format: "%.1f", viewModelWrapper.currentSpeed))
                            .font(.system(size: 96, weight: .bold))
                            .foregroundStyle(
                                LinearGradient(
                                    colors: [Color.blue, Color.purple],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                        Text("km/h")
                            .font(.title2)
                            .foregroundColor(.gray)
                    }
                    .padding(.vertical)
                    
                    // Incline display
                    HStack {
                        Text("坡度")
                            .foregroundColor(.gray)
                        Spacer()
                        Text(String(format: "%.1f%%", viewModelWrapper.currentIncline))
                            .font(.title)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                    }
                    .padding()
                    .background(Color.white.opacity(0.1))
                    .cornerRadius(12)
                    .padding(.horizontal)
                    
                    // Music info card
                    if let song = viewModelWrapper.currentSong {
                        HStack(spacing: 16) {
                            // Album art placeholder
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.blue.opacity(0.3))
                                .frame(width: 60, height: 60)
                                .overlay(
                                    Image(systemName: "music.note")
                                        .foregroundColor(.white)
                                        .font(.title2)
                                )
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(song.title)
                                    .font(.headline)
                                    .foregroundColor(.white)
                                    .lineLimit(1)
                                Text(song.artist)
                                    .font(.subheadline)
                                    .foregroundColor(.gray)
                                    .lineLimit(1)
                                if let bpm = song.bpm {
                                    Text("\(bpm) BPM")
                                        .font(.caption)
                                        .foregroundColor(.blue)
                                }
                            }
                            Spacer()
                        }
                        .padding()
                        .background(Color.white.opacity(0.1))
                        .cornerRadius(12)
                        .padding(.horizontal)
                    }
                    
                    // Coach messages panel
                    VStack(alignment: .leading, spacing: 8) {
                        Text("AI 教练")
                            .font(.headline)
                            .foregroundColor(.white)
                            .padding(.horizontal)
                        
                        if viewModelWrapper.coachMessages.isEmpty {
                            Text("等待训练开始...")
                                .foregroundColor(.gray)
                                .padding()
                        } else {
                            ForEach(Array(viewModelWrapper.coachMessages.reversed().enumerated()), id: \.offset) { _, message in
                                HStack {
                                    if message.isAI {
                                        Spacer(minLength: 40)
                                    }
                                    Text(message.text)
                                        .padding()
                                        .background(message.isAI ? Color.blue : Color.gray.opacity(0.3))
                                        .foregroundColor(.white)
                                        .cornerRadius(12)
                                    if !message.isAI {
                                        Spacer(minLength: 40)
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                    }
                    .padding(.top)
                    
                    Spacer()
                }
            }
        }
    }
}

#Preview {
    Text("Preview not available without Koin context")
}

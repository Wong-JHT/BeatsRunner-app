import SwiftUI
import Shared

struct LoginView: View {
    @ObservedObject var viewModelWrapper: AuthViewModelWrapper
    @State private var username = ""
    @State private var password = ""
    
    var onLoginSuccess: () -> Void
    var onNavigateToRegister: () -> Void
    
    var body: some View {
        ZStack {
            // Gradient background
            LinearGradient(
                gradient: Gradient(colors: [
                    Color(red: 0.07, green: 0.07, blue: 0.07),
                    Color(red: 0.12, green: 0.12, blue: 0.12)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 32) {
                Spacer()
                
                // Logo
                Text("🏃")
                    .font(.system(size: 80))
                
                Text("BeatRunner")
                    .font(.system(size: 42, weight: .bold))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [Color.blue, Color.purple],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                
                Spacer().frame(height: 40)
                
                // Username field
                TextField("用户名", text: $username)
                    .textFieldStyle(CustomTextFieldStyle())
                    .autocapitalization(.none)
                
                // Password field
                SecureField("密码", text: $password)
                    .textFieldStyle(CustomTextFieldStyle())
                
                // Login button
                Button(action: {
                    if !username.isEmpty && !password.isEmpty {
                        viewModelWrapper.login(username: username, password: password)
                    }
                }) {
                    if viewModelWrapper.authState is AuthViewModel.AuthStateLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .frame(height: 24)
                    } else {
                        Text("登录")
                            .fontWeight(.semibold)
                    }
                }
                .buttonStyle(PrimaryButtonStyle())
                .disabled(viewModelWrapper.authState is AuthViewModel.AuthStateLoading)
                
                // Register link
                Button(action: onNavigateToRegister) {
                    Text("还没有账号？立即注册")
                        .foregroundColor(.blue)
                        .font(.system(size: 16))
                }
                
                // Error message
                if let errorState = viewModelWrapper.authState as? AuthViewModel.AuthStateError {
                    Text(errorState.message)
                        .foregroundColor(.red)
                        .padding()
                        .background(Color.red.opacity(0.2))
                        .cornerRadius(8)
                        .padding(.horizontal)
                }
                
                Spacer()
            }
            .padding(32)
        }
        .onChange(of: viewModelWrapper.authState) { newState in
            if newState is AuthViewModel.AuthStateSuccess {
                onLoginSuccess()
            }
        }
    }
}

// Custom TextField Style
struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color.white.opacity(0.1))
            .cornerRadius(8)
            .foregroundColor(.white)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.blue.opacity(0.5), lineWidth: 1)
            )
    }
}

// Primary Button Style
struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(8)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}

#Preview {
    Text("Preview not available without Koin context")
}

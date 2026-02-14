import SwiftUI
import Shared

struct RegisterView: View {
    @ObservedObject var viewModelWrapper: AuthViewModelWrapper
    @State private var username = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var email = ""
    
    @State private var usernameError: String? = nil
    @State private var passwordError: String? = nil
    @State private var confirmPasswordError: String? = nil
    
    var onRegisterSuccess: () -> Void
    var onNavigateToLogin: () -> Void
    
    var canRegister: Bool {
        username.count >= 3 &&
        password.count >= 6 &&
        password == confirmPassword &&
        usernameError == nil &&
        passwordError == nil &&
        confirmPasswordError == nil
    }
    
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
            
            ScrollView {
                VStack(spacing: 24) {
                    Spacer().frame(height: 40)
                    
                    // Logo
                    Text("🏃")
                        .font(.system(size: 60))
                    
                    Text("创建账号")
                        .font(.system(size: 36, weight: .bold))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [Color.blue, Color.purple],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                    
                    Spacer().frame(height: 20)
                    
                    // Username field
                    VStack(alignment: .leading, spacing: 4) {
                        TextField("用户名", text: $username)
                            .textFieldStyle(CustomTextFieldStyle())
                            .autocapitalization(.none)
                            .onChange(of: username) { newValue in
                                usernameError = newValue.count < 3 ? "用户名至少3个字符" : nil
                            }
                        if let error = usernameError {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(.leading, 4)
                        }
                    }
                    
                    // Email field (optional)
                    TextField("邮箱 (可选)", text: $email)
                        .textFieldStyle(CustomTextFieldStyle())
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                    
                    // Password field
                    VStack(alignment: .leading, spacing: 4) {
                        SecureField("密码", text: $password)
                            .textFieldStyle(CustomTextFieldStyle())
                            .onChange(of: password) { newValue in
                                passwordError = newValue.count < 6 ? "密码至少6个字符" : nil
                            }
                        if let error = passwordError {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(.leading, 4)
                        }
                    }
                    
                    // Confirm password field
                    VStack(alignment: .leading, spacing: 4) {
                        SecureField("确认密码", text: $confirmPassword)
                            .textFieldStyle(CustomTextFieldStyle())
                            .onChange(of: confirmPassword) { newValue in
                                confirmPasswordError = newValue != password ? "密码不匹配" : nil
                            }
                        if let error = confirmPasswordError {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                                .padding(.leading, 4)
                        }
                    }
                    
                    Spacer().frame(height: 8)
                    
                    // Register button
                    Button(action: {
                        viewModelWrapper.register(
                            username: username,
                            password: password,
                            email: email.isEmpty ? nil : email
                        )
                    }) {
                        if viewModelWrapper.authState is AuthViewModel.AuthStateLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .frame(height: 24)
                        } else {
                            Text("注册")
                                .fontWeight(.semibold)
                        }
                    }
                    .buttonStyle(PrimaryButtonStyle())
                    .disabled(!canRegister || viewModelWrapper.authState is AuthViewModel.AuthStateLoading)
                    
                    // Login link
                    Button(action: onNavigateToLogin) {
                        Text("已有账号？立即登录")
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
                    }
                    
                    Spacer().frame(height: 40)
                }
                .padding(32)
            }
        }
        .onChange(of: viewModelWrapper.authState) { newState in
            if newState is AuthViewModel.AuthStateSuccess {
                onRegisterSuccess()
            }
        }
    }
}

#Preview {
    // Start Koin if not started (for preview context)
    // KoinHelper.shared.start() // Careful with double start in previews
    
    // Mock or get real VM
    // For preview, we might assume Koin is not ready or we mock it.
    // Let's just create a dummy if possible, or try to get if Koin started.
    // But since start() is global, it might crash if called twice.
    
    // Better approach for Preview: Just use a dummy VM wrapper if possible, or mock the VM.
    // Since AuthViewModel is a class, we might need a real instance.
    
    /* 
       Note: SwiftUI Previews with KMD DI can be tricky. 
       We will assume Koin is running or just comment out for now to fix build.
    */
    
    Text("Preview not available without Koin context")
}

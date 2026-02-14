# BeatRunner iOS App

BeatRunner 的 iOS 原生应用，使用 SwiftUI 构建 UI，通过 Kotlin Multiplatform shared framework 共享业务逻辑。

## 架构

```
SwiftUI Views (iOS 原生)
        ↓
  ViewModelWrapper
        ↓
Shared Framework (Kotlin)
        ↓
  业务逻辑 (100% 共享)
```

## 项目结构

```
iosApp/
├── iosApp/
│   ├── iosApp.swift                    # App 入口
│   ├── ContentView.swift               # 根视图
│   ├── Views/
│   │   ├── Auth/
│   │   │   ├── LoginView.swift         # 登录界面
│   │   │   └── RegisterView.swift      # 注册界面
│   │   └── Workout/
│   │       └── WorkoutView.swift       # 主界面
│   ├── ViewModels/
│   │   └── ViewModelWrapper.swift      # Kotlin ViewModel 包装器
│   └── Info.plist                      # 权限配置
```

## 技术栈

- **UI**: SwiftUI
- **业务逻辑**: Kotlin Multiplatform (shared framework)
- **依赖注入**: Koin
- **状态管理**: Kotlin StateFlow → SwiftUI @Published

## 构建说明

### 1. 生成 Shared Framework

```bash
cd /path/to/BeatsRunner-app
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

Framework 输出路径：
- Simulator: `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework`
- Device: `shared/build/bin/iosArm64/debugFramework/shared.framework`

### 2. 在 Xcode 中打开项目

```bash
cd iosApp
open iosApp.xcodeproj
```

### 3. 配置 Build Script

在 Xcode 中：
1. 选择 Target → Build Phases
2. 添加 New Run Script Phase
3. 脚本内容：

```bash
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### 4. 运行

1. 选择模拟器或真机
2. ⌘R 运行

## 核心组件

### ViewModelWrapper

桥接 Kotlin `StateFlow` 到 SwiftUI `@Published`：

```swift
class AuthViewModelWrapper: ObservableObject {
    @Published var isLoggedIn: Bool = false
    @Published var authState: AuthViewModel.AuthState
    
    init(viewModel: AuthViewModel) {
        // Observe Kotlin StateFlow
        viewModel.isLoggedIn.watch { [weak self] value in
            self?.isLoggedIn = value?.boolValue ?? false
        }
    }
}
```

### SwiftUI 视图

- **LoginView**: 登录界面（渐变背景、Material 风格）
- **RegisterView**: 注册界面（表单验证）
- **WorkoutView**: 主界面（速度显示、音乐卡片、AI 消息）
- **ContentView**: 导航路由（认证流程 ↔ 主应用）

## 权限配置

`Info.plist` 中已配置：

- ✅ 蓝牙权限 (`NSBluetoothAlwaysUsageDescription`)
- ✅ 音乐库访问 (`NSAppleMusicUsageDescription`)
- ✅ 后台模式 (蓝牙、音频)

## 功能特性

与 Android 应用功能完全一致：

1. ✅ 用户注册/登录
2. ✅ JWT Token 自动登录
3. ✅ 音乐监听（MPNowPlayingInfoCenter）
4. ✅ 蓝牙 FTMS 跑步机控制
5. ✅ AI 教练实时指导

## 测试

### 模拟器测试

```bash
# 构建并运行
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj
```

### 真机测试

需要：
- Apple Developer 账号
- 配置签名证书
- 授予蓝牙和音乐权限

## 常见问题

### Q: Framework 找不到？
A: 运行 `./gradlew :shared:embedAndSignAppleFrameworkForXcode`

### Q: Koin 初始化失败？
A: 确保 `iosApp.swift` 中调用了 `IOSModuleKt.initKoin()`

### Q: StateFlow 观察无响应？
A: 检查 `ViewModelWrapper` 中的 `watch()` 实现

## 相关文档

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [SwiftUI](https://developer.apple.com/xcode/swiftui/)
- [Koin](https://insert-koin.io/)

---

**一套业务逻辑，原生 iOS 体验** 🚀

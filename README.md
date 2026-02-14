# BeatRunner

BeatRunner 是一款创新的 KMP (Kotlin Multiplatform) 移动应用，通过监听音乐节奏自动调节跑步机速度，让您的训练更智能、更有趣。

## 功能特性

- 🎵 **智能音乐监听**：自动捕获 Android/iOS 系统正在播放的音乐
- 🤖 **AI 教练**：基于音乐 BPM 和您的运动数据，实时调整训练强度
- 📶 **蓝牙控制**：通过 FTMS 协议无缝控制跑步机速度和坡度
- 🎨 **沉浸式 UI**：使用 Compose Multiplatform 构建的精美仪表盘
- 📊 **训练记录**：本地数据库存储您的每一次进步

## 技术栈

- **核心**：Kotlin 2.1+ with Kotlin Multiplatform
- **UI**：Compose Multiplatform
- **蓝牙**：Kable (BLE GATT)
- **网络**：Ktor Client
- **数据库**：SQLDelight
- **依赖注入**：Koin

## 项目结构

```
BeatsRunner-app/
├── shared/              # 跨平台共享代码
│   ├── commonMain/      # 公共业务逻辑和 UI
│   ├── androidMain/     # Android 特定实现
│   └── iosMain/         # iOS 特定实现
├── androidApp/          # Android 应用
└── iosApp/              # iOS 应用 (待实现)
```

## 开发进度

参见 [任务清单](docs/task.md) 和 [实施计划](docs/implementation_plan.md)

## 构建说明

### Android

```bash
./gradlew androidApp:assembleDebug
```

### iOS (需要 macOS)

```bash
./gradlew shared:linkDebugFrameworkIosArm64
```

然后在 Xcode 中打开 `iosApp/iosApp.xcodeproj`

## 许可证

待定

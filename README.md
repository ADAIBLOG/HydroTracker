# 🌊 HydroTracker

A modern, intelligent water intake tracking application built with **Android Jetpack Compose** and **Material 3 Expressive APIs**. HydroTracker helps users maintain optimal hydration through personalized tracking, smart notifications, and comprehensive analytics.

## 📱 Features

### Core Functionality
- **Daily Water Tracking**: Log water intake with pre-defined container presets or custom amounts
- **Progress Visualization**: Real-time progress tracking with animated Material 3 wavy progress indicators
- **Goal Setting**: Personalized daily hydration goals based on user profile
- **Smart Analytics**: Comprehensive daily and historical statistics with trend analysis

### User Experience
- **Material 3 Design**: Latest Material Design 3 with Expressive APIs and dynamic theming
- **Personalized Onboarding**: Multi-step onboarding flow to collect user preferences
- **Container Presets**: Quick-add functionality with common container types (glass, bottle, cup)
- **History & Analytics**: Detailed intake history with patterns and insights

### Smart Features
- **Intelligent Notifications**: Context-aware hydration reminders that respect sleep schedules
- **Home Screen Widgets**: Multiple widget sizes (4x1, 2x1, 4x2) with real-time progress updates
- **Activity-Based Goals**: Hydration recommendations based on activity level and personal metrics
- **Data Persistence**: Robust SQLite database with automatic backups

### Accessibility & Customization
- **Theme Customization**: Dynamic colors, dark mode support, and accessibility features
- **Flexible Scheduling**: Customizable reminder intervals and active hours
- **Profile Management**: Comprehensive user profile with activity levels and preferences

## 🛠️ Technical Architecture

### Built With
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit with Material 3 Expressive APIs
- **Room Database** - SQLite abstraction with reactive queries
- **Navigation Compose** - Type-safe navigation with smooth transitions
- **Material 3** - Latest Material Design components and theming
- **Coroutines & Flow** - Asynchronous programming and reactive streams

### Architecture Pattern
- **MVVM** - Model-View-ViewModel architecture
- **Repository Pattern** - Clean separation of data layer
- **Single Activity** - Modern Android navigation architecture
- **Compose-First** - Built entirely with Jetpack Compose

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/HydroTracker.git
   cd HydroTracker
   ```

2. **Build Requirements**
   - Android Studio Koala Feature Drop | 2024.1.2 or later
   - Kotlin 2.0.21+
   - Gradle 8.12.0+
   - Android SDK 34+ (target SDK 36)

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

## 🚀 Getting Started

### First Launch
1. **Onboarding Setup**: Complete the personalized onboarding flow
   - Basic profile information (age, gender, activity level)
   - Daily hydration goal calculation
   - Notification preferences and timing
   - Container preset selection

2. **Grant Permissions**: Allow notifications for hydration reminders
   - POST_NOTIFICATIONS (Android 13+)
   - SCHEDULE_EXACT_ALARM for precise timing

3. **Start Tracking**: Begin logging your daily water intake
   - Use quick-add container presets
   - Add custom amounts via floating action button
   - View real-time progress on home screen

### Daily Usage
- **Quick Add**: Tap container presets in the Material 3 carousel
- **Custom Amounts**: Use the + FAB for specific volumes
- **Progress Monitoring**: View animated progress indicators and statistics
- **History Review**: Access detailed analytics in the History tab

## 📊 Screenshots

*Add your app screenshots here to showcase the beautiful Material 3 interface*

## 🔧 Configuration

### Notification Settings
Configure hydration reminders in Settings:
- **Reminder Interval**: 15-120 minutes
- **Active Hours**: Set wake up and sleep times
- **Notification Style**: Choose reminder content and actions

### Widget Setup
Add home screen widgets for quick access:
1. Long press on home screen
2. Select "Widgets" → "HydroTracker"
3. Choose widget size (Progress, Compact, or Large)
4. Widgets update automatically with your progress

### Theme Customization
Personalize your experience:
- **Dynamic Colors**: Automatic system color extraction (Android 12+)
- **Dark Mode**: Automatic or manual theme selection
- **Color Sources**: Choose from multiple Material You color palettes

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Specific Test Classes
```bash
./gradlew test --tests "com.cemcakmak.hydrotracker.ExampleUnitTest"
```

## 📈 Analytics & Privacy

### Data Collection
HydroTracker prioritizes user privacy:
- **Local Storage**: All data stored locally using Room database
- **No Cloud Sync**: Data remains on your device
- **No Analytics**: No usage tracking or personal data collection
- **Offline First**: Full functionality without internet connection

### Data Export
Future versions will support:
- CSV export of hydration data
- Backup and restore functionality
- Health app integration

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Follow Kotlin coding conventions and Material Design guidelines
4. Write tests for new functionality
5. Submit a pull request with detailed description

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use Material 3 components and design tokens
- Implement responsive layouts for all screen sizes
- Ensure accessibility compliance

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: [GitHub Issues](https://github.com/your-username/HydroTracker/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/HydroTracker/discussions)
- **Documentation**: Comprehensive technical documentation available

## 🙏 Acknowledgments

- **Material Design Team** - For the beautiful Material 3 design system
- **Android Jetpack Team** - For Compose and modern Android development tools
- **Community Contributors** - For feedback, testing, and feature suggestions

---

**HydroTracker** - Stay hydrated, stay healthy! 🌊💧

*Built with ❤️ using Android Jetpack Compose and Material 3*
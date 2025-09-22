# Talos Rowing

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

Talos Rowing is an open source mobile device application for rowers to monitor their rowing technique. It uses device sensors to show speed, stroke rate, distance, and real-time metrics & graphs about rowing quality. The project consists of Java modules for desktop (Swing) and Android applications.

## Working Effectively

### Prerequisites and Environment Setup
- Java 17 is required and already available on this system at `/usr/lib/jvm/temurin-17-jdk-amd64`
- Gradle wrapper is available - always use `./gradlew` instead of system gradle
- For GUI applications, use `xvfb-run -a -s "-screen 0 1024x768x24"` prefix for headless display

### Bootstrap and Build Process
- **Initial setup**: Use the Gradle wrapper - run `./gradlew --version` to initialize if needed
- **Android SDK issue**: The default build fails due to incomplete Android SDK. Use the workaround below.
- **Java-only build workaround**: 
  1. Temporarily modify `settings.gradle.kts` to comment out Android modules:
     ```kotlin
     // include(":android-common")
     // include(":talos-main") 
     // include(":talos-remote")
     // project(":android-common").projectDir = file("android/android-common")
     // project(":talos-main").projectDir = file("android/android-apk/talos-main")
     // project(":talos-remote").projectDir = file("android/android-apk/talos-remote")
     ```
  2. Then run builds normally
- **Build Java modules**: `./gradlew build -x test` -- takes 2-3 seconds, NEVER CANCEL
- **Build with tests**: `./gradlew :common:test` -- takes 6-7 seconds, NEVER CANCEL

### Run Applications
- **Desktop Swing app**: `xvfb-run -a -s "-screen 0 1024x768x24" ./gradlew :swing:run`
  - The app will start successfully and show "launching Talos Rowing" message
  - Runs indefinitely until terminated - use timeout or Ctrl+C to stop
- **Common module tests**: `./gradlew :common:test` - these pass consistently

### Build Times and Timeouts
- **NEVER CANCEL**: Build operations are very fast but always set timeouts of 30+ seconds minimum
- Java module build: 1-2 seconds when up-to-date, 2-3 seconds on first run
- Common module tests: 1 second when up-to-date, 6-7 seconds on first run  
- Swing GUI startup: 2-3 seconds then runs continuously
- **Gradle daemon startup**: First run takes 15-20 seconds to download and setup
- **Clean builds**: Take slightly longer but still under 10 seconds

## Validation
- **Always test Swing app startup** after changes using `xvfb-run -a -s "-screen 0 1024x768x24" timeout 15 ./gradlew :swing:run`
- **Always run common tests** with `./gradlew :common:test` before committing
- **Build validation**: Run `./gradlew build -x test` to verify compilation
- The Swing application can be built and started but cannot be interacted with in headless mode
- Tests in the swing module fail due to GUI dependencies - this is expected in headless environment

## Android Development
- **Android SDK required**: The Android modules require a complete Android SDK installation
- **Current limitation**: System Android SDK is incomplete - missing platforms and proper Gradle plugin resolution
- **Workaround for Java-only development**: Temporarily modify `settings.gradle.kts` to comment out Android modules:
  ```kotlin
  // include(":android-common")
  // include(":talos-main") 
  // include(":talos-remote")
  ```
- **Android CI**: The GitHub Actions workflow shows Android builds work with proper SDK setup

## Common Tasks

### Repository Structure
```
.
├── README.md                    - Project overview
├── settings.gradle.kts          - Multi-module Gradle configuration  
├── gradle.properties           - Gradle JVM settings
├── common/                     - Core Java library (sensor data, rowing logic)
├── swing/                      - Desktop Swing application
├── android/                    - Android applications
│   ├── android-common/         - Shared Android code
│   └── android-apk/           - Android app modules
│       ├── talos-main/        - Main Android app
│       └── talos-remote/      - Remote Android app
├── etc/                       - Documentation and resources
└── .github/workflows/         - CI/CD configuration
```

### Key Commands Reference
```bash
# Build Java modules (fast, 1-2 seconds when up-to-date)
./gradlew build -x test

# Test common module (1 second when up-to-date, 6-7 seconds on first run)  
./gradlew :common:test

# Run Swing desktop app
xvfb-run -a -s "-screen 0 1024x768x24" ./gradlew :swing:run

# Clean build artifacts
./gradlew clean

# View available tasks (works with Java-only setup)
./gradlew tasks

# Check project structure (shows common and swing modules)
./gradlew projects

# Build distribution packages for Swing app
./gradlew :swing:distZip
```

### Quick Development Workflow
1. **Setup for Java development**: Modify `settings.gradle.kts` to comment out Android modules (see Bootstrap section)
2. Make changes to Java code in `common/` or `swing/` modules
3. Build and test: `./gradlew build -x test && ./gradlew :common:test`
4. Validate Swing app: `xvfb-run -a -s "-screen 0 1024x768x24" timeout 15 ./gradlew :swing:run`
5. Expected output: "launching Talos Rowing" message indicates successful startup

### Module Dependencies
- `swing` depends on `common` - changes to common require rebuilding swing
- All modules use JUnit 4.13.2 for testing
- Swing module uses additional dependencies: VLC, JNA, ZXing for barcode generation
- Common module has minimal dependencies: SLF4J for logging

### Troubleshooting Common Issues
- **"Plugin [id: 'com.android.library'] was not found"**: Android SDK not properly configured
- **"BUILD FAILED" on Android modules**: Use Java-only workflow by commenting out Android includes
- **GUI tests fail**: Expected in headless environment - use `xvfb-run` prefix for GUI operations
- **Gradle daemon issues**: Run `./gradlew --stop` and retry
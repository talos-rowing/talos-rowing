# Talos Rowing

Talos Rowing is a multi-platform rowing analytics application with Android and Java Swing front-ends. It analyzes rowing technique using mobile device accelerometer and GPS sensors to show speed, stroke rate, distance, and real-time rowing quality metrics.

**ALWAYS follow these instructions first and fallback to additional search and context gathering only when the information here is incomplete or found to be in error.**

## Working Effectively

### Prerequisites and Environment Setup
- **Java 17** is required and already available in the system
- **CRITICAL**: Modern Gradle is required - system Gradle 4.4.1 is too old. Use Gradle 8.5+:
  ```bash
  cd /tmp && wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && unzip gradle-8.5-bin.zip
  export PATH=/tmp/gradle-8.5/bin:$PATH
  ```
- Android SDK is required for Android modules but may not be available in CI environments

### Build Commands (Java Modules Only)
- **NEVER CANCEL**: First build may take up to 30 seconds due to dependency downloads. Set timeout to 120+ seconds.
- Build without tests (fast): `gradle build -x test` -- takes ~5-15 seconds
- Build with tests: `gradle build` -- takes ~25-30 seconds. **NEVER CANCEL** - wait for completion.
- Build specific module: `gradle :common:build` or `gradle :swing:build`
- Clean build: `gradle clean build` -- takes ~10-15 seconds

### Testing
- **Common module tests**: `gradle :common:test` -- takes ~5-8 seconds, all 8 tests should pass
- **Swing module tests**: Tests fail in headless environments (expected) due to GUI dependencies
- **NEVER CANCEL**: Test execution may take 10-15 seconds. Set timeout to 60+ seconds.
- **Note**: Tests may occasionally be flaky - if tests fail, try `gradle :common:clean :common:test`

### Running the Application
- **Swing application**: `gradle :swing:run` 
- Main class: `org.nargila.robostroke.app.RoboStrokeSwing`
- **Note**: Application will fail in headless environments with "No X11 DISPLAY" error (expected)
- Application creates distributions: `swing/build/distributions/swing.tar` and `swing.zip`

### Android Modules
- **Android modules cannot be built without Android SDK** (compileSdk 34, minSdk 30, targetSdk 34)
- If Android SDK is not available, temporarily disable Android modules in `settings.gradle.kts`:
  ```kotlin
  // Comment out ALL these lines:
  // include(":android-common")
  // include(":talos-main") 
  // include(":talos-remote")
  // project(":android-common").projectDir = file("android/android-common")
  // project(":talos-main").projectDir = file("android/android-apk/talos-main")
  // project(":talos-remote").projectDir = file("android/android-apk/talos-remote")
  ```

## Validation Steps

### Always validate changes by:
1. **Clean build**: `gradle clean build -x test` -- should complete in ~10-15 seconds
2. **Run tests**: `gradle :common:test` -- should pass all 8 tests in ~5-8 seconds  
3. **Check compilation**: Verify no compilation errors in output
4. **Test app startup**: `gradle :swing:run` -- should attempt to launch (will fail in headless)
5. **If tests fail**: Try `gradle :common:clean :common:test` to resolve flaky tests

### Key Project Structure
```
.
├── common/             # Core rowing analysis logic (Java)
├── swing/              # Desktop Swing UI application  
├── android/            # Android modules (require Android SDK)
├── settings.gradle.kts # Project configuration
├── gradle.properties   # Gradle settings
└── Makefile           # Legacy Maven commands (non-functional)
```

## Common Tasks

### Repository root contents
```
.git/
.github/
.gitignore
Makefile
README.md
android/
common/
etc/
gradle/
gradle.properties
settings.gradle.kts
swing/
```

### Available Gradle tasks
- `gradle projects` -- list all subprojects
- `gradle tasks` -- list available tasks
- `gradle :swing:run` -- run the desktop application
- `gradle build` -- build all enabled modules
- `gradle clean` -- clean build artifacts

### Timing expectations
- **Initial Gradle daemon startup**: 10-15 seconds  
- **Clean build (no tests)**: 5-10 seconds
- **Full build with tests**: 25-30 seconds (**NEVER CANCEL**)
- **Test execution**: 10-15 seconds (**NEVER CANCEL**)
- **Incremental builds**: 1-3 seconds

## Known Limitations

### Build Environment
- **Makefile is non-functional** - references Maven but no pom.xml files exist (migrated to Gradle)
- **System Gradle 4.4.1 is too old** - must use Gradle 8.5+ for settings.gradle.kts syntax
- **Android builds require Android SDK** - may not be available in all CI environments
- **GUI tests fail in headless environments** - expected behavior

### Workarounds
- **For Android SDK issues**: Temporarily disable Android modules in settings.gradle.kts
- **For headless GUI failures**: Use `-x test` flag or expect test failures in swing module
- **For old Gradle**: Download and use modern Gradle version as shown above

## Manual Testing Scenarios

After making changes, validate:
1. **Core functionality**: Ensure common module tests pass
2. **Build integrity**: Clean build succeeds without errors  
3. **Application startup**: Swing app attempts to launch (may fail in headless)
4. **No regressions**: All previously working tests continue to pass

The main Swing application provides rowing analysis capabilities including:
- Stroke rate monitoring
- GPS-based distance tracking  
- Accelerometer-based rowing technique analysis
- Real-time performance metrics display
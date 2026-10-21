# Harsh's Olympiad Lab — Android Studio Project

This ZIP is a standalone Android Studio project. The production web application is already bundled under `app/src/main/assets/public/`, alongside the native Android WebView app and reminder components.

## Build with GitHub Actions (recommended)
1. Extract this ZIP on your computer.
2. Copy the extracted project files into the root of the target GitHub repository, including the hidden `.github` directory. Confirm that `gradlew`, `settings.gradle`, `build.gradle`, `app/` and `.github/workflows/build-apk.yml` are at repository root.
3. Commit the updated files to `main` or `master`. The workflow should start automatically. You can also open **Actions → Build Android APK → Run workflow**.
4. Open the successful workflow run and download `harsh-olympiad-lab-debug-apk` from **Artifacts**.
5. Extract the downloaded artifact ZIP to obtain `app-debug.apk`.

## Build in Android Studio
1. Open Android Studio and choose **File → Open**.
2. Select the folder containing this README, `settings.gradle`, `gradlew` and the `app/` folder. This folder is the project root; do not select an `android/` subfolder.
3. Let Gradle sync. Build using **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. The APK should be at `app/build/outputs/apk/debug/app-debug.apk`.

## Build from terminal
Requires JDK 17 and an Android SDK installation. Run from this project root:

Linux/macOS:
```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

Windows PowerShell:
```powershell
.\gradlew.bat assembleDebug
```

## Project details
- Application ID: `com.harsh.olympiadlab` (debug build adds `.debug`).
- Minimum Android version: API 24.
- Compile/target SDK: 34.
- Java: 17.
- This workflow produces a debug APK for testing, not a signed production release.
- Google Sheets, Google Drive and Gemini-connected functionality still requires its relevant deployment, authorisation and backend configuration.

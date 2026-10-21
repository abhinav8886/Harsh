# Harsh's Olympiad Lab - Official Android Studio Project

This directory contains the **complete, native Android application project** for **Harsh's Olympiad Lab**.
It bundles the **genuine production application** (HTML, CSS, JavaScript, and question banks) directly inside `app/src/main/assets/public/`.

---

## What is Inside
- **Real Production App Bundle**: Pre-compiled React 18 SPA with all Olympiad features (Practice, Timed Quiz, Mock Test, Worksheets, Mistakes Review, Parent Analytics, AI Question Generator).
- **Offline Question Bank**: Built-in questions and local caching for full offline practice without an internet connection.
- **Two-way Google Sheets Sync**: Saves quiz scores and fetches live questions when online.
- **Native Android Controller**: `MainActivity.java` with hardware back-button navigation, local asset serving, and smooth progress feedback.

---

## How to Build the APK

### Option 1: Automatic Cloud Build with GitHub Actions (Recommended - 0 Installs)
1. Commit and push this repository to GitHub.
2. The included `.github/workflows/build-apk.yml` will automatically build the APK on GitHub's cloud runners.
3. Go to the **Actions** tab on your GitHub repository, click the latest workflow run, and download **harsh-olympiad-lab-debug-apk**!

### Option 2: Open in Android Studio
1. Open **Android Studio**.
2. Select **File → Open** and choose the `android` folder.
3. Wait for Gradle sync to complete.
4. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Retrieve your APK from `app/build/outputs/apk/debug/app-debug.apk`.

### Option 3: Terminal Command
```bash
chmod +x gradlew
./gradlew assembleDebug
```

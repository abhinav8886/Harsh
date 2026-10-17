# Harsh's Olympiad Lab — Android project

## Included
- Offline-first maths practice app
- Ratio & Proportion, Area & Perimeter, Fractions, Number Challenge, Logic Puzzles and Mixed Olympiad
- Multiple-choice questions, hints, explanations, points and session progress

## Build an APK
1. Install Android Studio (stable version) and its Android SDK.
2. Open this folder in Android Studio.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will show the APK location, usually `app/build/outputs/apk/debug/app-debug.apk`.

The first build needs internet to download Gradle and Android SDK components. This workspace has prepared the source project, but has not compiled or verified an APK binary.

## Notes
- The starter version runs offline and does not send data to a server.
- Points and progress reset when the app is closed; persistent progress can be added next.
- Please review the questions before formal exam use.


## Build an APK in the cloud (no Android Studio needed)

This project includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.
GitHub can compile the APK for you on its hosted build machine.

1. Sign in to GitHub and create a **new private repository** (for example, `harsh-olympiad-lab`).
2. Upload the contents of this project ZIP to that repository, keeping the `.github/workflows/build-apk.yml` file and folder structure.
   - Make sure the `.github` folder is included. Some file managers hide folders beginning with a dot.
3. Open the repository's **Actions** tab and select **Build Harsh's Olympiad Lab APK**.
4. Click **Run workflow** (if prompted, choose the `main` branch).
5. Wait for the workflow to finish with a green check mark.
6. Open the completed workflow run, scroll to **Artifacts**, and download `Harsh-Olympiad-Lab-debug-apk`.
7. Extract the downloaded artifact ZIP. It contains `app-debug.apk`. Transfer that APK to your Android phone and open it to install.
   Android may ask you to allow installation from that app (such as Chrome or Files).

Notes:
- This produces a **debug APK** for personal testing, not a Play Store release.
- The repository can be private. Do not upload personal information or credentials.
- The APK has not been compiled or tested in this chat environment; GitHub Actions will perform the actual build and report any errors.

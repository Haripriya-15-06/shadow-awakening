# Phone-only APK build

This version is prepared for a cloud build using GitHub Actions. You do NOT need Android Studio on your phone.

## From your phone

1. Create a new GitHub repository.
2. Extract this ZIP on your phone if your file manager supports extraction.
3. On the repository page, choose **Add file → Upload files**.
4. Upload the project contents, keeping these paths:
   - `app/...`
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
   - `.github/workflows/build-apk.yml`
5. Open the repository's **Actions** tab.
6. Select **Build Shadow Awakening APK**.
7. Tap **Run workflow**.
8. Wait for the build to finish.
9. Open the completed workflow run and find **Artifacts**.
10. Download **ShadowAwakening-V1-debug**.
11. Extract the downloaded artifact and install `app-debug.apk` on your Android phone.

## Important

- The APK is a debug build for testing.
- Android may ask you to allow installation from the browser/file manager. Only enable that for the app you are using to install your own APK.
- No PC is required for the build itself.

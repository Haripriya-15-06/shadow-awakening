# Shadow Awakening V1 — Phone-only APK build

This version is prepared for a **phone-only cloud build**. You do not need Android Studio on your phone.

## Easiest phone workflow: GitHub Actions

1. On your phone, download this ZIP and extract it.
2. Create a GitHub repository (a free account is enough).
3. Upload **the contents of the extracted `ShadowAwakeningV1` folder** to the repository.
4. Open the repository's **Actions** tab.
5. Choose **Build Shadow Awakening APK** and tap **Run workflow** (or push to `main`, which starts it automatically).
6. Wait for the build to finish.
7. Open the completed workflow run → **Artifacts** → download `ShadowAwakening-debug`.
8. Extract the artifact and install `app-debug.apk` on your Android phone.
9. If Android asks, allow your browser/file manager to install apps from that source.

## What V1 does

Touch → cyan chest/core activation → eye/headband glow → Japanese-style rune activation → zoom-out to full warrior → flight → dramatic drop → heavy landing → screen shake + synthesized thump + haptic feedback.

## Important

This is a **debug APK** for personal testing. It is not a Play Store release build.

The project includes a GitHub Actions workflow under `.github/workflows/build-apk.yml`, so the APK is compiled in the cloud rather than on your phone.

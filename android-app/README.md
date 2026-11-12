# Android portfolio manager

This is a native Kotlin/Jetpack Compose Android app for editing the same portfolio content as the Angular `/manage` page. It supports owner sign-in, project/experience/service editing, technology editing, publishing, and restoring the original content.

## Configure Supabase

1. Install Android Studio with Android SDK 36 and use JDK 17 for Gradle.
2. Open `android-app` in Android Studio once so it can create `local.properties` with the local Android SDK path.
3. Add these values to `local.properties`, using the same project URL, public anon/publishable key, and owner UUID as `../src/app/services/supabase.config.ts`:

   ```properties
   SUPABASE_URL=https://your-project.supabase.co
   ANON_KEY=your-public-anon-or-publishable-key
   ADMIN_USER_ID=your-owner-user-uuid
   ```

   `local.properties` is ignored by Git. Never use a Supabase `service_role` key in this app. Row-level security must be enabled as described in `../SUPABASE_SETUP.md`.

## Build and install

From `android-app`, build a debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Install it on a connected Android device with USB debugging enabled:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The app loads and saves the shared `portfolio_content` row. It asks for owner sign-in each time it starts; credentials and session tokens remain in memory only, and sessions refresh on demand while the app stays open.

## Contact inbox and replies

The Android manager includes an Inbox tab for visitor messages and email replies. Deploy the contact inbox migration and Supabase Edge Functions, then configure the Resend email secrets as described in [`../SUPABASE_SETUP.md`](../SUPABASE_SETUP.md). Refresh the inbox to check for new messages.

## Release APK

The release build uses a private signing keystore. Keep both `portfolio-release.p12` and the `RELEASE_*` settings in `local.properties` backed up securely. Do not commit or share the keystore: future updates signed with a different key will not install over this app.

Build the signed release APK from `android-app`:

```powershell
.\gradlew.bat assembleRelease
```

The APK is written to `app\build\outputs\apk\release\app-release.apk`.

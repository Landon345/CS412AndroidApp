# CS412 — Assignment 2: Intents App

**Name:** Landon Schlangen
**Student ID:** 1209690

An Android app demonstrating navigation between two activities using both
**explicit** and **implicit** intents.

## Device / Emulator and Android OS Version

| | |
|---|---|
| **Device** | Pixel 9 (Android Studio Emulator / AVD) |
| **Android OS version** | Android 17 |
| **API level** | 37 |
| **System image** | Google APIs Play Store, x86_64 (16 KB page size) |
| **Emulator version** | 37.1.11 |

### Build configuration

| | |
|---|---|
| `minSdk` | 24 (Android 7.0 Nougat) |
| `targetSdk` | 37 |
| `compileSdk` | 37 |
| Language / UI | Kotlin with Jetpack Compose (Material 3) |

## Features

### Main Activity
- Displays full name and student ID.
- **Start Activity Explicitly** — launches `SecondActivity` with an explicit
  intent that names the target component directly:
  ```kotlin
  val intent = Intent(this, SecondActivity::class.java)
  startActivity(intent)
  ```
- **Start Activity Implicitly** — launches `SecondActivity` with an implicit
  intent using a custom action, resolved by the system through the intent
  filter declared in the manifest:
  ```kotlin
  val intent = Intent("com.example.intentsapp.ACTION_CHALLENGES").apply {
      addCategory(Intent.CATEGORY_DEFAULT)
  }
  startActivity(intent)
  ```

The matching intent filter in `AndroidManifest.xml`:

```xml
<activity android:name=".SecondActivity" android:exported="true">
    <intent-filter>
        <action android:name="com.example.intentsapp.ACTION_CHALLENGES" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

### Second Activity
Displays five mobile software engineering challenges:

1. Device Fragmentation
2. OS Fragmentation
3. Unstable and Dynamic Environments
4. Rapid Changes
5. Tool Support

A **Main Activity** button returns to the main screen by calling `finish()`,
which pops this activity off the back stack and reveals the existing
`MainActivity` instance rather than creating a duplicate.

## Project Structure

```
app/src/main/
├── AndroidManifest.xml                  # Activity declarations + intent filter
├── java/com/example/intentsapp/
│   ├── MainActivity.kt                  # Name, ID, and the two launch buttons
│   ├── SecondActivity.kt                # Challenges list + return button
│   └── ui/theme/                        # Compose theme
└── res/                                 # Resources
```

## Building and Running

1. Open the project in Android Studio.
2. Select the **Pixel 9** emulator (or any device running API 24 or higher).
3. Click **Run**.

From the command line:

```bash
./gradlew assembleDebug      # build the debug APK
./gradlew installDebug       # install to a running device/emulator
```

# Assignment 4 — Part 1: Manifest & Component Analysis (NewPipe)

**Manifest analyzed:** `app/src/main/AndroidManifest.xml`
(`app/src/debug/AndroidManifest.xml` only overrides the `Application` class for debug builds and declares no additional components.)

---

## 1. Declared App Components

### Summary counts

| Component type       | Total declared | `exported="true"` (explicit or intent-filter default) |
|-----------------------|:--:|:--:|
| Activities             | 11 | 4 |
| Services                | 6 | 1 |
| Broadcast Receivers  | 1 | 1 |
| Content Providers   | 1 | 0 |

**Exported activities:** `MainActivity`, `PanicResponderActivity`, `FilePickerActivityHelper`, `RouterActivity`
**Exported services:** `PlayerService`
**Exported receivers:** `MediaButtonReceiver`
**Exported providers:** none (`FileProvider` is explicitly `exported="false"`)

All components in this manifest set `android:exported` explicitly, except three same-process-only services (`FeedLoadService`, `SystemForegroundService`, `DownloadManagerService`) which have no intent-filter and therefore default to `exported="false"`.

---

### Activities (11)

| Activity | Exported | Lifecycle callbacks overridden |
|---|:--:|---|
| `MainActivity` | true | `onCreate()`, `onStart()`, `onResume()`, `onStop()`, `onDestroy()` |
| `player.PlayQueueActivity` | false | `onCreate()`, `onDestroy()` |
| `settings.SettingsActivity` | false | `onCreate()`, `onDestroy()` |
| `about.AboutActivity` | false | *No corresponding source file found in `app/src/main`.* The manifest still declares it, but the class appears to have been removed or is mid-migration (a Compose "about" screen exists under `shared/src/commonMain/.../screen/about`). This is a stale/dangling manifest reference worth flagging. |
| `PanicResponderActivity` | true | `onCreate()` |
| `ExitActivity` | false | `onCreate()` |
| `error.ErrorActivity` | false | `onCreate()` |
| `download.DownloadActivity` | false | `onCreate()` |
| `util.FilePickerActivityHelper` | true | `onCreate()` |
| `error.ReCaptchaActivity` | false | `onCreate()` |
| `RouterActivity` | true | `onCreate()`, `onStart()`, `onStop()`, `onDestroy()` |

### Services (6)

| Service | Exported | Lifecycle callbacks overridden |
|---|:--:|---|
| `androidx.appcompat.app.AppLocalesMetadataHolderService` | false | Library component (AndroidX AppCompat) — no source in this repository. |
| `player.PlayerService` | true | `onCreate()`, `onStartCommand()`, `onBind()`, `onDestroy()` |
| `local.feed.service.FeedLoadService` | false | `onCreate()`, `onStartCommand()`, `onBind()` |
| `androidx.work.impl.foreground.SystemForegroundService` | false | Library component (AndroidX WorkManager) — no source in this repository. |
| `us.shandian.giga.service.DownloadManagerService` | false | `onCreate()`, `onStartCommand()`, `onBind()`, `onDestroy()` |
| `RouterActivity$FetcherService` | false | `onCreate()`, `onDestroy()`. Note: this is an `IntentService`, so instead of overriding `onStartCommand()` it overrides `onHandleIntent()`, which `IntentService` uses internally to dispatch work on a background thread. |

### Broadcast Receivers (1)

| Receiver | Exported | Lifecycle callbacks overridden |
|---|:--:|---|
| `androidx.media.session.MediaButtonReceiver` | true | Library component (AndroidX Media) — no source in this repository. |

### Content Providers (1)

| Provider | Exported | Notes |
|---|:--:|---|
| `androidx.core.content.FileProvider` | false | Library component; configured via `<meta-data>` pointing at `@xml/nnf_provider_paths` to expose app files (e.g. downloads) to other apps through content URIs. |

---

### Unique lifecycle callbacks used across all components

Of the callback set the assignment asks about, the app-defined components actually use:

- **`onCreate()`** — Called once when the component (Activity or Service) is first created; used to perform one-time initialization such as inflating layouts, setting up view state, or preparing resources.
- **`onStart()`** — Called when an Activity is becoming visible to the user, right before it enters the foreground.
- **`onResume()`** — Called when an Activity moves into the foreground and starts interacting with the user (top of the activity stack).
- **`onStop()`** — Called when an Activity is no longer visible to the user, e.g. because another activity has fully covered it.
- **`onDestroy()`** — Called when an Activity or Service is being permanently shut down, used to release resources and perform final cleanup.
- **`onStartCommand()`** — Called each time a Service is started via `startService()`/`startForegroundService()`, indicating how the system should handle the request (e.g. return `START_STICKY`).
- **`onBind()`** — Called when a client binds to a Service via `bindService()`, returning an `IBinder` used for client-service communication.

Not overridden by any component declared in this manifest: `onPause()`, `onRestart()` (Activity), and `onReceive()` (no first-party `BroadcastReceiver` subclass exists in the codebase — the one declared receiver, `MediaButtonReceiver`, is a library class).

---

## 2. Permissions

| Permission | Description |
|---|---|
| `android.permission.INTERNET` | Allows the app to open network sockets and access the internet, required to fetch videos/audio and metadata from streaming services. |
| `android.permission.WAKE_LOCK` | Allows the app to keep the CPU awake so audio/video playback and downloads can continue even when the screen is off. |
| `android.permission.ACCESS_NETWORK_STATE` | Allows the app to check current network connectivity state (e.g. whether Wi-Fi or mobile data is available) so it can adapt behavior accordingly. |
| `android.permission.WRITE_EXTERNAL_STORAGE` | Allows the app to write files to shared/external storage, used for saving downloaded video/audio on older Android versions. |
| `android.permission.SYSTEM_ALERT_WINDOW` | Allows the app to draw over other apps' windows, used to support the floating/popup video player mode. |
| `android.permission.FOREGROUND_SERVICE` | Allows the app to run foreground services (required on Android 9+ for any long-running background task shown via a persistent notification). |
| `android.permission.FOREGROUND_SERVICE_DATA_SYNC` | Declares intent to run a foreground service of type "dataSync" (e.g. downloads, feed loading), required on Android 14+. |
| `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Declares intent to run a foreground service of type "mediaPlayback" (the media player), required on Android 14+. |
| `android.permission.POST_NOTIFICATIONS` | Allows the app to display notifications to the user (e.g. playback controls, download progress), required at runtime on Android 13+. |

**Total: 9 `<uses-permission>` entries.**

---

## 3. Manifest Elements and Attributes

(Per assignment instructions, `android:name` and `android:label` are excluded below.)

### Unique XML elements (15)

| Element | Purpose |
|---|---|
| `<manifest>` | Root element of the file; declares the package's overall metadata and wraps every other element. |
| `<uses-permission>` | Declares a system permission the app requires at install/runtime. |
| `<queries>` | Declares other packages/intents the app needs to be able to see, required for package visibility on Android 11+. |
| `<intent>` | Inside `<queries>`, specifies a particular intent signature (action + data) the app needs visibility into. |
| `<uses-feature>` | Declares a hardware/software feature the app uses or requires, used here to mark touchscreen and leanback (TV) support as optional. |
| `<application>` | Declares application-wide attributes and contains all component declarations. |
| `<activity>` | Declares a screen (Activity) the app provides. |
| `<intent-filter>` | Declares which implicit intents a component can respond to (actions, categories, data). |
| `<action>` | Inside an `<intent-filter>`, specifies the intent action the component can handle. |
| `<category>` | Inside an `<intent-filter>`, further qualifies which intents match (e.g. `LAUNCHER`, `BROWSABLE`). |
| `<data>` | Inside an `<intent-filter>`, specifies the data type/URI scheme/host/path the component can handle. |
| `<receiver>` | Declares a `BroadcastReceiver` component that responds to system or app broadcasts. |
| `<service>` | Declares a background `Service` component. |
| `<provider>` | Declares a `ContentProvider` component that exposes structured data to other apps. |
| `<meta-data>` | Attaches an arbitrary key/value pair of extra data to the enclosing component or application. |

### Unique attributes (excluding `android:name`, `android:label`)

| Attribute | Purpose |
|---|---|
| `xmlns:android` | Declares the standard Android XML namespace used for all `android:*` attributes. |
| `xmlns:tools` | Declares the tools XML namespace used for build-time-only attributes like `tools:ignore` and `tools:node`. |
| `android:installLocation` | Controls whether the app can be installed on external (adopted) storage as well as internal storage. |
| `android:scheme` | On `<data>`, restricts an intent filter match to a specific URI scheme (e.g. `http`, `vnd.youtube`). |
| `android:required` | On `<uses-feature>`, indicates whether the declared hardware/software feature is mandatory for installation. |
| `android:allowBackup` | Controls whether the app's data may be included in automatic backup/restore. |
| `android:banner` | Specifies the banner image shown for the app on Android TV. |
| `android:icon` | Specifies the app's launcher icon. |
| `android:logo` | Specifies a secondary logo image resource for the app. |
| `android:resizeableActivity` | Indicates whether the app's activities support multi-window/split-screen resizing. |
| `android:theme` | Specifies the UI theme applied to the application or a specific component. |
| `tools:ignore` | Suppresses a specific Android Lint warning for the annotated element at build time. |
| `android:exported` | Controls whether a component can be launched/accessed by components from other applications. |
| `android:launchMode` | Controls how a new instance of an Activity is created and placed on the task back stack. |
| `android:enabled` | Controls whether the component can be instantiated by the system at all. |
| `android:value` | On `<meta-data>`, supplies the literal value paired with the meta-data's key. |
| `android:foregroundServiceType` | Declares the category of work a foreground service performs (e.g. `mediaPlayback`, `dataSync`), required on newer Android versions. |
| `tools:node` | Controls how the manifest merger combines this element with the same element from a library manifest (e.g. `merge`). |
| `android:noHistory` | Indicates the Activity should not remain in the back stack once the user navigates away from it. |
| `android:host` | On `<data>`, restricts an intent filter match to a specific URI host (e.g. `youtube.com`). |
| `android:pathPrefix` | On `<data>`, restricts an intent filter match to URIs whose path starts with the given prefix. |
| `android:mimeType` | On `<data>`, restricts an intent filter match to a specific MIME type (e.g. `text/plain`). |
| `android:sspPattern` | On `<data>`, restricts an intent filter match using a scheme-specific-part pattern (used for hostless URIs like Bandcamp custom links). |
| `android:excludeFromRecents` | Excludes the Activity's task from the system's "recent apps" list. |
| `android:taskAffinity` | Specifies which task an Activity prefers to belong to, used here to keep the router activity out of the main app's task. |
| `android:authorities` | On `<provider>`, declares the unique authority string used to address the content provider via `content://` URIs. |
| `android:grantUriPermissions` | On `<provider>`, allows temporary read/write access to be granted to specific URIs beyond the provider's normal permission model. |
| `android:resource` | On `<meta-data>`, supplies a reference to a resource (as opposed to a literal value) paired with the meta-data's key. |

---

## Sanity-check summary

- **11** Activities (4 exported)
- **6** Services (1 exported)
- **1** Broadcast Receiver (1 exported)
- **1** Content Provider (0 exported)
- **9** `<uses-permission>` entries
- **15** unique manifest elements
- **28** unique attributes (excluding `android:name`/`android:label`)
- **7** unique lifecycle callbacks actually overridden in-repo: `onCreate()`, `onStart()`, `onResume()`, `onStop()`, `onDestroy()`, `onStartCommand()`, `onBind()`

---

# Assignment 4 — Part 2: Manifest Configuration Implementation (CS412AndroidApp)

Both changes were made in `app/src/main/AndroidManifest.xml` of this app.

## Changes made

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:launchMode="singleInstance"      <!-- added -->
    android:theme="@style/Theme.IntentsApp"
    android:windowSoftInputMode="adjustResize">
    ...
</activity>

<activity
    android:name=".SecondActivity"
    android:exported="true"
    android:label="Second Activity"
    android:screenOrientation="landscape"    <!-- added -->
    android:theme="@style/Theme.IntentsApp">
    ...
</activity>
```

**Note on the attribute name:** the assignment says `android:orientation="landscape"`, but `android:orientation` is not a valid attribute on `<activity>` (it belongs to layouts such as `LinearLayout`). The manifest attribute that locks an activity's orientation is `android:screenOrientation`, so that is what was used. Using the invalid name would be ignored or flagged by lint and would have no effect.

## 1. `android:launchMode="singleInstance"` (MainActivity)

**Purpose:** Controls how an Activity instance is created and placed in the task back stack. With `singleInstance`, the system creates at most one instance of `MainActivity`, and that instance lives alone in its own task. No other activity can be placed in that task. Any activity it starts is launched into a different task, and launching `MainActivity` again reuses the existing instance (delivered through `onNewIntent()`) instead of creating a new one.

**Observable effect on the app:**
- Tapping **Start Activity Explicitly** or **Start Activity Implicitly** opens `SecondActivity` in a *separate task*, since `MainActivity`'s task cannot hold other activities.
- The two screens appear as two separate entries in the Recents/overview screen rather than a single stack.
- Pressing Back on `SecondActivity` finishes its task and returns to `MainActivity`'s task, which keeps its state. The app is not broken: the Start Service, Bind Service and Send Broadcast buttons on `MainActivity` all work as before, because they do not depend on the back stack.
- There can never be duplicate `MainActivity` instances, for example from relaunching via the launcher icon.

## 2. `android:screenOrientation="landscape"` (SecondActivity)

**Purpose:** Forces the activity to always display in landscape orientation, regardless of the device's physical orientation or the user's auto-rotate setting.

**Observable effect on the app:**
- When `SecondActivity` opens, the screen switches to landscape, even if the phone is held upright with auto-rotate on.
- Rotating the device while on `SecondActivity` does not change its orientation; it stays landscape.
- Returning to `MainActivity` (which has no orientation restriction) restores its normal, sensor-driven orientation (portrait if the device is held upright).
- The app is not broken because `SecondActivity` is a simple screen that is usable in landscape.

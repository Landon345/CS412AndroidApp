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

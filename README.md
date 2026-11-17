# ADB Control

ADB Control is an Android diagnostics app for inspecting which debugging capabilities are available on the current device.

## Important security and platform limitation

An ordinary Android app cannot grant itself ADB privileges, enable Android's Wireless debugging setting, or execute privileged `adb` commands merely because USB debugging is enabled. Android requires an authorized ADB host/pairing session, root, or an explicitly granted privileged bridge (for example Shizuku). ADB Control does not bypass these protections and does not pretend that a command ran when it did not.

The first version provides a Russian-language diagnostics screen and a command console that clearly distinguishes local app-shell checks from ADB commands that require an authorized bridge. Device-specific EMUI support must be verified on the target phone.

## Build

Open this project in Android Studio or run the GitHub Actions workflow **Build APK**. The workflow builds a debug APK and uploads it as an artifact.

## Requirements

- Android 8.0+ (API 26+)
- JDK 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21

## Current scope

- Device and Android version diagnostics
- Detection of Android debugging settings exposed to ordinary apps
- Safe local app-process diagnostics
- ADB command help and explicit blocked/ready status (no fake execution)

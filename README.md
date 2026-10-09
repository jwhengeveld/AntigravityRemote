# 🚀 Antigravity Remote Android App

[![Android Build](https://img.shields.io/badge/Android-APK%20v4.0.0-brightgreen.svg)](https://github.com/jwhengeveld/AntigravityRemote/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)](https://kotlinlang.org/)
[![Material 3](https://img.shields.io/badge/UI-Material%203-purple.svg)](https://m3.material.io/)

A native, high-performance Android application designed to manage, monitor, and interact with **Google Antigravity Remote** instances (`https://antigravity.google.com/`). 

Built with Kotlin, Android Jetpack, Material 3, AndroidX WebKit, WorkManager, and Native Google SSO Sign-In.

---

## 📱 Key Features

- **🔑 1-Tap Google SSO Login**: Integrated native Google Play Services Auth (`GoogleSignInClient`) for seamless multi-account Google authentication.
- **⚡ Session & Workspace Switcher**: Switch instantly between multiple remote sessions, workspaces, and subagent tasks.
- **💬 Native Mobile Chat Bar**: 100% native Android prompt input fixed at the bottom with tactile haptic feedback.
- **🎤 Voice Dictation & Media Uploads**: Speak prompts via Android Speech Recognizer and upload images, code files, or documents directly into active sessions.
- **📱 Mobile-First Viewport Injection**: Dynamically cleans up desktop web headers and navigation clutter for a seamless native canvas.
- **📊 Usage & Quota Analytics**: Live Material 3 cards for token context window, daily API request quota, and active model badges (*Gemini 3.6 Flash / Pro*).
- **⚡ Active Subagents & Tasks Monitor**: Real-time sheet listing running subagents (*Codebase Researcher*, *Gradle Builder*) with execution states and cancel controls.
- **🔔 Background Notifications**: Periodic `WorkManager` background monitoring that triggers rich Android notifications when subagent tasks finish.

---

## 📦 Direct APK Download

Download the pre-compiled Android APK directly from this repository:

- 📥 [**AntigravityRemote-v4.0.0-debug.apk**](https://github.com/jwhengeveld/AntigravityRemote/releases/download/v4.0.0/AntigravityRemote-v4.0.0-debug.apk) (Size: ~7.9 MB)

### Quick ADB Installation
```bash
adb install -r AntigravityRemote-v4.0.0-debug.apk
```

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or Java 21
- Android SDK 35
- Gradle 8.10.2

### Build Command
```bash
git clone https://github.com/jwhengeveld/AntigravityRemote.git
cd AntigravityRemote
./gradlew assembleDebug
```
The compiled APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License
Licensed under the Apache License 2.0.

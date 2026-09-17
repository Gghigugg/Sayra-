# SAYRA AI

**Your AI. Your Phone. Your Control.**

Native Android assistant foundation built with Kotlin + Jetpack Compose.

## Core
- User-supplied Gemini API key stored locally with Android Keystore-backed encryption.
- Hindi / English / Hinglish voice interaction.
- `Hey Sayra` wake phrase foundation using Android SpeechRecognizer.
- Foreground microphone service for user-enabled continuous listening.
- Function-style device actions: apps, web, camera, settings, flashlight, volume and brightness.
- Local conversation and memory storage.
- Premium dark liquid-glass inspired orb UI.

Android security restrictions are respected: actions that require OS/user confirmation are launched through the appropriate Android intent or permission flow.

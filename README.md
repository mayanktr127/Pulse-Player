# Pulse Player 🎬

A modern, privacy-respecting Android video player inspired by NewPipe, built from the ground up with **Jetpack Compose**, **Material 3**, and **AndroidX Media3 (ExoPlayer)**.

Pulse Player combines compliance, local privacy, and multi-source streaming in an AMOLED dark interface.

---

## ✨ Features

- **📺 YouTube Playback (ToS Compliant)**:
  - Official YouTube IFrame Player API running inside a hardware-accelerated container.
  - Bidirectional JavaScript bridge syncing play, pause, seek, duration, and buffering state.
  - Full compliance with YouTube Developer Terms of Service (no stream-scraping, DRM bypassing, or unauthorized ripping).
  - Search and discovery powered by **YouTube Data API v3** with support for direct video URL and video ID recognition.

- **⚡ Media3 / ExoPlayer Engine**:
  - Plays authorized HLS (`.m3u8`), DASH (`.mpd`), MP4/WebM progressive streams, and local media.
  - Multi-bitrate track selection (1080p, 720p, 480p, 360p, Auto).
  - Variable playback speeds (0.5x, 0.75x, 1x, 1.25x, 1.5x, 1.75x, 2.0x).
  - Smooth gestures: 10s double-tap seek, timeline scrubber, full-screen orientation lock.

- **🎧 Background Media Session**:
  - AndroidX `MediaSessionService` foreground playback for authorized audio/video.
  - System media notification with lock-screen controls.
  - Headphone unplug handling (`ACTION_AUDIO_BECOMING_NOISY`) & automatic audio focus management.
  - Picture-in-Picture (PiP) mode enabled.

- **💾 Local SQLite Database (Room)**:
  - Watch History with playback position resume.
  - Bookmarks / Favorites.
  - Custom user playlists (add, remove, and manage playlist items).
  - Complete data privacy: 100% on-device SQLite storage, zero trackers, zero telemetry.

- **📁 Local Media Library**:
  - Discovers on-device video and audio files using Android's modern `MediaStore` APIs.
  - Search, filter by category (All, Videos, Audio), and play directly.

- **📥 Authorized Downloads Manager**:
  - Download manager for authorized remote media.
  - Tracks file size, progress, and offline playback.
  - Respects content restrictions: YouTube downloading is explicitly prohibited and disabled per terms.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.0 (Kotlin Coroutines & StateFlow)
- **UI Toolkit**: Jetpack Compose & Material 3
- **Media Engine**: AndroidX Media3 (ExoPlayer 1.4.1, HLS, DASH, UI, Session)
- **Local Persistence**: Room SQLite 2.6.1 + KSP
- **Image Loading & Network**: Coil Compose + Square OkHttp 4.12
- **Architecture**: Clean Architecture (Presentation, Domain, Data, Services) + MVVM

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 35 (compileSdk 35, minSdk 24)
- JDK 17

### Building from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/mayanktr127/Pulse-Player.git
   cd Pulse-Player
   ```
2. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run the unit tests:
   ```bash
   ./gradlew test
   ```

### (Optional) YouTube Data API v3 Configuration
To enable live YouTube search by arbitrary text queries:
1. Obtain an API key from the [Google Cloud Console](https://console.cloud.google.com/) with **YouTube Data API v3** enabled.
2. Either:
   - Paste the key in the app under **Search** or **Settings**, OR
   - Add it to `local.properties`:
     ```properties
     youtube.api.key=YOUR_API_KEY_HERE
     ```
*(Note: Direct video URLs and video IDs like `dQw4w9WgXcQ` work immediately without an API key).*

---

## 📜 License
This project is an independent implementation created for educational and authorized media playback purposes.

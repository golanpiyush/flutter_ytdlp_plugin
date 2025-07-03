# 🎬 flutter_ytdlp_plugin

A powerful and lightweight Flutter plugin for fetching YouTube video/audio streams, metadata, and trending/search data using [`yt-dlp`](https://github.com/yt-dlp/yt-dlp). This plugin is designed for developers who want full control over YouTube content inside Flutter apps without relying on the YouTube API.

> 🔧 **Built on top of [Chopqy](https://github.com/golanpiyush/chopqy)** – a Python-powered engine for communicating with `yt-dlp` safely and efficiently via Chaquopy.

---

## 📦 Installation

### 1. Add Dependency

In your `pubspec.yaml`:

```yaml
dependencies:
  flutter_ytdlp_plugin: ^1.0.0
```

Then run:

```bash
flutter pub get
```

### 2. Android Setup (Chaquopy)

In `android/app/build.gradle`:

```gradle
plugins {
    id 'com.android.application'
    id 'com.chaquo.python'  // 👈 Required for Python execution
}

android {
    ...
    defaultConfig {
        ...
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a"
        }
    }
}

chaquopy {
    python {
        version "3.11"
        pip {
            install "yt-dlp"
        }
    }
}
```

> ✅ You can also bundle a compiled `yt-dlp` binary under `assets/python/` for offline use.

---

## 🚀 Features

- 🔗 Fetch all streaming links (video/audio – all available qualities & formats)
- 🎯 Extract best-quality video/audio URLs
- 🔍 Search YouTube videos (fast and accurate)
- 🧠 Get random videos (can be filtered by keyword/category)
- 📈 Fetch trending videos in real-time
- 📚 Retrieve full metadata (title, duration, thumbnail, views, etc.)
- ⚙️ Customizable backend powered by `Chopqy` & `yt-dlp`
- 🐍 Seamless Python (Chaquopy) integration with full Dart APIs
- ✅ No need for YouTube API key or OAuth
- 💡 Built for privacy-conscious, knowledge-seeking apps

---

## 📘 API Reference

### `getStreamingLinks(String url)`
Returns all available video & audio stream links (with format, resolution, codec, etc.)

```dart
final links = await FlutterYTDLP.getStreamingLinks("https://youtu.be/...");
```

### `getTrendingVideos()`
Fetch trending YouTube videos from `yt-dlp`’s trending extractor.

```dart
final trending = await FlutterYTDLP.getTrendingVideos();
```

### `getRandomVideo({String? keyword})`
Get a random YouTube video. You can filter by keyword or niche.

```dart
final random = await FlutterYTDLP.getRandomVideo(keyword: "science");
```

### `searchYTDLP(String query)`
Search for YouTube videos using the provided query.

```dart
final results = await FlutterYTDLP.searchYTDLP("AI documentary");
```

### `getRelatedVideos(String videoId)`
Get related/recommended videos based on a given YouTube video ID.

```dart
final related = await FlutterYTDLP.getRelatedVideos("dQw4w9WgXcQ");
```

---

## 💡 Use Case Ideas

- Build your own **YouTube-like clone**
- Create an **offline video player**
- Design **smart music players** using audio-only links
- Use it in **torrent/video streaming hybrid apps** for metadata or fallback streams
- Educational/research apps that explore **video trends by category**

---

## 🔐 Disclaimer

This project is intended for **educational and research purposes only**. It is not affiliated with or endorsed by YouTube or Google.

- Please respect YouTube’s [Terms of Service](https://www.youtube.com/t/terms).
- Do **not** use this plugin for scraping copyrighted content or violating content restrictions.
- **You are solely responsible** for how you use this plugin.

---

## 🛠 Contributing

We welcome PRs and new features! Here's how to contribute:

1. Fork the repository
2. Clone and set up your environment (`flutter pub get`, `Chaquopy`, etc.)
3. Add your feature or fix
4. Submit a PR with a clear description

---

## 🙌 Acknowledgments

- [`yt-dlp`](https://github.com/yt-dlp/yt-dlp) – the heart of the plugin
- [`Chopqy`](https://github.com/golanpiyush/chopqy) – backend bridge
- [`Chaquopy`](https://chaquo.com/chaquopy/) – Python in Android made easy

---

## 📜 License

MIT License © 2025 [Piyush Golan](https://github.com/golanpiyush)

---

> _“I stand by those who build tools that empower, not distract. This plugin is for seekers of knowledge, not consumers of noise.”_  
> — ✨ Piyush Golan
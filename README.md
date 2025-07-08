# flutter_ytdlp_plugin 🎥

A powerful Flutter plugin that leverages [`yt-dlp`](https://github.com/yt-dlp/yt-dlp) to fetch streaming URLs, video/audio formats, related videos, trending videos, and more — directly from YouTube.

Ideal for building custom YouTube players, media browsers, or any app that requires dynamic YouTube data without relying on official APIs.

---

## ✨ Features

- 🔗 Fetch **direct streaming URLs** for videos and audio (MP3-like formats)
- 🎶 Extract **audio-only formats** or best video+audio combinations
- 🔍 **Search** YouTube videos (with optional limit)
- 🔀 Fetch **random videos** for discovery/exploration
- 📈 Get **trending video lists** from YouTube
- 🔁 Fetch **related videos** based on a title
- 📦 Retrieve **detailed format information** for a given video
- 💡 Optimized for performance and cross-platform compatibility

---

## 🚀 Getting Started

Add this plugin to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_ytdlp_plugin:
    git:
      url: https://github.com/golanpiyush/flutter_ytdlp_plugin.git

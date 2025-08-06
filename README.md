# Flutter YTDLP Plugin

[![pub package](https://img.shields.io/pub/v/flutter_ytdlp_plugin.svg)](https://pub.dev/packages/flutter_ytdlp_plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Flutter plugin that provides **YouTube stream extraction** capabilities using [yt-dlp](https://github.com/yt-dlp/yt-dlp).  
This plugin uses [Chaquopy](https://chaquo.com/chaquopy/) to execute Python code on Android devices for advanced video/audio extraction.

---

## ✨ Features

- ✅ Check video **availability status**
- 🎥 Extract **video streams** with quality preferences
- 🔊 Extract **audio streams** with bitrate preferences
- 🔄 Get **unified streams** (video + audio) with codec options
- ⚡ **Concurrent processing** for performance
- 🐞 **Automatic debug mode** detection
- 🛡️ **Robust error handling**

---

## 📱 Platform Support

| Platform | Support        |
|----------|----------------|
| Android  | ✅ Supported   |
| iOS      | ❌ Not supported |
| Web      | ❌ Not supported |
| Desktop  | ❌ Not supported |

---

## 📦 Installation

Add this to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_ytdlp_plugin:
    git:
      url: https://github.com/your-repo/flutter_ytdlp_plugin.git
      ref: main
```

---

## ⚙️ Android Setup

### 1. Add Chaquopy to `android/app/build.gradle`

```groovy
android {
    ...
    defaultConfig {
        ...
        python {
            version "3.8"
        }
        ndk {
            abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
        }
    }
}
```

### 2. Create `requirements.txt` in `android/app/`

```
yt-dlp>=2023.11.16
```

---

## 🚀 Usage

### Import the plugin

```dart
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin.dart';
```

### Initialize

```dart
final ytdlp = FlutterYtdlpPlugin();
```

---

## 📘 API Methods

### 1. Check Video Status

```dart
final status = await ytdlp.checkStatus(videoId: 'dQw4w9WgXcQ');
print(status);
```

**Response:**

```json
{
  "available": true,
  "status": "available",
  "error": null
}
```

- `available`: `bool`
- `status`: `String` (`'available'`, `'private'`, `'age_restricted'`, etc.)
- `error`: `String?` (nullable)

---

### 2. Get Video Streams

```dart
final streams = await ytdlp.getVideoStreams(
  videoId: 'dQw4w9WgXcQ',
  quality: '1080p', // Default: 1080p
);
print(streams);
```

**Returns:** `List<Map<String, dynamic>>`

```json
[
  {
    "url": "...",
    "ext": "mp4",
    "resolution": "1920x1080",
    "height": 1080,
    "width": 1920,
    "bitrate": 2500.0,
    "codec": "avc1.640028",
    "filesize": 12345678,
    "formatNote": "1080p",
    "formatId": "137"
  }
]
```

---

### 3. Get Audio Streams

```dart
final streams = await ytdlp.getAudioStreams(
  videoId: 'dQw4w9WgXcQ',
  bitrate: 192, // Default: 192 kbps
  codec: 'opus', // Optional
);
print(streams);
```

**Returns:** `List<Map<String, dynamic>>`

```json
[
  {
    "url": "...",
    "ext": "webm",
    "bitrate": 192,
    "codec": "opus",
    "filesize": 4321000,
    "formatId": "251"
  }
]
```

---

### 4. Get Unified Streams (Video + Audio)

```dart
final result = await ytdlp.getUnifiedStreams(
  videoId: 'dQw4w9WgXcQ',
  audioBitrate: 192,
  videoQuality: '1080p',
  audioCodec: 'opus', // Optional
  videoCodec: 'avc1', // Optional
  includeVideo: true,  // Default: true
  includeAudio: true,  // Default: true
);
print(result);
```

**Response:**

```json
{
  "duration": 213,
  "video": [ ... ],
  "audio": [ ... ]
}
```

- `duration`: `int` (seconds)
- `video`: `List<Map<String, dynamic>>?` (optional)
- `audio`: `List<Map<String, dynamic>>?` (optional)

---

## ❗ Error Handling

Exceptions are thrown as `PlatformException` with the following error codes:

| Error Code        | Description                     |
|-------------------|---------------------------------|
| `INVALID_ARGUMENT` | Missing required parameters     |
| `PYTHON_ERROR`     | Python execution failed         |
| `EXCEPTION`        | Unexpected error occurred       |

---

## 🧪 Example

```dart
try {
  final status = await ytdlp.checkStatus(videoId: videoUrl);
  if (status['available'] == true) {
    final streams = await ytdlp.getUnifiedStreams(
      videoId: videoUrl,
      videoQuality: '720p',
      audioBitrate: 128,
    );
    // Use streams...
  }
} on PlatformException catch (e) {
  print('Error: ${e.message}');
}
```

---

## ⚠️ Limitations

- **Android only**: iOS is not supported due to Python runtime restrictions.
- **Larger APK size**: Adds ~25MB because of embedded Python.
- **No downloading**: Only extracts stream information; does **not** download media.

---

## 🤝 Contributing

Pull requests are welcome!  
For major changes, please open an issue first to discuss what you would like to change.

---

## 🎉 Happy Streaming!

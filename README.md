Flutter YTDLP Plugin
[![pub package](https://img.shields.io/pub/v/flutter_ytdlp_plugin.svg: MIT](https://img.shields.io/badge/License-MIT-yellow.svg plugin that provides YouTube stream extraction capabilities using yt-dlp. This plugin uses Chaquopy to execute Python code on Android devices for advanced video/audio extraction.

Features
✅ Check video availability status

🎥 Extract video streams with quality preferences

🔊 Extract audio streams with bitrate preferences

🔄 Get unified streams (video + audio) with codec options

⚡ Concurrent processing for performance

🐞 Automatic debug mode detection

🛡️ Robust error handling

Platform Support
Platform	Support
Android	✅ Supported
iOS	❌ Not supported
Web	❌ Not supported
Desktop	❌ Not supported
Installation
Add to your pubspec.yaml:

text
dependencies:
  flutter_ytdlp_plugin:
    git:
      url: https://github.com/your-repo/flutter_ytdlp_plugin.git
      ref: main
Android Setup
1. Add Chaquopy to your app's build.gradle (android/app/build.gradle):

text
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
2. Create requirements.txt in your Android project (android/app/):

text
yt-dlp>=2023.11.16
Usage
Import the package
dart
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin.dart';
Initialize
dart
final ytdlp = FlutterYtdlpPlugin();
API Methods
1. Check Video Status
dart
final status = await ytdlp.checkStatus(videoId: 'dQw4w9WgXcQ');
print(status);
// {
//   'available': true,
//   'status': 'available',
//   'error': null
// }
Response:

available: bool

status: String ('available', 'private', 'age_restricted', etc.)

error: String? (nullable)

2. Get Video Streams
dart
final streams = await ytdlp.getVideoStreams(
  videoId: 'dQw4w9WgXcQ',
  quality: '1080p', // Default: '1080p'
);
print(streams);
// [
//   {
//     'url': '...',
//     'ext': 'mp4',
//     'resolution': '1920x1080',
//     'height': 1080,
//     'width': 1920,
//     'bitrate': 2500.0,
//     'codec': 'avc1.640028',
//     'filesize': 12345678,
//     'formatNote': '1080p',
//     'formatId': '137'
//   }
// ]
Returns: List<Map<String, dynamic>> (best matching stream)

3. Get Audio Streams
dart
final streams = await ytdlp.getAudioStreams(
  videoId: 'dQw4w9WgXcQ',
  bitrate: 192, // Default: 192 (kbps)
  codec: 'opus', // Optional: filter by codec
);
print(streams);
// [
//   {
//     'url': '...',
//     'ext': 'webm',
//     'bitrate': 192,
//     'codec': 'opus',
//     'filesize': 4321000,
//     'formatId': '251'
//   }
// ]
Returns: List<Map<String, dynamic>> (best matching stream)

4. Get Unified Streams (Video + Audio)
dart
final result = await ytdlp.getUnifiedStreams(
  videoId: 'dQw4w9WgXcQ',
  audioBitrate: 192,
  videoQuality: '1080p',
  audioCodec: 'opus', // Optional
  videoCodec: 'avc1', // Optional
  includeVideo: true, // Default: true
  includeAudio: true, // Default: true
);
print(result);
// {
//   'duration': 213,
//   'video': [ ... ],
//   'audio': [ ... ]
// }
Response:

duration: int (video duration in seconds)

video: List<Map<String, dynamic>>? (if includeVideo=true)

audio: List<Map<String, dynamic>>? (if includeAudio=true)

Error Handling
The plugin throws platform exceptions with these error codes:

Error Code	Description
INVALID_ARGUMENT	Missing required parameters
PYTHON_ERROR	Python execution failed
EXCEPTION	Unexpected error occurred
Example
dart
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
Limitations
Android Only: iOS is not supported due to Python runtime limitations.

Large APK Size: Adds ~25MB due to embedded Python.

No Download: Only extracts stream information; does not download videos.

Contributing
Pull requests are welcome! For major changes, please open an issue first and discuss the desired changes.

Happy streaming!

in markdown format
in markdown format
text
# Flutter YTDLP Plugin

A Flutter plugin that provides **YouTube stream extraction** capabilities using [yt-dlp](https://github.com/yt-dlp/yt-dlp). This plugin uses [Chaquopy](https://chaquo.com/chaquopy/) to execute Python code on Android devices for advanced video/audio extraction.

## Features

- ✅ Check video **availability status**
- 🎥 Extract **video streams** with quality preferences
- 🔊 Extract **audio streams** with bitrate preferences
- 🔄 Get **unified streams** (video + audio) with codec options
- ⚡ **Concurrent processing** for performance
- 🐞 **Automatic debug mode** detection
- 🛡️ **Robust error handling**

## Platform Support

| Platform | Support            |
|----------|--------------------|
| Android  | ✅ Supported       |
| iOS      | ❌ Not supported   |
| Web      | ❌ Not supported   |
| Desktop  | ❌ Not supported   |

## Installation

Add to your `pubspec.yaml`:

dependencies:
flutter_ytdlp_plugin:
git:
url: https://github.com/your-repo/flutter_ytdlp_plugin.git
ref: main

text

### Android Setup

**1. Add Chaquopy to your app's `build.gradle` (android/app/build.gradle):**

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

text

**2. Create `requirements.txt` in your Android project (android/app/):**

yt-dlp>=2023.11.16

text

## Usage

### Import the package

import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin.dart';

text

### Initialize

final ytdlp = FlutterYtdlpPlugin();

text

## API Methods

### 1. Check Video Status

final status = await ytdlp.checkStatus(videoId: 'dQw4w9WgXcQ');
print(status);
// {
// 'available': true,
// 'status': 'available',
// 'error': null
// }

text
- **Response:**
  - `available`: bool
  - `status`: String (`'available'`, `'private'`, `'age_restricted'`, etc.)
  - `error`: String? (nullable)

### 2. Get Video Streams

final streams = await ytdlp.getVideoStreams(
videoId: 'dQw4w9WgXcQ',
quality: '1080p', // Default: '1080p'
);
print(streams);
// [
// {
// 'url': '...',
// 'ext': 'mp4',
// 'resolution': '1920x1080',
// 'height': 1080,
// 'width': 1920,
// 'bitrate': 2500.0,
// 'codec': 'avc1.640028',
// 'filesize': 12345678,
// 'formatNote': '1080p',
// 'formatId': '137'
// }
// ]

text
- **Returns:** `List<Map<String, dynamic>>` (best matching stream)

### 3. Get Audio Streams

final streams = await ytdlp.getAudioStreams(
videoId: 'dQw4w9WgXcQ',
bitrate: 192, // Default: 192 (kbps)
codec: 'opus', // Optional: filter by codec
);
print(streams);
// [
// {
// 'url': '...',
// 'ext': 'webm',
// 'bitrate': 192,
// 'codec': 'opus',
// 'filesize': 4321000,
// 'formatId': '251'
// }
// ]

text
- **Returns:** `List<Map<String, dynamic>>` (best matching stream)

### 4. Get Unified Streams (Video + Audio)

final result = await ytdlp.getUnifiedStreams(
videoId: 'dQw4w9WgXcQ',
audioBitrate: 192,
videoQuality: '1080p',
audioCodec: 'opus', // Optional
videoCodec: 'avc1', // Optional
includeVideo: true, // Default: true
includeAudio: true, // Default: true
);
print(result);
// {
// 'duration': 213,
// 'video': [ ... ],
// 'audio': [ ... ]
// }

text
- **Response:**
  - `duration`: int (video duration in seconds)
  - `video`: List<Map<String, dynamic>>? (if includeVideo=true)
  - `audio`: List<Map<String, dynamic>>? (if includeAudio=true)

## Error Handling

The plugin throws platform exceptions with these error codes:

| Error Code         | Description                    |
|--------------------|-------------------------------|
| INVALID_ARGUMENT   | Missing required parameters    |
| PYTHON_ERROR       | Python execution failed        |
| EXCEPTION          | Unexpected error occurred      |

## Example

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

text

## Limitations

- **Android Only:** iOS is not supported due to Python runtime limitations.
- **Large APK Size:** Adds ~25MB due to embedded Python.
- **No Download:** Only extracts stream information; does not download videos.

## Contributing

Pull requests are welcome! For major changes, please open an issue first and discuss your proposal.

**Happy streaming!**
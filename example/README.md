# flutter_ytdlp_plugin_example

Demonstrates how to use the flutter_ytdlp_plugin plugin.

## 📘 Plugin's API Methods

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
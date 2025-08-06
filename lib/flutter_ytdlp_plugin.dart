import 'package:flutter/services.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin_platform_interface.dart';

/// The main plugin class that provides YouTube stream extraction capabilities
class FlutterYtdlpPlugin {
  /// The platform interface instance
  static final FlutterYtdlpPluginPlatform _platform =
      FlutterYtdlpPluginPlatform.instance;

  /// Checks the availability status of a YouTube video
  ///
  /// Returns a map containing:
  /// - 'available': bool indicating if video is accessible
  /// - 'status': String describing the status (available, private, unavailable, etc.)
  /// - 'error': String? error message if any
  static Future<Map<String, dynamic>> checkStatus(String videoId) async {
    try {
      return await _platform.checkStatus(videoId);
    } on PlatformException catch (e) {
      throw _convertPlatformException(e);
    }
  }

  /// Gets video streams for a YouTube video
  ///
  /// Parameters:
  /// - videoId: YouTube video ID or URL
  /// - quality: Preferred video quality (e.g., '1080p', '720p')
  ///
  /// Returns a list of stream info maps (usually contains one best match)
  static Future<List<Map<String, dynamic>>> getVideoStreams(
    String videoId, {
    String quality = '1080p',
  }) async {
    try {
      return await _platform.getVideoStreams(videoId, quality: quality);
    } on PlatformException catch (e) {
      throw _convertPlatformException(e);
    }
  }

  /// Gets audio streams for a YouTube video
  ///
  /// Parameters:
  /// - videoId: YouTube video ID or URL
  /// - bitrate: Preferred audio bitrate in kbps (default: 192)
  /// - codec: Preferred audio codec (e.g., 'opus', 'mp4a.40.2', 'aac')
  ///
  /// Returns a list of stream info maps (usually contains one best match)
  static Future<List<Map<String, dynamic>>> getAudioStreams(
    String videoId, {
    int bitrate = 320,
    String? codec,
  }) async {
    try {
      return await _platform.getAudioStreams(
        videoId,
        bitrate: bitrate,
        codec: codec,
      );
    } on PlatformException catch (e) {
      throw _convertPlatformException(e);
    }
  }

  /// Gets muxed streams (video + audio) for a YouTube video
  ///
  /// Parameters:
  /// - videoId: YouTube video ID or URL
  /// - audioBitrate: Preferred audio bitrate in kbps (default: 192)
  /// - videoQuality: Preferred video quality (default: '1080p')
  /// - audioCodec: Preferred audio codec (e.g., 'opus', 'mp4a.40.2')
  /// - videoCodec: Preferred video codec (e.g., 'vp9', 'avc1')
  /// - includeVideo: Whether to include video streams (default: true)
  /// - includeAudio: Whether to include audio streams (default: true)
  ///
  /// Returns a map containing:
  /// - 'duration': int video duration in seconds
  /// - 'video': List<Map>? video streams if includeVideo=true
  /// - 'audio': List<Map>? audio streams if includeAudio=true
  static Future<Map<String, dynamic>> getMuxedStreams(
    String videoId, {
    int audioBitrate = 320,
    String videoQuality = '1080p',
    String? audioCodec,
    String? videoCodec,
    bool includeVideo = true,
    bool includeAudio = true,
  }) async {
    try {
      return await _platform.getMuxededStreams(
        videoId,
        audioBitrate: audioBitrate,
        videoQuality: videoQuality,
        audioCodec: audioCodec,
        videoCodec: videoCodec,
        includeVideo: includeVideo,
        includeAudio: includeAudio,
      );
    } on PlatformException catch (e) {
      throw _convertPlatformException(e);
    }
  }

  /// Converts PlatformException to more specific Dart exceptions
  static Object _convertPlatformException(PlatformException e) {
    switch (e.code) {
      case 'INVALID_ARGUMENT':
        return ArgumentError(e.message);
      case 'PYTHON_ERROR':
        return Exception('Python error: ${e.message}');
      case 'EXCEPTION':
        return Exception(e.message ?? 'Unknown error');
      default:
        return Exception('Platform error: ${e.message}');
    }
  }
}

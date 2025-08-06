import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin_platform_interface.dart';

/// The method channel implementation of [FlutterYtdlpPluginPlatform].
class MethodChannelFlutterYtdlpPlugin extends FlutterYtdlpPluginPlatform {
  /// The method channel used to interact with the native platform.
  final methodChannel = const MethodChannel('flutter_ytdlp_plugin');

  /// Checks the availability status of a YouTube video
  @override
  Future<Map<String, dynamic>> checkStatus(String videoId) async {
    try {
      final result = await methodChannel.invokeMethod<Map<dynamic, dynamic>>(
        'checkStatus',
        {'videoId': videoId},
      );
      return _convertMap(result);
    } on PlatformException catch (e) {
      throw PlatformException(
        code: e.code,
        message: e.message,
        details: e.details,
      );
    }
  }

  /// Gets video streams for a YouTube video
  @override
  Future<List<Map<String, dynamic>>> getVideoStreams(
    String videoId, {
    String quality = '1080p',
  }) async {
    try {
      final result = await methodChannel.invokeMethod<List<dynamic>>(
        'getVideoStreams',
        {'videoId': videoId, 'quality': quality},
      );
      return _convertList(result);
    } on PlatformException catch (e) {
      throw PlatformException(
        code: e.code,
        message: e.message,
        details: e.details,
      );
    }
  }

  /// Gets audio streams for a YouTube video
  @override
  Future<List<Map<String, dynamic>>> getAudioStreams(
    String videoId, {
    int bitrate = 192,
    String? codec,
  }) async {
    try {
      final result = await methodChannel.invokeMethod<List<dynamic>>(
        'getAudioStreams',
        {
          'videoId': videoId,
          'bitrate': bitrate,
          if (codec != null) 'codec': codec,
        },
      );
      return _convertList(result);
    } on PlatformException catch (e) {
      throw PlatformException(
        code: e.code,
        message: e.message,
        details: e.details,
      );
    }
  }

  /// Gets unified streams (video + audio) for a YouTube video
  @override
  Future<Map<String, dynamic>> getMuxededStreams(
    String videoId, {
    int audioBitrate = 192,
    String videoQuality = '1080p',
    String? audioCodec,
    String? videoCodec,
    bool includeVideo = true,
    bool includeAudio = true,
  }) async {
    try {
      final result = await methodChannel
          .invokeMethod<Map<dynamic, dynamic>>('getUnifiedStreams', {
            'videoId': videoId,
            'audioBitrate': audioBitrate,
            'videoQuality': videoQuality,
            if (audioCodec != null) 'audioCodec': audioCodec,
            if (videoCodec != null) 'videoCodec': videoCodec,
            'includeVideo': includeVideo,
            'includeAudio': includeAudio,
          });
      return _convertUnifiedResult(result);
    } on PlatformException catch (e) {
      throw PlatformException(
        code: e.code,
        message: e.message,
        details: e.details,
      );
    }
  }

  /// Converts the platform Map<dynamic, dynamic> to Map<String, dynamic>
  Map<String, dynamic> _convertMap(Map<dynamic, dynamic>? original) {
    if (original == null) return {};
    return Map<String, dynamic>.from(original);
  }

  /// Converts the platform List<dynamic> to List<Map<String, dynamic>>
  List<Map<String, dynamic>> _convertList(List<dynamic>? original) {
    if (original == null) return [];
    return original
        .map((item) => Map<String, dynamic>.from(item as Map<dynamic, dynamic>))
        .toList();
  }

  /// Converts the unified streams result from platform to Dart types
  Map<String, dynamic> _convertUnifiedResult(Map<dynamic, dynamic>? original) {
    if (original == null) return {};

    final result = Map<String, dynamic>.from(original);

    // Convert video streams if present
    if (result.containsKey('video')) {
      result['video'] = _convertList(result['video'] as List<dynamic>?);
    }

    // Convert audio streams if present
    if (result.containsKey('audio')) {
      result['audio'] = _convertList(result['audio'] as List<dynamic>?);
    }

    return result;
  }
}

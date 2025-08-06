import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

/// The interface that implementations of flutter_ytdlp_plugin must implement.
abstract class FlutterYtdlpPluginPlatform extends PlatformInterface {
  /// Constructs a FlutterYtdlpPluginPlatform.
  FlutterYtdlpPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterYtdlpPluginPlatform _instance =
      MethodChannelFlutterYtdlpPlugin();

  /// The default instance of [FlutterYtdlpPluginPlatform] to use.
  static FlutterYtdlpPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterYtdlpPluginPlatform] when
  /// they register themselves.
  static set instance(FlutterYtdlpPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// Checks the availability status of a YouTube video
  Future<Map<String, dynamic>> checkStatus(String videoId) {
    throw UnimplementedError('checkStatus() has not been implemented.');
  }

  /// Gets video streams for a YouTube video
  Future<List<Map<String, dynamic>>> getVideoStreams(
    String videoId, {
    String quality = '1080p',
  }) {
    throw UnimplementedError('getVideoStreams() has not been implemented.');
  }

  /// Gets audio streams for a YouTube video
  Future<List<Map<String, dynamic>>> getAudioStreams(
    String videoId, {
    int bitrate = 320,
    String? codec,
  }) {
    throw UnimplementedError('getAudioStreams() has not been implemented.');
  }

  /// Gets unified streams (video + audio) for a YouTube video
  Future<Map<String, dynamic>> getMuxededStreams(
    String videoId, {
    int audioBitrate = 320,
    String videoQuality = '1080p',
    String? audioCodec,
    String? videoCodec,
    bool includeVideo = true,
    bool includeAudio = true,
  }) {
    throw UnimplementedError('getUnifiedStreams() has not been implemented.');
  }
}

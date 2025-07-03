// flutter_ytdlp_plugin.dart
import 'package:flutter/services.dart';

class FlutterYtdlpPlugin {
  static const MethodChannel _channel = MethodChannel('flutter_ytdlp_plugin');

  static Future<Map<String, dynamic>> getStreamingLinks({
    required String title,
    String channelName = "",
  }) async {
    try {
      final result = await _channel.invokeMethod('getStreamingLinks', {
        'title': title,
        'channelName': channelName,
      });
      return Map<String, dynamic>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to get streaming links: ${e.message}");
    }
  }

  static Future<List<Map<String, dynamic>>> getRelatedVideos({
    required String title,
    String channelName = "",
    int count = 200,
  }) async {
    try {
      final result = await _channel.invokeMethod('getRelatedVideos', {
        'title': title,
        'channelName': channelName,
        'count': count,
      });
      return List<Map<String, dynamic>>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to get related videos: ${e.message}");
    }
  }

  static Future<List<Map<String, dynamic>>> getRandomVideos({
    String? category,
    int count = 50,
  }) async {
    try {
      final result = await _channel.invokeMethod('getRandomVideos', {
        'category': category,
        'count': count,
      });
      return List<Map<String, dynamic>>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to get random videos: ${e.message}");
    }
  }

  static Future<List<Map<String, dynamic>>> getTrendingVideos({
    int count = 50,
  }) async {
    try {
      final result = await _channel.invokeMethod('getTrendingVideos', {
        'count': count,
      });
      return List<Map<String, dynamic>>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to get trending videos: ${e.message}");
    }
  }

  static Future<List<Map<String, dynamic>>> searchVideos({
    required String query,
    int maxResults = 25,
  }) async {
    try {
      final result = await _channel.invokeMethod('searchVideos', {
        'query': query,
        'maxResults': maxResults,
      });
      return List<Map<String, dynamic>>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to search videos: ${e.message}");
    }
  }

  static Future<Map<String, dynamic>> getVideoDetailsWithFormats({
    required String videoId,
  }) async {
    try {
      final result = await _channel.invokeMethod('getVideoDetailsWithFormats', {
        'videoId': videoId,
      });
      return Map<String, dynamic>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to get video details: ${e.message}");
    }
  }

  static Future<Map<String, dynamic>> getBestFormatUrls({
    required String videoId,
  }) async {
    try {
      final result = await _channel.invokeMethod('getBestFormatUrls', {
        'videoId': videoId,
      });
      return Map<String, dynamic>.from(result);
    } on PlatformException catch (e) {
      throw Exception("Failed to get best format URLs: ${e.message}");
    }
  }
}

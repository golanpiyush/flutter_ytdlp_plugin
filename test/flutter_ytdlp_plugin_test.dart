import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_ytdlp_plugin');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'initialize':
          return true;
        case 'isInitialized':
          return true;
        case 'getStreamingLinks':
          return <String, dynamic>{'url': 'https://example.com/audio.mp3'};
        case 'getRelatedVideos':
          return [
            <String, dynamic>{'title': 'Related 1'},
            <String, dynamic>{'title': 'Related 2'},
          ];
        case 'getTrendingVideos':
          return [
            <String, dynamic>{'videoId': 'tr1', 'title': 'Trending Video'},
          ];
        case 'getRandomVideos':
          return [
            <String, dynamic>{'videoId': 'rnd1', 'title': 'Random Video'},
          ];
        case 'searchVideos':
          return [
            <String, dynamic>{'videoId': 'abc123', 'title': 'Search Result'},
          ];
        case 'getVideoDetailsWithFormats':
          return <String, dynamic>{
            'videoId': 'abc123',
            'formats': ['mp4', 'webm'],
          };
        case 'getBestFormatUrls':
          return <String, dynamic>{
            'videoId': 'abc123',
            'formats': ['webm'],
          };
        default:
          return null;
      }
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('initialize() and isInitialized() should succeed', () async {
    final plugin = FlutterYtdlpPlugin();
    await plugin.initialize();
    final initialized = await plugin.isInitialized();
    expect(initialized, isTrue);
  });

  test('getStreamingLinks() returns mock data', () async {
    final result = await FlutterYtdlpPlugin.getStreamingLinks(title: 'Test');
    expect(result, isA<Map<String, dynamic>>());
    expect(result['url'], contains('example.com'));
  });

  test('getRelatedVideos() returns mock list', () async {
    final result = await FlutterYtdlpPlugin.getRelatedVideos(title: 'Test');
    expect(result, isA<List<Map<String, dynamic>>>());
    expect(result.length, 2);
    expect(result[0]['title'], equals('Related 1'));
  });

  test('getTrendingVideos() returns mock trending list', () async {
    final result = await FlutterYtdlpPlugin.getTrendingVideos();
    expect(result, isA<List<Map<String, dynamic>>>());
    expect(result[0]['videoId'], equals('tr1'));
  });

  test('getRandomVideos() returns mock random list', () async {
    final result = await FlutterYtdlpPlugin.getRandomVideos();
    expect(result, isA<List<Map<String, dynamic>>>());
    expect(result[0]['videoId'], equals('rnd1'));
  });

  test('searchVideos() returns sample search result', () async {
    final result = await FlutterYtdlpPlugin.searchVideos(query: 'Test');
    expect(result, isA<List<Map<String, dynamic>>>());
    expect(result[0]['videoId'], equals('abc123'));
  });

  test('getVideoDetailsWithFormats() returns format list', () async {
    final result = await FlutterYtdlpPlugin.getVideoDetailsWithFormats(
      videoId: 'abc123',
    );
    expect(result, isA<Map<String, dynamic>>());
    expect(result['formats'], contains('mp4'));
  });

  test('getBestFormatUrls() returns best formats', () async {
    final result = await FlutterYtdlpPlugin.getBestFormatUrls(
      videoId: 'abc123',
    );
    expect(result, isA<Map<String, dynamic>>());
    expect(result['formats'], contains('webm'));
  });
}

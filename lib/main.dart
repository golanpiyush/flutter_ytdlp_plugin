import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(title: 'YouTube Plugin Test', home: TestScreen());
  }
}

class TestScreen extends StatefulWidget {
  @override
  _TestScreenState createState() => _TestScreenState();
}

class _TestScreenState extends State<TestScreen> {
  static const platform = MethodChannel('flutter_ytdlp_plugin');
  bool _isRunning = false;

  @override
  void initState() {
    super.initState();
    // Auto-run the test when the app starts
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _runTest();
    });
  }

  Future<void> _runTest() async {
    if (_isRunning) return;

    setState(() {
      _isRunning = true;
    });

    print('\n' + '=' * 60);
    print('STARTING YOUTUBE PLUGIN TEST');
    print('=' * 60);
    print('Testing with query: "edsheeran - perfect"');
    print('Requesting 4 results for each function');
    print('=' * 60 + '\n');

    try {
      final testResults = await platform.invokeMethod('testAllFunctions');
      _printTestResults(testResults);
    } catch (e) {
      print('❌ TEST EXECUTION FAILED: $e');
    } finally {
      setState(() {
        _isRunning = false;
      });

      // Exit the app after test completion
      print('\n' + '=' * 60);
      print('TEST COMPLETED - EXITING APPLICATION');
      print('=' * 60);

      // Wait a bit before exiting so user can see results
      await Future.delayed(Duration(seconds: 2));
      exit(0);
    }
  }

  void _printTestResults(Map<dynamic, dynamic> results) {
    // Print test summary first
    if (results.containsKey('test_summary')) {
      final summary = results['test_summary'] as Map<dynamic, dynamic>;
      print('📊 TEST SUMMARY');
      print('-' * 30);
      print('Total Tests: ${summary['total_tests']}');
      print('Successful: ${summary['successful_tests']}');
      print('Failed: ${summary['failed_tests']}');
      print('Success Rate: ${summary['success_rate']}');
      print('Test Query: "${summary['test_query']}"');
      print('Test Title: "${summary['test_title']}"');
      print('Test Channel: "${summary['test_channel']}"');
      print('Requested Count: ${summary['requested_count']}');
      print('-' * 30 + '\n');
    }

    // Print individual test results
    final testOrder = [
      '1_initialize',
      '2_isInitialized',
      '3_getStreamingLinks',
      '4_getRelatedVideos',
      '5_getRandomVideos',
      '6_getTrendingVideos',
      '7_searchVideos',
      '8_getVideoDetailsWithFormats',
      '9_getBestFormatUrls',
    ];

    for (String testKey in testOrder) {
      if (results.containsKey(testKey)) {
        _printTestResult(testKey, results[testKey] as Map<dynamic, dynamic>);
      }
    }
  }

  void _printTestResult(String testName, Map<dynamic, dynamic> result) {
    final status = result['status'] as String;
    final isSuccess = status == 'success';
    final emoji = isSuccess ? '✅' : '❌';

    print('$emoji ${_formatTestName(testName)}');
    print('   Status: ${status.toUpperCase()}');

    if (isSuccess) {
      if (result.containsKey('data')) {
        final data = result['data'];
        if (data is List) {
          print('   Results: ${data.length} items');
          if (data.isNotEmpty) {
            print('   Sample: ${_formatSampleData(data.first)}');
          }
        } else if (data is Map) {
          print('   Data: ${_formatSampleData(data)}');
        }
      }

      if (result.containsKey('count')) {
        print('   Count: ${result['count']}');
      }

      if (result.containsKey('initialized')) {
        print('   Initialized: ${result['initialized']}');
      }

      if (result.containsKey('video_id')) {
        print('   Video ID: ${result['video_id']}');
      }
    } else {
      print('   Error: ${result['message']}');
    }

    print('');
  }

  String _formatTestName(String testKey) {
    switch (testKey) {
      case '1_initialize':
        return 'Initialize Library';
      case '2_isInitialized':
        return 'Check Initialization';
      case '3_getStreamingLinks':
        return 'Get Streaming Links';
      case '4_getRelatedVideos':
        return 'Get Related Videos';
      case '5_getRandomVideos':
        return 'Get Random Videos';
      case '6_getTrendingVideos':
        return 'Get Trending Videos';
      case '7_searchVideos':
        return 'Search Videos';
      case '8_getVideoDetailsWithFormats':
        return 'Get Video Details & Formats';
      case '9_getBestFormatUrls':
        return 'Get Best Format URLs';
      default:
        return testKey;
    }
  }

  String _formatSampleData(dynamic data) {
    if (data is Map) {
      final keys = data.keys.take(3).toList();
      final preview = keys
          .map((key) => '$key: ${_truncateValue(data[key])}')
          .join(', ');
      return '{$preview${data.length > 3 ? ', ...' : ''}}';
    } else if (data is List) {
      return '[${data.length} items]';
    } else {
      return _truncateValue(data);
    }
  }

  String _truncateValue(dynamic value) {
    final str = value.toString();
    return str.length > 50 ? '${str.substring(0, 50)}...' : str;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('YouTube Plugin Test'),
        backgroundColor: Colors.red,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.terminal, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'Running CLI Test...',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 8),
            Text(
              'Check your console/terminal for detailed results',
              style: TextStyle(fontSize: 14, color: Colors.grey),
            ),
            SizedBox(height: 24),
            if (_isRunning)
              CircularProgressIndicator()
            else
              ElevatedButton(
                onPressed: _runTest,
                child: Text('Run Test Again'),
              ),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin.dart';

void main() {
  runApp(const YouTubeDLPApp());
}

class YouTubeDLPApp extends StatelessWidget {
  const YouTubeDLPApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'YouTube DLP Demo',
      theme: ThemeData(primarySwatch: Colors.blue, useMaterial3: true),
      home: const YouTubeDLPHomePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class YouTubeDLPHomePage extends StatefulWidget {
  const YouTubeDLPHomePage({super.key});

  @override
  State<YouTubeDLPHomePage> createState() => _YouTubeDLPHomePageState();
}

class _YouTubeDLPHomePageState extends State<YouTubeDLPHomePage> {
  // Current video ID or URL being processed
  String _videoId =
      'dQw4w9WgXcQ'; // Default: Rick Astley - Never Gonna Give You Up

  // Console output buffer
  final List<String> _consoleOutput = [];
  final ScrollController _scrollController = ScrollController();

  // Quality and codec preferences
  String _videoQuality = '1080p';
  int _audioBitrate = 192;
  String? _audioCodec;
  String? _videoCodec;

  // Loading state
  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('YouTube DLP Demo'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: _showSettingsDialog,
            tooltip: 'Change settings',
          ),
        ],
      ),
      body: Column(
        children: [
          // Console-like output area
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(8),
              color: Colors.black,
              child: ListView.builder(
                controller: _scrollController,
                itemCount: _consoleOutput.length,
                itemBuilder: (context, index) {
                  return SelectableText(
                    _consoleOutput[index],
                    style: const TextStyle(
                      color: Colors.white,
                      fontFamily: 'Monospace',
                      fontSize: 14,
                    ),
                  );
                },
              ),
            ),
          ),

          // Current video info
          Container(
            padding: const EdgeInsets.all(8),
            color: Colors.grey[200],
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    'Current Video: $_videoId',
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.edit),
                  onPressed: _changeVideoId,
                  tooltip: 'Change video',
                ),
              ],
            ),
          ),

          // Action buttons
          Padding(
            padding: const EdgeInsets.all(8),
            child: Wrap(
              spacing: 8,
              runSpacing: 8,
              alignment: WrapAlignment.center,
              children: [
                ElevatedButton.icon(
                  icon: const Icon(Icons.check_circle),
                  label: const Text('Check Status'),
                  onPressed: _checkStatus,
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.videocam),
                  label: const Text('Get Video'),
                  onPressed: _getVideoStreams,
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.audiotrack),
                  label: const Text('Get Audio'),
                  onPressed: _getAudioStreams,
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.all_inclusive),
                  label: const Text('Get Unified'),
                  onPressed: _getUnifiedStreams,
                ),
                ElevatedButton.icon(
                  icon: const Icon(Icons.clear),
                  label: const Text('Clear Console'),
                  onPressed: _clearConsole,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // Add a line to the console output
  void _log(String message, {bool isError = false}) {
    final timestamp = DateTime.now().toString().split(' ')[1].split('.')[0];
    final prefix = isError ? '[ERROR]' : '[INFO]';
    setState(() {
      _consoleOutput.add('$timestamp $prefix $message');
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      });
    });
  }

  // Clear the console output
  void _clearConsole() {
    setState(() {
      _consoleOutput.clear();
    });
  }

  // Change the current video ID
  Future<void> _changeVideoId() async {
    final controller = TextEditingController(text: _videoId);

    final newId = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Change Video ID/URL'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(
            labelText: 'YouTube Video ID or URL',
            hintText: 'e.g. dQw4w9WgXcQ',
          ),
          onSubmitted: (value) => Navigator.pop(context, value),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              if (controller.text.isNotEmpty) {
                Navigator.pop(context, controller.text);
              } else {
                Navigator.pop(context);
              }
            },
            child: const Text('OK'),
          ),
        ],
      ),
    );

    if (newId != null && newId.isNotEmpty) {
      setState(() {
        _videoId = newId.trim();
      });
      _log('Changed video ID to: $_videoId');
    }

    controller.dispose();
  }

  // Show settings dialog for quality and codec preferences
  Future<void> _showSettingsDialog() async {
    final videoQualities = [
      '144p',
      '240p',
      '360p',
      '480p',
      '720p',
      '1080p',
      '1440p',
      '2160p',
      '4320p',
    ];
    final audioBitrates = [64, 128, 192, 256, 320];
    final codecs = ['None', 'opus', 'mp4a.40.2', 'aac', 'vp9', 'avc1', 'av01'];

    String? newVideoQuality = _videoQuality;
    int? newAudioBitrate = _audioBitrate;
    String? newAudioCodec = _audioCodec;
    String? newVideoCodec = _videoCodec;

    await showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('Stream Preferences'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Video quality dropdown
                    DropdownButtonFormField<String>(
                      value: newVideoQuality,
                      items: videoQualities
                          .map(
                            (q) => DropdownMenuItem(value: q, child: Text(q)),
                          )
                          .toList(),
                      onChanged: (value) => setState(() {
                        newVideoQuality = value;
                      }),
                      decoration: const InputDecoration(
                        labelText: 'Video Quality',
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Audio bitrate dropdown
                    DropdownButtonFormField<int>(
                      value: newAudioBitrate,
                      items: audioBitrates
                          .map(
                            (b) => DropdownMenuItem(
                              value: b,
                              child: Text('$b kbps'),
                            ),
                          )
                          .toList(),
                      onChanged: (value) => setState(() {
                        newAudioBitrate = value;
                      }),
                      decoration: const InputDecoration(
                        labelText: 'Audio Bitrate',
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Audio codec dropdown
                    DropdownButtonFormField<String?>(
                      value: newAudioCodec,
                      items: [
                        DropdownMenuItem(
                          value: null,
                          child: const Text('None (auto)'),
                        ),
                        ...codecs
                            .where(
                              (c) =>
                                  c != 'None' &&
                                  ['opus', 'mp4a.40.2', 'aac'].contains(c),
                            )
                            .map(
                              (c) => DropdownMenuItem(
                                value: c == 'None' ? null : c,
                                child: Text(c),
                              ),
                            )
                            .toList(),
                      ],
                      onChanged: (value) => setState(() {
                        newAudioCodec = value;
                      }),
                      decoration: const InputDecoration(
                        labelText: 'Audio Codec',
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Video codec dropdown
                    DropdownButtonFormField<String?>(
                      value: newVideoCodec,
                      items: [
                        DropdownMenuItem(
                          value: null,
                          child: const Text('None (auto)'),
                        ),
                        ...codecs
                            .where(
                              (c) =>
                                  c != 'None' &&
                                  ['vp9', 'avc1', 'av01'].contains(c),
                            )
                            .map(
                              (c) => DropdownMenuItem(
                                value: c == 'None' ? null : c,
                                child: Text(c),
                              ),
                            )
                            .toList(),
                      ],
                      onChanged: (value) => setState(() {
                        newVideoCodec = value;
                      }),
                      decoration: const InputDecoration(
                        labelText: 'Video Codec',
                      ),
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Cancel'),
                ),
                TextButton(
                  onPressed: () {
                    setState(() {
                      _videoQuality = newVideoQuality ?? '1080p';
                      _audioBitrate = newAudioBitrate ?? 192;
                      _audioCodec = newAudioCodec;
                      _videoCodec = newVideoCodec;
                    });
                    Navigator.pop(context);
                    _log('Updated preferences:');
                    _log('  Video Quality: $_videoQuality');
                    _log('  Audio Bitrate: $_audioBitrate kbps');
                    _log('  Audio Codec: ${_audioCodec ?? 'auto'}');
                    _log('  Video Codec: ${_videoCodec ?? 'auto'}');
                  },
                  child: const Text('Save'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  // Check video status
  Future<void> _checkStatus() async {
    if (_isLoading) return;
    _isLoading = true;
    _log('Checking status for video: $_videoId');

    try {
      final status = await FlutterYtdlpPlugin.checkStatus(_videoId);
      _log('Status result:');
      _log('  Available: ${status['available']}');
      _log('  Status: ${status['status']}');
      if (status['error'] != null) {
        _log('  Error: ${status['error']}', isError: true);
      }
    } catch (e) {
      _log('Error checking status: $e', isError: true);
    } finally {
      _isLoading = false;
    }
  }

  // Get video streams
  Future<void> _getVideoStreams() async {
    if (_isLoading) return;
    _isLoading = true;
    _log('Getting video streams for: $_videoId');
    _log('Preferred quality: $_videoQuality');
    if (_videoCodec != null) {
      _log('Preferred video codec: $_videoCodec');
    }

    try {
      final streams = await FlutterYtdlpPlugin.getVideoStreams(
        _videoId,
        quality: _videoQuality,
      );

      if (streams.isEmpty) {
        _log('No video streams found', isError: true);
        return;
      }

      _log('Found ${streams.length} video stream(s)');
      for (final stream in streams) {
        _log('  Stream:');
        _log('    URL: ${_truncateUrl(stream['url'])}');
        _log('    Format: ${stream['ext']}');
        _log('    Resolution: ${stream['resolution']}');
        _log('    Codec: ${stream['codec']}');
        _log('    Bitrate: ${stream['bitrate']} kbps');
        if (stream['filesize'] != null) {
          _log('    Size: ${_formatFileSize(stream['filesize'])}');
        }
      }
    } catch (e) {
      _log('Error getting video streams: $e', isError: true);
    } finally {
      _isLoading = false;
    }
  }

  // Get audio streams
  Future<void> _getAudioStreams() async {
    if (_isLoading) return;
    _isLoading = true;
    _log('Getting audio streams for: $_videoId');
    _log('Preferred bitrate: $_audioBitrate kbps');
    if (_audioCodec != null) {
      _log('Preferred audio codec: $_audioCodec');
    }

    try {
      final streams = await FlutterYtdlpPlugin.getAudioStreams(
        _videoId,
        bitrate: _audioBitrate,
        codec: _audioCodec,
      );

      if (streams.isEmpty) {
        _log('No audio streams found', isError: true);
        return;
      }

      _log('Found ${streams.length} audio stream(s)');
      for (final stream in streams) {
        _log('  Stream:');
        _log('    URL: ${_truncateUrl(stream['url'])}');
        _log('    Format: ${stream['ext']}');
        _log('    Codec: ${stream['codec']}');
        _log('    Bitrate: ${stream['bitrate']} kbps');
        if (stream['filesize'] != null) {
          _log('    Size: ${_formatFileSize(stream['filesize'])}');
        }
      }
    } catch (e) {
      _log('Error getting audio streams: $e', isError: true);
    } finally {
      _isLoading = false;
    }
  }

  // Get unified streams (video + audio)
  Future<void> _getUnifiedStreams() async {
    if (_isLoading) return;
    _isLoading = true;
    _log('Getting unified streams for: $_videoId');
    _log('Video quality: $_videoQuality');
    _log('Audio bitrate: $_audioBitrate kbps');
    if (_videoCodec != null) _log('Video codec: $_videoCodec');
    if (_audioCodec != null) _log('Audio codec: $_audioCodec');

    try {
      final result = await FlutterYtdlpPlugin.getMuxedStreams(
        _videoId,
        videoQuality: _videoQuality,
        audioBitrate: _audioBitrate,
        videoCodec: _videoCodec,
        audioCodec: _audioCodec,
      );

      _log('Video duration: ${result['duration']} seconds');

      if (result['video'] != null) {
        final videoStreams = result['video'] as List;
        _log('Found ${videoStreams.length} video stream(s)');
        for (final stream in videoStreams) {
          _log('  Video Stream:');
          _log('    URL: ${_truncateUrl(stream['url'])}');
          _log('    Format: ${stream['ext']}');
          _log('    Resolution: ${stream['resolution']}');
          _log('    Codec: ${stream['codec']}');
          _log('    Bitrate: ${stream['bitrate']} kbps');
          if (stream['filesize'] != null) {
            _log('    Size: ${_formatFileSize(stream['filesize'])}');
          }
        }
      }

      if (result['audio'] != null) {
        final audioStreams = result['audio'] as List;
        _log('Found ${audioStreams.length} audio stream(s)');
        for (final stream in audioStreams) {
          _log('  Audio Stream:');
          _log('    URL: ${_truncateUrl(stream['url'])}');
          _log('    Format: ${stream['ext']}');
          _log('    Codec: ${stream['codec']}');
          _log('    Bitrate: ${stream['bitrate']} kbps');
          if (stream['filesize'] != null) {
            _log('    Size: ${_formatFileSize(stream['filesize'])}');
          }
        }
      }
    } catch (e) {
      _log('Error getting unified streams: $e', isError: true);
    } finally {
      _isLoading = false;
    }
  }

  // Helper to truncate long URLs for display
  String _truncateUrl(String url) {
    if (url.length <= 50) return url;
    return '${url.substring(0, 20)}...${url.substring(url.length - 20)}';
  }

  // Helper to format file size
  String _formatFileSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    if (bytes < 1024 * 1024 * 1024) {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }
}

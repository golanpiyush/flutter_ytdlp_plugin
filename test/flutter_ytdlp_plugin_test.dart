import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin_platform_interface.dart';
import 'package:flutter_ytdlp_plugin/flutter_ytdlp_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterYtdlpPluginPlatform
    with MockPlatformInterfaceMixin
    implements FlutterYtdlpPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterYtdlpPluginPlatform initialPlatform = FlutterYtdlpPluginPlatform.instance;

  test('$MethodChannelFlutterYtdlpPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterYtdlpPlugin>());
  });

  test('getPlatformVersion', () async {
    FlutterYtdlpPlugin flutterYtdlpPlugin = FlutterYtdlpPlugin();
    MockFlutterYtdlpPluginPlatform fakePlatform = MockFlutterYtdlpPluginPlatform();
    FlutterYtdlpPluginPlatform.instance = fakePlatform;

    expect(await flutterYtdlpPlugin.getPlatformVersion(), '42');
  });
}

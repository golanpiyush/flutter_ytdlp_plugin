# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.2] 

### Fixed
- Fixed SSL socket resource warnings by properly closing connections
- Added explicit cleanup of Python resources when plugin is detached
- Configured SSL context to prevent certificate verification warnings
- Improved thread-local resource management for yt-dlp instances
- Added proper error handling for network resource cleanup

### Changed
- Updated yt-dlp connection handling to use context managers
- Modified Kotlin cleanup sequence to ensure proper resource release
- Improved logging for resource management operations

## [2.0.1]

### Fixed
- Fixed `CHANGELOG.MD` visually.

## [2.0.0]

### Updated
- Updated `ytdlp` python version from `25.06.30` to `25.07.21`

### Added
- Added `getVideoStreams`, `getAudioStreams`, `getMuxedStreams` these replaces old methods.

## [0.0.9]
### Fixed
- Fixed `chaquopy` python version from 3.9 to 3.11

## [0.0.8]
### Fixed
- Fixes to the plugin's `build.gradle`

## [0.0.7]
### Fixed
- Fixed an issue where dart couldn't find the `mainClass` entry in the plugin's pubspec.yaml.

## [0.0.6] 
### Fixed
- Fixed an issue where the python module doesn't get initalised `fileNameSpace` issues.
- [forDevelopers]
  - Added tests checks for ytmusic and Ytdlp

## [0.0.5]
### Fixed
- Resolved Kotlin compilation error: "Unreachable code" in method call handler
- Fixed nested `when` statement structure causing unreachable code after `return@execute`
- Improved code structure by moving initialization checks to individual method handlers
- Enhanced null safety in Python-Kotlin bridge helper methods

## [0.0.4]
### Fixed
- Resolved an issue where the compiler failed to locate the plugin entry point, resulting in the error: "The plugin flutter_ytdlp_plugin doesn't have a main class defined in FlutterYtdlpPlugin.java or .kt."

## [0.0.3]
### Added
- Initialization checks for Python and yt-dlp
- **BREAKING**: `initialize()` now returns `InitializationResult` instead of `bool`

## [0.0.2]
### Fixed
- Crash on Android when Python environment not properly initialized
- Memory leaks in long-running Python operations
- Thread synchronization issues in Kotlin executor
- Initialization checks for Python and yt-dlp
- Better error handling with detailed messages

### Changed
- Improved thread safety in Kotlin bridge
- Initialization checks for Python and yt-dlp

### Added
- `initialize()` method to explicitly start the plugin
- `isInitialized()` method to check plugin status
- Proper documentation for all public methods

## [0.0.1]
### Added
- Initial release of flutter_ytdlp_plugin
- Support for YouTube video streaming via yt-dlp
- Methods for:
  - Getting streaming links
  - Fetching related videos
  - Searching YouTube
  - Getting video details with formats
  - Extracting best quality URLs
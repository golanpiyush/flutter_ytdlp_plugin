package com.golanpiyush.flutter_ytdlp_plugin

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.*
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import android.content.Context

/**
 * FlutterYtdlpPlugin - FIXED VERSION
 * 
 * Fixed initialization issues and improved error handling
 */
class FlutterYtdlpPlugin: FlutterPlugin, MethodCallHandler {
  
  // Method channel for communication with Flutter
  private lateinit var channel: MethodChannel
  
  // Python instance for executing ytdlp_services.py
  private var python: Python? = null
  
  // Python module reference
  private var ytdlpModule: com.chaquo.python.PyObject? = null
  
  // Extractor instance from Python
  private var extractor: com.chaquo.python.PyObject? = null
  
  // Coroutine scope for async operations
  private val pluginScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  
  // Application context
  private var applicationContext: Context? = null
  
  // Initialization status tracking
  private var initializationAttempted = false
  private var initializationError: String? = null

  /**
   * Called when the plugin is attached to the Flutter engine
   */
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_ytdlp_plugin")
    channel.setMethodCallHandler(this)
    
    // Initialize Python environment asynchronously to avoid blocking
    pluginScope.launch(Dispatchers.IO) {
      initializePython()
    }
  }

  /**
   * FIXED: Initialize Python environment and load ytdlp_services module
   * Enhanced error handling and logging
   */
  private fun initializePython() {
    if (initializationAttempted && extractor != null) {
      println("YTDLP_FLUTTER_DEBUG: Python already initialized successfully")
      return
    }
    
    initializationAttempted = true
    initializationError = null
    
    try {
      println("YTDLP_FLUTTER_DEBUG: Starting Python initialization...")
      
      // Step 1: Check application context
      val context = applicationContext
      if (context == null) {
        initializationError = "Application context is null"
        println("YTDLP_FLUTTER_ERROR: $initializationError")
        return
      }
      
      // Step 2: Initialize Python if not already initialized
      try {
        if (!Python.isStarted()) {
          println("YTDLP_FLUTTER_DEBUG: Initializing Python platform...")
          Python.start(AndroidPlatform(context))
          println("YTDLP_FLUTTER_DEBUG: Python platform initialized")
        } else {
          println("YTDLP_FLUTTER_DEBUG: Python already started")
        }
      } catch (e: Exception) {
        initializationError = "Failed to start Python platform: ${e.message}"
        println("YTDLP_FLUTTER_ERROR: $initializationError")
        e.printStackTrace()
        return
      }
      
      // Step 3: Get Python instance
      try {
        python = Python.getInstance()
        println("YTDLP_FLUTTER_DEBUG: Python instance obtained")
      } catch (e: Exception) {
        initializationError = "Failed to get Python instance: ${e.message}"
        println("YTDLP_FLUTTER_ERROR: $initializationError")
        e.printStackTrace()
        return
      }
      
      // Step 4: Import the ytdlp_services module
      try {
        println("YTDLP_FLUTTER_DEBUG: Importing ytdlp_services module...")
        ytdlpModule = python?.getModule("ytdlp_services")
        
        if (ytdlpModule == null) {
          initializationError = "Failed to import ytdlp_services module - module is null"
          println("YTDLP_FLUTTER_ERROR: $initializationError")
          return
        }
        println("YTDLP_FLUTTER_DEBUG: ytdlp_services module imported successfully")
        
      } catch (e: Exception) {
        initializationError = "Failed to import ytdlp_services module: ${e.message}"
        println("YTDLP_FLUTTER_ERROR: $initializationError")
        e.printStackTrace()
        
        // Try to list available modules for debugging
        try {
          val sysModule = python?.getModule("sys")
          val modules = sysModule?.get("modules")
          println("YTDLP_FLUTTER_DEBUG: Available modules: ${modules?.callAttr("keys")}")
        } catch (debugException: Exception) {
          println("YTDLP_FLUTTER_DEBUG: Could not list modules: ${debugException.message}")
        }
        return
      }
      
      // Step 5: Create extractor instance
      try {
        println("YTDLP_FLUTTER_DEBUG: Creating YouTubeStreamExtractor instance...")
        val extractorClass = ytdlpModule?.get("YouTubeStreamExtractor")
        
        if (extractorClass == null) {
          initializationError = "YouTubeStreamExtractor class not found in ytdlp_services module"
          println("YTDLP_FLUTTER_ERROR: $initializationError")
          return
        }
        
        // Create extractor instance with default parameters
        extractor = extractorClass.call()
        
        if (extractor == null) {
          initializationError = "Failed to create YouTubeStreamExtractor instance - returned null"
          println("YTDLP_FLUTTER_ERROR: $initializationError")
          return
        }
        
        println("YTDLP_FLUTTER_DEBUG: YouTubeStreamExtractor instance created successfully")
        
      } catch (e: Exception) {
        initializationError = "Failed to create YouTubeStreamExtractor instance: ${e.message}"
        println("YTDLP_FLUTTER_ERROR: $initializationError")
        e.printStackTrace()
        return
      }
      
      // Step 6: Test the extractor
      try {
        println("YTDLP_FLUTTER_DEBUG: Testing extractor functionality...")
        // You can add a simple test call here if needed
        println("YTDLP_FLUTTER_DEBUG: Extractor test completed")
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_WARNING: Extractor test failed, but initialization completed: ${e.message}")
      }
      
      println("YTDLP_FLUTTER_DEBUG: Python environment initialized successfully")
      
    } catch (e: Exception) {
      initializationError = "Unexpected error during Python initialization: ${e.message}"
      println("YTDLP_FLUTTER_ERROR: $initializationError")
      e.printStackTrace()
    }
  }

  /**
   * ENHANCED: Handle method calls from Flutter with better error reporting
   */
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    // Special handling for initialization-related methods
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
        return
      }
      "initializePython" -> {
        handleManualInitialization(result)
        return
      }
      "getInitializationStatus" -> {
        handleGetInitializationStatus(result)
        return
      }
    }
    
    // Check if Python is properly initialized for other methods
    if (extractor == null) {
      val errorMessage = if (initializationError != null) {
        "Python initialization failed: $initializationError"
      } else if (!initializationAttempted) {
        "Python initialization not yet attempted"
      } else {
        "Python environment not initialized (unknown reason)"
      }
      
      result.error("PYTHON_NOT_INITIALIZED", errorMessage, mapOf(
        "initializationAttempted" to initializationAttempted,
        "initializationError" to initializationError
      ))
      return
    }
    
    when (call.method) {
      "checkStatus" -> handleCheckStatus(call, result)
      "getVideoStreams" -> handleGetVideoStreams(call, result)
      "getAudioStreams" -> handleGetAudioStreams(call, result)
      "getUnifiedStreams" -> handleGetUnifiedStreams(call, result)
      else -> result.notImplemented()
    }
  }

  /**
   * NEW: Handle manual initialization request
   */
  private fun handleManualInitialization(result: Result) {
    pluginScope.launch(Dispatchers.IO) {
      try {
        // Reset initialization state
        initializationAttempted = false
        initializationError = null
        extractor = null
        ytdlpModule = null
        
        // Re-initialize
        initializePython()
        
        withContext(Dispatchers.Main) {
          if (extractor != null) {
            result.success(mapOf(
              "success" to true,
              "message" to "Python initialized successfully"
            ))
          } else {
            result.error("INITIALIZATION_FAILED", 
              initializationError ?: "Unknown initialization error", 
              mapOf(
                "initializationError" to initializationError
              ))
          }
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          result.error("INITIALIZATION_EXCEPTION", 
            "Exception during initialization: ${e.message}", 
            mapOf("exception" to e.toString()))
        }
      }
    }
  }

  /**
   * NEW: Get initialization status for debugging
   */
  private fun handleGetInitializationStatus(result: Result) {
    val status = mapOf(
      "pythonStarted" to (python != null && Python.isStarted()),
      "pythonInstance" to (python != null),
      "ytdlpModuleLoaded" to (ytdlpModule != null),
      "extractorCreated" to (extractor != null),
      "initializationAttempted" to initializationAttempted,
      "initializationError" to initializationError,
      "applicationContextAvailable" to (applicationContext != null)
    )
    result.success(status)
  }

  /**
   * Handle check video status request with improved error handling
   */
  private fun handleCheckStatus(call: MethodCall, result: Result) {
    pluginScope.launch {
      try {
        val videoId = call.argument<String>("videoId")
          ?: return@launch result.error("INVALID_ARGUMENT", "Video ID is required", null)

        val pythonResult = withContext(Dispatchers.IO) {
          extractor?.callAttr("check_status", videoId)
        }

        if (pythonResult != null) {
          val statusMap = convertPythonDictToMap(pythonResult)
          result.success(statusMap)
        } else {
          result.error("PYTHON_ERROR", "Failed to check video status - null result", null)
        }
        
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_ERROR: Exception in checkStatus: ${e.message}")
        e.printStackTrace()
        result.error("EXCEPTION", "Error checking status: ${e.message}", null)
      }
    }
  }

  /**
   * Handle get video streams request with improved error handling
   */
  private fun handleGetVideoStreams(call: MethodCall, result: Result) {
    pluginScope.launch {
      try {
        val videoId = call.argument<String>("videoId")
          ?: return@launch result.error("INVALID_ARGUMENT", "Video ID is required", null)
        
        val quality = call.argument<String>("quality") ?: "1080p"

        val pythonResult = withContext(Dispatchers.IO) {
          extractor?.callAttr("get_video_streams", videoId, quality)
        }

        if (pythonResult != null) {
          val streamsList = convertPythonStreamListToMapList(pythonResult)
          result.success(streamsList)
        } else {
          result.error("PYTHON_ERROR", "Failed to get video streams - null result", null)
        }
        
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_ERROR: Exception in getVideoStreams: ${e.message}")
        e.printStackTrace()
        result.error("EXCEPTION", "Error getting video streams: ${e.message}", null)
      }
    }
  }

  /**
   * Handle get audio streams request with improved error handling
   */
  private fun handleGetAudioStreams(call: MethodCall, result: Result) {
    pluginScope.launch {
      try {
        val videoId = call.argument<String>("videoId")
          ?: return@launch result.error("INVALID_ARGUMENT", "Video ID is required", null)
        
        val bitrate = call.argument<Int>("bitrate") ?: 192
        val codec = call.argument<String>("codec") // Can be null

        val pythonResult = withContext(Dispatchers.IO) {
          if (codec != null) {
            extractor?.callAttr("get_audio_streams", videoId, bitrate, codec)
          } else {
            extractor?.callAttr("get_audio_streams", videoId, bitrate)
          }
        }

        if (pythonResult != null) {
          val streamsList = convertPythonStreamListToMapList(pythonResult)
          result.success(streamsList)
        } else {
          result.error("PYTHON_ERROR", "Failed to get audio streams - null result", null)
        }
        
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_ERROR: Exception in getAudioStreams: ${e.message}")
        e.printStackTrace()
        result.error("EXCEPTION", "Error getting audio streams: ${e.message}", null)
      }
    }
  }

  /**
   * Handle get unified streams request with improved error handling
   */
  private fun handleGetUnifiedStreams(call: MethodCall, result: Result) {
    pluginScope.launch {
      try {
        val videoId = call.argument<String>("videoId")
          ?: return@launch result.error("INVALID_ARGUMENT", "Video ID is required", null)
        
        // Extract parameters with defaults matching Python function
        val audioBitrate = call.argument<Int>("audioBitrate") ?: 192
        val videoQuality = call.argument<String>("videoQuality") ?: "1080p"
        val audioCodec = call.argument<String>("audioCodec") // Can be null
        val videoCodec = call.argument<String>("videoCodec") // Can be null
        val includeVideo = call.argument<Boolean>("includeVideo") ?: true
        val includeAudio = call.argument<Boolean>("includeAudio") ?: true

        val pythonResult = withContext(Dispatchers.IO) {
          // Call with all parameters explicitly
          extractor?.callAttr("get_unified_streams", videoId, 
            audioBitrate, videoQuality, audioCodec, videoCodec, includeVideo, includeAudio)
        }

        if (pythonResult != null) {
          val unifiedMap = convertPythonUnifiedResultToMap(pythonResult)
          result.success(unifiedMap)
        } else {
          result.error("PYTHON_ERROR", "Failed to get unified streams - null result", null)
        }
        
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_ERROR: Exception in getUnifiedStreams: ${e.message}")
        e.printStackTrace()
        result.error("EXCEPTION", "Error getting unified streams: ${e.message}", null)
      }
    }
  }

  // [Keep all the existing conversion methods unchanged]
  private fun convertPythonDictToMap(pythonDict: com.chaquo.python.PyObject): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    
    try {
      // Method 1: Direct key access for known dictionary structure
      val knownKeys = listOf("available", "status", "error", "duration")
      for (key in knownKeys) {
        try {
          val hasKey = pythonDict.callAttr("__contains__", key).toBoolean()
          if (hasKey) {
            val value = pythonDict.callAttr("__getitem__", key)
            map[key] = convertPythonValue(value)
          }
        } catch (e: Exception) {
          // Continue with next key
        }
      }
      
      if (map.isNotEmpty()) {
        return map
      }
      
      // Method 2: Use Python's dict conversion via string representation
      try {
        val dictStr = pythonDict.toString()
        if (dictStr.startsWith("{") && dictStr.endsWith("}")) {
          println("YTDLP_FLUTTER_DEBUG: Dict string representation: $dictStr")
        }
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_DEBUG: String conversion failed: ${e.message}")
      }
      
      // Method 3: Use Python's built-in dict() conversion through module
      try {
        val pythonModule = python?.getBuiltins()
        val dictFunc = pythonModule?.get("dict")
        val convertedDict = dictFunc?.call(pythonDict)
        
        if (convertedDict != null) {
          val keysObj = convertedDict.callAttr("keys")
          val keysIterator = keysObj.callAttr("__iter__")
          
          while (true) {
            try {
              val key = keysIterator.callAttr("__next__")
              val keyStr = key.toString()
              val value = convertedDict.callAttr("__getitem__", keyStr)
              map[keyStr] = convertPythonValue(value)
            } catch (e: Exception) {
              break
            }
          }
        }
      } catch (e: Exception) {
        println("YTDLP_FLUTTER_DEBUG: Method 3 failed: ${e.message}")
      }
      
      // Method 4: Last resort - manual key probing
      if (map.isEmpty()) {
        val commonKeys = listOf(
          "available", "status", "error", "duration", "video", "audio",
          "url", "ext", "resolution", "height", "width", "bitrate", "codec",
          "filesize", "format_note", "format_id"
        )
        
        for (key in commonKeys) {
          try {
            val value = pythonDict.callAttr("get", key)
            if (value != null && value.toString() != "None") {
              map[key] = convertPythonValue(value)
            }
          } catch (e: Exception) {
            // Continue with next key
          }
        }
      }
      
    } catch (e: Exception) {
      println("YTDLP_FLUTTER_ERROR: All conversion methods failed: ${e.message}")
      e.printStackTrace()
    }
    
    return map
  }

  private fun convertPythonStreamListToMapList(pythonList: com.chaquo.python.PyObject): List<Map<String, Any?>> {
    val streamsList = mutableListOf<Map<String, Any?>>()
    
    try {
      val listLength = pythonList.callAttr("__len__").toInt()
      if (listLength == 0) return streamsList
      
      for (i in 0 until listLength) {
        try {
          val streamObj = pythonList.callAttr("__getitem__", i)
          val streamMap = convertStreamInfoToMap(streamObj)
          if (streamMap.isNotEmpty()) {
            streamsList.add(streamMap)
          }
        } catch (e: Exception) {
          println("YTDLP_FLUTTER_WARNING: Failed to convert stream at index $i: ${e.message}")
          continue
        }
      }
      
    } catch (e: Exception) {
      println("YTDLP_FLUTTER_ERROR: Failed to convert Python stream list: ${e.message}")
      e.printStackTrace()
      
      try {
        val listItems = pythonList.asList()
        for (streamObj in listItems) {
          val streamMap = convertStreamInfoToMap(streamObj)
          if (streamMap.isNotEmpty()) {
            streamsList.add(streamMap)
          }
        }
      } catch (fallbackError: Exception) {
        println("YTDLP_FLUTTER_ERROR: Fallback method also failed: ${fallbackError.message}")
      }
    }
    
    return streamsList
  }

  private fun convertPythonUnifiedResultToMap(pythonDict: com.chaquo.python.PyObject): Map<String, Any?> {
    val resultMap = mutableMapOf<String, Any?>()
    
    try {
      val knownFields = listOf("duration", "video", "audio")
      
      for (field in knownFields) {
        try {
          val hasField = pythonDict.callAttr("__contains__", field).toBoolean()
          if (hasField) {
            val value = pythonDict.callAttr("__getitem__", field)
            
            when (field) {
              "duration" -> {
                resultMap[field] = value?.toInt() ?: 0
              }
              "video", "audio" -> {
                if (value != null && value.toString() != "None") {
                  resultMap[field] = convertPythonStreamListToMapList(value)
                }
              }
            }
          }
        } catch (e: Exception) {
          when (field) {
            "duration" -> resultMap[field] = 0
            "video", "audio" -> {
              println("YTDLP_FLUTTER_DEBUG: Optional field '$field' not available: ${e.message}")
            }
          }
        }
      }
      
      if (!resultMap.containsKey("duration")) {
        resultMap["duration"] = 0
      }
      
    } catch (e: Exception) {
      println("YTDLP_FLUTTER_ERROR: Failed to convert unified result: ${e.message}")
      e.printStackTrace()
      resultMap["duration"] = 0
    }
    
    return resultMap
  }

  private fun convertStreamInfoToMap(streamObj: com.chaquo.python.PyObject): Map<String, Any?> {
    val streamMap = mutableMapOf<String, Any?>()
    
    try {
      val fieldMapping = mapOf(
        "url" to "url",
        "ext" to "ext", 
        "height" to "height",
        "width" to "width",
        "bitrate" to "bitrate",
        "codec" to "codec",
        "resolution" to "resolution",
        "filesize" to "filesize",
        "format_id" to "formatId",
        "format_note" to "formatNote"
      )
      
      for ((pythonField, kotlinKey) in fieldMapping) {
        try {
          val value = streamObj.callAttr("__getattribute__", pythonField)
          streamMap[kotlinKey] = convertPythonValue(value)
        } catch (e: Exception) {
          try {
            val hasAttr = python?.getBuiltins()?.get("hasattr")?.call(streamObj, pythonField)?.toBoolean() ?: false
            if (hasAttr) {
              val value = python?.getBuiltins()?.get("getattr")?.call(streamObj, pythonField)
              streamMap[kotlinKey] = convertPythonValue(value)
            } else {
              streamMap[kotlinKey] = when (kotlinKey) {
                "url" -> ""
                "ext" -> "unknown"
                else -> null
              }
            }
          } catch (fallbackError: Exception) {
            streamMap[kotlinKey] = when (kotlinKey) {
              "url" -> ""
              "ext" -> "unknown"
              else -> null
            }
          }
        }
      }
      
    } catch (e: Exception) {
      println("YTDLP_FLUTTER_ERROR: Failed to convert StreamInfo: ${e.message}")
      e.printStackTrace()
      
      streamMap["url"] = ""
      streamMap["ext"] = "unknown"
      streamMap["height"] = null
      streamMap["width"] = null
      streamMap["bitrate"] = null
      streamMap["codec"] = null
      streamMap["resolution"] = null
      streamMap["filesize"] = null
      streamMap["formatId"] = null
      streamMap["formatNote"] = null
    }
    
    return streamMap
  }

  private fun convertPythonValue(pythonValue: com.chaquo.python.PyObject?): Any? {
    if (pythonValue == null) return null
    
    return try {
      val stringValue = pythonValue.toString()
      
      when {
        stringValue == "None" -> null
        stringValue == "True" -> true
        stringValue == "False" -> false
        else -> {
          stringValue.toIntOrNull()?.let { return it }
          stringValue.toDoubleOrNull()?.let { return it }
          stringValue
        }
      }
    } catch (e: Exception) {
      pythonValue.toString()
    }
  }

  /**
   * Called when the plugin is detached from the Flutter engine
   */
  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    
    // Cancel all coroutines
    pluginScope.cancel()
    
    // Clean up Python resources
    extractor = null
    ytdlpModule = null
    python = null
    applicationContext = null
  }
}
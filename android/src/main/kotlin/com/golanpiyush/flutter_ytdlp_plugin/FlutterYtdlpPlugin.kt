package com.golanpiyush.flutter_ytdlp_plugin

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** FlutterYtdlpPlugin */
class FlutterYtdlpPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private lateinit var executor: ExecutorService
  private lateinit var context: Context
  private lateinit var python: Python
  private var youtubeLibrary: com.chaquo.python.PyObject? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_ytdlp_plugin")
    channel.setMethodCallHandler(this)
    executor = Executors.newFixedThreadPool(4)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    // Execute all YouTube operations on background thread
    executor.execute {
      try {
        when (call.method) {
          "initialize" -> handleInitialize(result)
          "isInitialized" -> handleIsInitialized(result)
          "testAllFunctions" -> handleTestAllFunctions(result)
          "getStreamingLinks" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val title = call.argument<String>("title") ?: ""
            val channelName = call.argument<String>("channelName") ?: ""
            handleGetStreamingLinks(title, channelName, result)
          }
          "getRelatedVideos" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val title = call.argument<String>("title") ?: ""
            val channelName = call.argument<String>("channelName") ?: ""
            val count = call.argument<Int>("count") ?: 200
            handleGetRelatedVideos(title, channelName, count, result)
          }
          "getRandomVideos" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val category = call.argument<String>("category")
            val count = call.argument<Int>("count") ?: 50
            handleGetRandomVideos(category, count, result)
          }
          "getTrendingVideos" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val count = call.argument<Int>("count") ?: 50
            handleGetTrendingVideos(count, result)
          }
          "searchVideos" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val query = call.argument<String>("query") ?: ""
            val maxResults = call.argument<Int>("maxResults") ?: 25
            handleSearchVideos(query, maxResults, result)
          }
          "getVideoDetailsWithFormats" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val videoId = call.argument<String>("videoId") ?: ""
            handleGetVideoDetailsWithFormats(videoId, result)
          }
          "getBestFormatUrls" -> {
            if (youtubeLibrary == null) {
              result.error("NOT_INITIALIZED", "YouTube library not initialized", null)
              return@execute
            }
            val videoId = call.argument<String>("videoId") ?: ""
            handleGetBestFormatUrls(videoId, result)
          }
          else -> {
            result.notImplemented()
          }
        }
      } catch (e: Exception) {
        result.error("UNKNOWN_ERROR", e.message, null)
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    executor.shutdown()
  }

  // MARK: - Handler Methods

  private fun handleInitialize(result: Result) {
    try {
      if (!Python.isStarted()) {
        Python.start(AndroidPlatform(context))
      }
      python = Python.getInstance()
      
      val module = python.getModule("ytdlp_services")
      youtubeLibrary = module.callAttr("YouTubeStreamLibrary")
      
      // Call Python's initialize()
      val initResult = youtubeLibrary?.callAttr("initialize")
      val kotlinResult = pythonDictToKotlinMap(initResult)
      
      if (kotlinResult["status"] == "success") {
        result.success(true)
      } else {
        result.error("INIT_ERROR", kotlinResult["message"].toString(), null)
      }
    } catch (e: Exception) {
      result.error("INIT_ERROR", e.message, null)
    }
  }

  private fun handleIsInitialized(result: Result) {
    try {
      if (youtubeLibrary == null) {
        result.success(false)
        return
      }
      
      val pyResult = youtubeLibrary?.callAttr("is_initialized")
      val kotlinResult = pythonDictToKotlinMap(pyResult)
      result.success(kotlinResult["initialized"] as? Boolean ?: false)
    } catch (e: Exception) {
      result.error("INIT_CHECK_ERROR", e.message, null)
    }
  }

  private fun handleGetStreamingLinks(title: String, channelName: String, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("get_streaming_links", title, channelName)
        val streamingLinks = pythonListToKotlinList(pyResult)
        
        if (streamingLinks.isNotEmpty()) {
          val firstResult = streamingLinks.first()
          if (firstResult.containsKey("error")) {
            result.error("STREAMING_LINKS_ERROR", firstResult["error"].toString(), null)
          } else {
            result.success(firstResult)
          }
        } else {
          result.error("NO_STREAMING_LINKS", "No streaming links found", null)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("STREAMING_LINKS_ERROR", e.message, null)
    }
  }


  // Then add this handler method:
private fun handleTestAllFunctions(result: Result) {
  testAllFunctions(result)
}

private fun testAllFunctions(result: Result) {
  executor.execute {
    try {
      val testResults = mutableMapOf<String, Any?>()
      val testTitle = "edsheeran - perfect"
      val testChannelName = "Ed Sheeran"
      val testQuery = "edsheeran perfect"
      val testCount = 4
      
      // Test 1: Initialize
      testResults["1_initialize"] = try {
        if (!Python.isStarted()) {
          Python.start(AndroidPlatform(context))
        }
        python = Python.getInstance()
        
        val module = python.getModule("ytdlp_services")
        youtubeLibrary = module.callAttr("YouTubeStreamLibrary")
        
        val initResult = youtubeLibrary?.callAttr("initialize")
        val kotlinResult = pythonDictToKotlinMap(initResult)
        
        if (kotlinResult["status"] == "success") {
          mapOf("status" to "success", "message" to "Initialization successful")
        } else {
          mapOf("status" to "error", "message" to kotlinResult["message"].toString())
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Init error: ${e.message}")
      }
      
      // Test 2: Is Initialized
      testResults["2_isInitialized"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "initialized" to false, "message" to "Library not initialized")
        } else {
          val pyResult = youtubeLibrary?.callAttr("is_initialized")
          val kotlinResult = pythonDictToKotlinMap(pyResult)
          mapOf("status" to "success", "initialized" to (kotlinResult["initialized"] as? Boolean ?: false))
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Init check error: ${e.message}")
      }
      
      // Test 3: Get Streaming Links
      testResults["3_getStreamingLinks"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          val pyResult = youtubeLibrary!!.callAttr("get_streaming_links", testTitle, testChannelName)
          val streamingLinks = pythonListToKotlinList(pyResult)
          
          if (streamingLinks.isNotEmpty()) {
            val firstResult = streamingLinks.first()
            if (firstResult.containsKey("error")) {
              mapOf("status" to "error", "message" to firstResult["error"].toString())
            } else {
              mapOf("status" to "success", "data" to firstResult, "count" to streamingLinks.size)
            }
          } else {
            mapOf("status" to "error", "message" to "No streaming links found")
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Streaming links error: ${e.message}")
      }
      
      // Test 4: Get Related Videos
      testResults["4_getRelatedVideos"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          val pyResult = youtubeLibrary!!.callAttr("get_related_videos", testTitle, testChannelName, testCount)
          val relatedVideos = pythonListToKotlinList(pyResult)
          
          val firstError = relatedVideos.find { it.containsKey("error") }
          if (firstError != null) {
            mapOf("status" to "error", "message" to firstError["error"].toString())
          } else {
            mapOf("status" to "success", "data" to relatedVideos, "count" to relatedVideos.size)
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Related videos error: ${e.message}")
      }
      
      // Test 5: Get Random Videos
      testResults["5_getRandomVideos"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          val pyResult = youtubeLibrary!!.callAttr("get_random_videos", "music", testCount)
          val randomVideos = pythonListToKotlinList(pyResult)
          
          val firstError = randomVideos.find { it.containsKey("error") }
          if (firstError != null) {
            mapOf("status" to "error", "message" to firstError["error"].toString())
          } else {
            mapOf("status" to "success", "data" to randomVideos, "count" to randomVideos.size)
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Random videos error: ${e.message}")
      }
      
      // Test 6: Get Trending Videos
      testResults["6_getTrendingVideos"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          val pyResult = youtubeLibrary!!.callAttr("get_trending_videos", testCount)
          val trendingVideos = pythonListToKotlinList(pyResult)
          
          val firstError = trendingVideos.find { it.containsKey("error") }
          if (firstError != null) {
            mapOf("status" to "error", "message" to firstError["error"].toString())
          } else {
            mapOf("status" to "success", "data" to trendingVideos, "count" to trendingVideos.size)
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Trending videos error: ${e.message}")
      }
      
      // Test 7: Search Videos
      testResults["7_searchVideos"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          val pyResult = youtubeLibrary!!.callAttr("search_videos", testQuery, testCount)
          val searchResults = pythonListToKotlinList(pyResult)
          
          val firstError = searchResults.find { it.containsKey("error") }
          if (firstError != null) {
            mapOf("status" to "error", "message" to firstError["error"].toString())
          } else {
            mapOf("status" to "success", "data" to searchResults, "count" to searchResults.size)
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Search videos error: ${e.message}")
      }
      
      // Test 8: Get Video Details With Formats (using first video ID from search if available)
      testResults["8_getVideoDetailsWithFormats"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          // First try to get a video ID from search results
          val searchResult = testResults["7_searchVideos"] as? Map<String, Any?>
          val videoId = if (searchResult?.get("status") == "success") {
            val searchData = searchResult["data"] as? List<Map<String, Any?>>
            searchData?.firstOrNull()?.get("video_id")?.toString() ?: "dQw4w9WgXcQ" // Fallback to Rick Roll
          } else {
            "dQw4w9WgXcQ" // Fallback video ID
          }
          
          val pyResult = youtubeLibrary!!.callAttr("get_video_details_with_formats", videoId)
          val details = pythonDictToKotlinMap(pyResult)
          
          if (details.containsKey("error")) {
            mapOf("status" to "error", "message" to details["error"].toString())
          } else {
            mapOf("status" to "success", "data" to details, "video_id" to videoId)
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Video details error: ${e.message}")
      }
      
      // Test 9: Get Best Format URLs (using same video ID as above)
      testResults["9_getBestFormatUrls"] = try {
        if (youtubeLibrary == null) {
          mapOf("status" to "error", "message" to "Library not initialized")
        } else {
          // Use the same video ID from the previous test
          val detailsResult = testResults["8_getVideoDetailsWithFormats"] as? Map<String, Any?>
          val videoId = detailsResult?.get("video_id")?.toString() ?: "dQw4w9WgXcQ"
          
          val pyResult = youtubeLibrary!!.callAttr("get_best_format_urls", videoId)
          val formatUrls = pythonDictToKotlinMap(pyResult)
          
          if (formatUrls.containsKey("error")) {
            mapOf("status" to "error", "message" to formatUrls["error"].toString())
          } else {
            mapOf("status" to "success", "data" to formatUrls, "video_id" to videoId)
          }
        }
      } catch (e: Exception) {
        mapOf("status" to "error", "message" to "Best formats error: ${e.message}")
      }
      
      // Add test summary
      val successCount = testResults.values.count { 
        (it as? Map<String, Any?>)?.get("status") == "success" 
      }
      val totalTests = testResults.size
      
      testResults["test_summary"] = mapOf(
        "total_tests" to totalTests,
        "successful_tests" to successCount,
        "failed_tests" to (totalTests - successCount),
        "success_rate" to "${(successCount * 100.0 / totalTests).toInt()}%",
        "test_query" to testQuery,
        "test_title" to testTitle,
        "test_channel" to testChannelName,
        "requested_count" to testCount
      )
      
      result.success(testResults)
      
    } catch (e: Exception) {
      result.error("TEST_ERROR", "Test execution failed: ${e.message}", null)
    }
  }
}

  private fun handleGetRelatedVideos(title: String, channelName: String, count: Int, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("get_related_videos", title, channelName, count)
        val relatedVideos = pythonListToKotlinList(pyResult)
        
        val firstError = relatedVideos.find { it.containsKey("error") }
        if (firstError != null) {
          result.error("RELATED_VIDEOS_ERROR", firstError["error"].toString(), null)
        } else {
          result.success(relatedVideos)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("RELATED_VIDEOS_ERROR", e.message, null)
    }
  }

  private fun handleGetRandomVideos(category: String?, count: Int, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("get_random_videos", category, count)
        val randomVideos = pythonListToKotlinList(pyResult)
        
        val firstError = randomVideos.find { it.containsKey("error") }
        if (firstError != null) {
          result.error("RANDOM_VIDEOS_ERROR", firstError["error"].toString(), null)
        } else {
          result.success(randomVideos)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("RANDOM_VIDEOS_ERROR", e.message, null)
    }
  }

  private fun handleGetTrendingVideos(count: Int, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("get_trending_videos", count)
        val trendingVideos = pythonListToKotlinList(pyResult)
        
        val firstError = trendingVideos.find { it.containsKey("error") }
        if (firstError != null) {
          result.error("TRENDING_VIDEOS_ERROR", firstError["error"].toString(), null)
        } else {
          result.success(trendingVideos)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("TRENDING_VIDEOS_ERROR", e.message, null)
    }
  }

  private fun handleSearchVideos(query: String, maxResults: Int, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("search_videos", query, maxResults)
        val searchResults = pythonListToKotlinList(pyResult)
        
        val firstError = searchResults.find { it.containsKey("error") }
        if (firstError != null) {
          result.error("SEARCH_VIDEOS_ERROR", firstError["error"].toString(), null)
        } else {
          result.success(searchResults)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("SEARCH_VIDEOS_ERROR", e.message, null)
    }
  }

  private fun handleGetVideoDetailsWithFormats(videoId: String, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("get_video_details_with_formats", videoId)
        val details = pythonDictToKotlinMap(pyResult)
        
        if (details.containsKey("error")) {
          result.error("VIDEO_DETAILS_ERROR", details["error"].toString(), null)
        } else {
          result.success(details)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("VIDEO_DETAILS_ERROR", e.message, null)
    }
  }

  private fun handleGetBestFormatUrls(videoId: String, result: Result) {
    try {
      youtubeLibrary?.let { library ->
        val pyResult = library.callAttr("get_best_format_urls", videoId)
        val formatUrls = pythonDictToKotlinMap(pyResult)
        
        if (formatUrls.containsKey("error")) {
          result.error("BEST_FORMATS_ERROR", formatUrls["error"].toString(), null)
        } else {
          result.success(formatUrls)
        }
      } ?: result.error("PYTHON_ERROR", "YouTube library not initialized", null)
    } catch (e: Exception) {
      result.error("BEST_FORMATS_ERROR", e.message, null)
    }
  }

  // MARK: - Helper Methods

  private fun pythonListToKotlinList(pyList: com.chaquo.python.PyObject?): List<Map<String, Any?>> {
    if (pyList == null) return emptyList()
    
    val result = mutableListOf<Map<String, Any?>>()
    
    try {
      val listSize = pyList.callAttr("__len__").toInt()
      
      for (i in 0 until listSize) {
        val pyItem = pyList.callAttr("__getitem__", i)
        result.add(pythonDictToKotlinMap(pyItem))
      }
    } catch (e: Exception) {
      // Return empty list if conversion fails
    }
    
    return result
  }

  private fun pythonDictToKotlinMap(pyDict: com.chaquo.python.PyObject?): Map<String, Any?> {
    if (pyDict == null) return emptyMap()
    
    val result = mutableMapOf<String, Any?>()
    
    try {
      val keys = pyDict.callAttr("keys")
      val keysList = pythonKeysToList(keys)
      
      for (key in keysList) {
        val value = pyDict.callAttr("get", key)
        result[key] = pythonValueToKotlin(value)
      }
    } catch (e: Exception) {
      result["error"] = "Failed to convert Python dict: ${e.message}"
    }
    
    return result
  }

  private fun pythonKeysToList(pyKeys: com.chaquo.python.PyObject): List<String> {
    val result = mutableListOf<String>()
    try {
      val keysList = pyKeys.callAttr("__iter__")
      while (true) {
        try {
          val key = keysList.callAttr("__next__")
          result.add(key.toString())
        } catch (e: Exception) {
          break // End of iteration
        }
      }
    } catch (e: Exception) {
      // Handle iteration error
    }
    return result
  }

  private fun pythonValueToKotlin(pyValue: com.chaquo.python.PyObject?): Any? {
    if (pyValue == null) return null
    
    return try {
      when {
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "NoneType" -> null
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "str" -> pyValue.toString()
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "int" -> pyValue.toInt()
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "float" -> pyValue.toDouble()
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "bool" -> pyValue.toBoolean()
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "list" -> pythonListToKotlinList(pyValue)
        pyValue.callAttr("__class__").callAttr("__name__").toString() == "dict" -> pythonDictToKotlinMap(pyValue)
        else -> pyValue.toString()
      }
    } catch (e: Exception) {
      pyValue.toString()
    }
  }
}
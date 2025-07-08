package com.golanpiyush.flutter_ytdlp_plugin

// VideoFormat.kt
data class VideoFormat(
    val formatId: String,
    val url: String,
    val ext: String,
    val quality: Int,
    val resolution: String,
    val height: Int,
    val width: Int,
    val fps: Int,
    val filesize: Long,
    val tbr: Int,  // total bitrate
    val vbr: Int,  // video bitrate
    val abr: Int,  // audio bitrate
    val acodec: String,
    val vcodec: String,
    val formatNote: String,
    val protocol: String
)

// AudioFormat.kt
data class AudioFormat(
    val formatId: String,
    val url: String,
    val ext: String,
    val abr: Int,  // audio bitrate
    val acodec: String,
    val formatNote: String,
    val protocol: String
)

// VideoInfo.kt
data class VideoInfo(
    val videoId: String,
    val title: String,
    val channelName: String?,
    val channelId: String?,
    val duration: Int?,
    val viewCount: Long?,
    val uploadDate: String?,
    val thumbnail: String?,
    val description: String?,
    val webpageUrl: String?,
    val originalUrl: String?,
    val timestamp: String,
    val videoFormats: List<VideoFormat>?,
    val audioFormats: List<AudioFormat>?
)

// BestFormatUrls.kt
data class BestFormatUrls(
    val videoId: String,
    val title: String,
    val bestVideoUrl: String?,
    val bestAudioUrl: String?,
    val bestCombinedUrl: String?,
    val formatsAvailable: Int
)

// SearchResult.kt
data class SearchResult(
    val videoId: String,
    val title: String,
    val channelName: String?,
    val channelId: String?,
    val thumbnail: String?,
    val duration: Int?,
    val viewCount: Long?,
    val uploadDate: String?,
    val webpageUrl: String?,
    val searchQuery: String?,
    val searchRank: Int?
)

// RelatedVideo.kt
data class RelatedVideo(
    val videoId: String,
    val title: String,
    val channelName: String?,
    val channelId: String?,
    val thumbnail: String?,
    val duration: Int?,
    val viewCount: Long?,
    val uploadDate: String?,
    val webpageUrl: String?
)

// RandomVideo.kt
data class RandomVideo(
    val videoId: String,
    val title: String,
    val channelName: String?,
    val channelId: String?,
    val thumbnail: String?,
    val duration: Int?,
    val viewCount: Long?,
    val uploadDate: String?,
    val category: String?,
    val searchTerm: String?,
    val webpageUrl: String?
)

// TrendingVideo.kt
data class TrendingVideo(
    val videoId: String,
    val title: String,
    val channelName: String?,
    val channelId: String?,
    val thumbnail: String?,
    val duration: Int?,
    val viewCount: Long?,
    val uploadDate: String?,
    val trendingQuery: String?,
    val isTrending: Boolean,
    val webpageUrl: String?
)
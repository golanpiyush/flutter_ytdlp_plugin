import yt_dlp
import json
import asyncio
from typing import Dict, List, Optional, Generator
import random
import re
from datetime import datetime, timedelta

import yt_dlp
import os

class YouTubeStreamLibrary:
    def __init__(self):
        self.ydl = None
        self.initialized = False
        self.initialize()

    def initialize(self):
        """Initialize YT-DLP with configurable options."""
        try:
            self.ydl_opts = {
                'quiet': True,
                'no_warnings': True,
                'extract_flat': False,
                'format': 'best',
                'ignoreerrors': True,
                'no_check_certificate': True,
                # Cache settings (optional)
                'cachedir': os.path.join(os.getcwd(), 'ytdlp_cache'),
            }
            
            self.ydl = yt_dlp.YoutubeDL(self.ydl_opts)
            self.initialized = True
            print("✅ YT-DLP initialized successfully")
        except Exception as e:
            print(f"❌ YT-DLP initialization failed: {e}")
            self.initialized = False
            raise

    def is_initialized(self):
        return self.initialized

    # All other methods (get_streaming_links, etc.)
    def get_streaming_links(self, title: str, channel_name: str = "") -> Generator[Dict, None, None]:
        """
        Method 1: Get streaming links for video/audio with quality options
        Returns streaming data for the found video
        """
        try:
            # Construct search query - be more specific
            if channel_name:
                search_query = f"{title} {channel_name}"
            else:
                search_query = title
            
            # Clean up the search query
            search_query = self._clean_search_query(search_query)
            
            # First, do a flat search to get video IDs
            flat_search_opts = {
                **self.ydl_opts,
                'extract_flat': True,
                'ignoreerrors': True,
            }
            
            with yt_dlp.YoutubeDL(flat_search_opts) as ydl:
                search_url = f"ytsearch10:{search_query}"
                print(f"Searching for: {search_url}")  # Debug info
                
                search_results = ydl.extract_info(search_url, download=False)
                
                if not search_results or 'entries' not in search_results:
                    yield {"error": "No videos found"}
                    return
                
                # Filter out None entries and default entries
                valid_entries = [
                    entry for entry in search_results['entries'] 
                    if (entry and 
                        entry.get('id') and 
                        entry.get('id') != 'default' and 
                        entry.get('title') and 
                        entry.get('title') != 'default' and
                        entry.get('title') != 'NA')
                ]
                
                if not valid_entries:
                    yield {"error": "No valid videos found"}
                    return
                
                # Find the best match
                best_match = None
                if channel_name:
                    for entry in valid_entries:
                        if (self._is_title_match(title, entry.get('title', '')) and 
                            channel_name.lower() in entry.get('uploader', '').lower()):
                            best_match = entry
                            break
                
                # If no exact match found, take the first valid entry
                if not best_match and valid_entries:
                    best_match = valid_entries[0]
                
                if not best_match:
                    yield {"error": "No matching video found"}
                    return
                
                print(f"Selected video: {best_match.get('title', 'Unknown')}")  # Debug info
                
                # Now get detailed info with formats using the video ID
                video_id = best_match.get('id')
                if video_id:
                    detailed_info = self.get_video_details_with_formats(video_id)
                    if 'error' not in detailed_info:
                        yield detailed_info
                        return
                
                # Fallback: try to get formats from the URL
                try:
                    detailed_opts = {
                        **self.ydl_opts,
                        'extract_flat': False,
                        'format': 'bestvideo+bestaudio/best',  # Updated format selection
                        'ignoreerrors': True,
                    }
                    
                    with yt_dlp.YoutubeDL(detailed_opts) as detail_ydl:
                        video_info = detail_ydl.extract_info(
                            f"https://www.youtube.com/watch?v={video_id}", 
                            download=False
                        )
                        
                        if video_info and video_info.get('id') != 'default':
                            # Process this video info
                            pass
                        else:
                            # Use the basic info we have
                            video_info = best_match
                            
                except Exception as e:
                    print(f"Error getting detailed info: {e}")
                    # Use the basic info we have
                    video_info = best_match
                
                # Process formats if available
                video_formats = []
                audio_formats = []
                
                if 'formats' in video_info and video_info['formats']:
                    for fmt in video_info.get('formats', []):
                        if fmt and fmt.get('url'):
                            format_data = {
                                'format_id': fmt.get('format_id'),
                                'url': fmt.get('url'),
                                'ext': fmt.get('ext'),
                                'quality': fmt.get('quality', 0),
                                'resolution': fmt.get('resolution', 'unknown'),
                                'height': fmt.get('height', 0),
                                'width': fmt.get('width', 0),
                                'fps': fmt.get('fps', 0),
                                'filesize': fmt.get('filesize', 0),
                                'tbr': fmt.get('tbr', 0),  # total bitrate
                                'vbr': fmt.get('vbr', 0),  # video bitrate
                                'abr': fmt.get('abr', 0),  # audio bitrate
                                'acodec': fmt.get('acodec', 'none'),
                                'vcodec': fmt.get('vcodec', 'none'),
                            }
                            
                            if fmt.get('vcodec') and fmt.get('vcodec') != 'none':  # Has video
                                video_formats.append(format_data)
                            elif fmt.get('acodec') and fmt.get('acodec') != 'none':  # Audio only
                                audio_formats.append(format_data)
                
                # Sort by quality - handle None values properly
                video_formats.sort(key=lambda x: (x.get('height') or 0, x.get('tbr') or 0), reverse=True)
                audio_formats.sort(key=lambda x: x.get('abr') or 0, reverse=True)
                
                result = {
                    'video_id': video_info.get('id'),
                    'title': video_info.get('title'),
                    'channel_name': video_info.get('uploader'),
                    'channel_id': video_info.get('channel_id'),
                    'duration': video_info.get('duration'),
                    'view_count': video_info.get('view_count'),
                    'upload_date': video_info.get('upload_date'),
                    'thumbnail': video_info.get('thumbnail'),
                    'description': video_info.get('description', '')[:500],  # Limit description
                    'video_formats': video_formats,
                    'audio_formats': audio_formats,
                    'timestamp': datetime.now().isoformat(),
                    'webpage_url': video_info.get('webpage_url'),
                    'original_url': video_info.get('original_url'),
                }
                
                yield result
                
        except Exception as e:
            yield {"error": f"Error in get_streaming_links: {str(e)}"}
    
    def get_streaming_links_simple(self, title: str, channel_name: str = "") -> Generator[Dict, None, None]:
        """
        Method 1 Alternative: Simple approach to get streaming links
        Uses the working search method and then gets detailed format info
        """
        try:
            # Use the working search method first
            found_video = None
            for video in self.search_videos(title, 1):
                if 'error' not in video:
                    found_video = video
                    break
            
            if not found_video:
                yield {"error": "No video found"}
                return
            
            # Now get detailed format information
            video_id = found_video.get('video_id')
            if video_id:
                detailed_info = self.get_video_details_with_formats(video_id)
                if 'error' not in detailed_info:
                    yield detailed_info
                else:
                    # Return basic info if detailed extraction fails
                    yield {
                        **found_video,
                        'video_formats': [],
                        'audio_formats': [],
                        'timestamp': datetime.now().isoformat(),
                        'note': 'Format extraction failed, basic info only'
                    }
            else:
                yield {"error": "No video ID found"}
                
        except Exception as e:
            yield {"error": f"Error in get_streaming_links_simple: {str(e)}"}

    def get_related_videos(self, title: str, channel_name: str = "", count: int = 200) -> Generator[Dict, None, None]:
        """
        Method 2: Get related videos (minimum 200) with streaming links
        Streams data as soon as each video is processed
        """
        try:
            # First find the main video
            main_video = None
            for result in self.get_streaming_links(title, channel_name):
                if 'error' not in result:
                    main_video = result
                    break
            
            if not main_video:
                yield {"error": "Could not find main video"}
                return
            
            # Get related videos from multiple sources
            related_queries = [
                f"{main_video['channel_name']} videos" if main_video['channel_name'] else f"{title} videos",
                f"{title} similar",
                f"{title} related",
                # Extract keywords from title for better related content
                *[f"{keyword} tutorial" for keyword in self._extract_keywords(title)[:3]]
            ]
            
            found_videos = set()
            video_count = 0
            
            for query in related_queries:
                if video_count >= count:
                    break
                    
                try:
                    search_opts = {**self.ydl_opts, 'extract_flat': True}
                    with yt_dlp.YoutubeDL(search_opts) as ydl:
                        search_results = ydl.extract_info(
                            f"ytsearch50:{query}", 
                            download=False
                        )
                        
                        if search_results and 'entries' in search_results:
                            for entry in search_results['entries']:
                                if video_count >= count:
                                    break
                                    
                                if (entry and 
                                    entry.get('id') and
                                    entry.get('id') not in found_videos and 
                                    entry.get('id') != 'default' and
                                    entry.get('title') and
                                    entry.get('title') != 'default' and
                                    entry.get('title') != 'NA'):
                                    found_videos.add(entry['id'])
                                    
                                    video_result = {
                                        'video_id': entry.get('id'),
                                        'title': entry.get('title'),
                                        'channel_name': entry.get('uploader'),
                                        'channel_id': entry.get('channel_id'),
                                        'thumbnail': entry.get('thumbnail'),
                                        'duration': entry.get('duration'),
                                        'view_count': entry.get('view_count'),
                                        'upload_date': entry.get('upload_date'),
                                        'url': entry.get('url'),
                                        'webpage_url': entry.get('webpage_url'),
                                    }
                                    
                                    yield video_result
                                    video_count += 1
                                    
                except Exception as e:
                    print(f"Error in related videos search: {e}")
                    continue
                    
        except Exception as e:
            yield {"error": f"Error in get_related_videos: {str(e)}"}
    
    def get_random_videos(self, category: Optional[str] = None, count: int = 50) -> Generator[Dict, None, None]:
        """
        Method 3: Get random videos with optional category filter
        """
        try:
            # Random search terms
            base_terms = [
                "tutorial", "how to", "guide", "tips", "tricks", "best",
                "top 10", "review", "explained", "beginner", "advanced"
            ]
            
            category_terms = {
                'coding': ['programming', 'python', 'javascript', 'web development', 'coding tutorial'],
                'teaching': ['education', 'learning', 'course', 'lesson', 'study'],
                'gaming': ['gameplay', 'game review', 'gaming tips', 'walkthrough'],
                'music': ['music video', 'song', 'artist', 'cover', 'live performance'],
                'news': ['news update', 'breaking news', 'current events', 'analysis'],
                'sports': ['highlights', 'match', 'player', 'team', 'sports news'],
                'comedy': ['funny moments', 'comedy sketch', 'stand up', 'humor'],
                'science': ['science explained', 'experiment', 'discovery', 'research'],
                'technology': ['tech review', 'gadget', 'smartphone', 'computer'],
                'cooking': ['recipe', 'cooking tutorial', 'food', 'kitchen tips'],
            }
            
            # Choose search terms based on category
            if category and category.lower() in category_terms:
                search_terms = category_terms[category.lower()] + base_terms
            else:
                search_terms = base_terms
            
            found_videos = set()
            video_count = 0
            
            # Generate random search queries
            for i in range(min(10, len(search_terms))):
                if video_count >= count:
                    break
                    
                search_term = random.choice(search_terms)
                
                try:
                    search_opts = {**self.ydl_opts, 'extract_flat': True}
                    with yt_dlp.YoutubeDL(search_opts) as ydl:
                        search_results = ydl.extract_info(
                            f"ytsearch{min(20, count-video_count)}:{search_term}", 
                            download=False
                        )
                        
                        if search_results and 'entries' in search_results:
                            # Randomize the order
                            entries = [e for e in search_results['entries'] 
                                     if e and e.get('id') != 'default' and e.get('title') != 'default' and e.get('title') != 'NA']
                            random.shuffle(entries)
                            
                            for entry in entries:
                                if video_count >= count:
                                    break
                                    
                                if entry.get('id') not in found_videos:
                                    found_videos.add(entry['id'])
                                    
                                    video_result = {
                                        'video_id': entry.get('id'),
                                        'title': entry.get('title'),
                                        'channel_name': entry.get('uploader'),
                                        'channel_id': entry.get('channel_id'),
                                        'thumbnail': entry.get('thumbnail'),
                                        'duration': entry.get('duration'),
                                        'view_count': entry.get('view_count'),
                                        'upload_date': entry.get('upload_date'),
                                        'category': category,
                                        'search_term': search_term,
                                        'webpage_url': entry.get('webpage_url'),
                                    }
                                    
                                    yield video_result
                                    video_count += 1
                                    
                except Exception as e:
                    print(f"Error in random videos search: {e}")
                    continue
                    
        except Exception as e:
            yield {"error": f"Error in get_random_videos: {str(e)}"}
    
    def get_trending_videos(self, count: int = 50) -> Generator[Dict, None, None]:
        """
        Method 4: Get trending videos
        """
        try:
            trending_queries = [
                "trending 2024",
                "viral videos",
                "popular today",
                "most viewed",
                "trending now",
                "hot videos",
                "latest viral",
                "trending music",
                "trending gaming",
                "trending tech"
            ]
            
            found_videos = set()
            video_count = 0
            
            for query in trending_queries:
                if video_count >= count:
                    break
                    
                try:
                    search_opts = {**self.ydl_opts, 'extract_flat': True}
                    with yt_dlp.YoutubeDL(search_opts) as ydl:
                        search_results = ydl.extract_info(
                            f"ytsearch{min(20, count-video_count)}:{query}", 
                            download=False
                        )
                        
                        if search_results and 'entries' in search_results:
                            for entry in search_results['entries']:
                                if video_count >= count:
                                    break
                                    
                                if (entry and 
                                    entry.get('id') and
                                    entry.get('id') not in found_videos and 
                                    entry.get('id') != 'default' and
                                    entry.get('title') and
                                    entry.get('title') != 'default' and
                                    entry.get('title') != 'NA'):
                                    found_videos.add(entry['id'])
                                    
                                    video_result = {
                                        'video_id': entry.get('id'),
                                        'title': entry.get('title'),
                                        'channel_name': entry.get('uploader'),
                                        'channel_id': entry.get('channel_id'),
                                        'thumbnail': entry.get('thumbnail'),
                                        'duration': entry.get('duration'),
                                        'view_count': entry.get('view_count'),
                                        'upload_date': entry.get('upload_date'),
                                        'trending_query': query,
                                        'is_trending': True,
                                        'webpage_url': entry.get('webpage_url'),
                                    }
                                    
                                    yield video_result
                                    video_count += 1
                                    
                except Exception as e:
                    print(f"Error in trending videos search: {e}")
                    continue
                    
        except Exception as e:
            yield {"error": f"Error in get_trending_videos: {str(e)}"}
    
    def search_videos(self, query: str, max_results: int = 25) -> Generator[Dict, None, None]:
        """
        Method 5: Search for videos (up to 25 results)
        """
        try:
            # Clean the search query
            clean_query = self._clean_search_query(query)
            
            search_opts = {**self.ydl_opts, 'extract_flat': True}
            with yt_dlp.YoutubeDL(search_opts) as ydl:
                search_results = ydl.extract_info(
                    f"ytsearch{max_results}:{clean_query}", 
                    download=False
                )
                
                if not search_results or 'entries' not in search_results:
                    yield {"error": "No search results found"}
                    return
                
                valid_entries = [
                    entry for entry in search_results['entries'] 
                    if entry and entry.get('id') != 'default' and entry.get('title') != 'default' and entry.get('title') != 'NA'
                ]
                
                for i, entry in enumerate(valid_entries[:max_results]):
                    video_result = {
                        'video_id': entry.get('id'),
                        'title': entry.get('title'),
                        'channel_name': entry.get('uploader'),
                        'channel_id': entry.get('channel_id'),
                        'thumbnail': entry.get('thumbnail'),
                        'duration': entry.get('duration'),
                        'view_count': entry.get('view_count'),
                        'upload_date': entry.get('upload_date'),
                        'search_query': query,
                        'search_rank': i + 1,
                        'webpage_url': entry.get('webpage_url'),
                    }
                    
                    yield video_result
                        
        except Exception as e:
            yield {"error": f"Error in search_videos: {str(e)}"}
    
    def get_video_details_with_formats(self, video_id: str) -> Dict:
        """
        Get detailed video information including all available formats
        """
        try:
            url = f"https://www.youtube.com/watch?v={video_id}"
            
            search_opts = {
                **self.ydl_opts,
                'extract_flat': False,
                'format': 'bestvideo+bestaudio/best',  # Updated format selection
                'listformats': False,
            }
            
            with yt_dlp.YoutubeDL(search_opts) as ydl:
                video_info = ydl.extract_info(url, download=False)
                
                if not video_info:
                    return {"error": "Failed to extract video info"}
                
                # Process formats
                video_formats = []
                audio_formats = []
                
                formats = video_info.get('formats', [])
                if formats:
                    for fmt in formats:
                        if fmt and fmt.get('url'):
                            # Handle None values properly
                            format_data = {
                                'format_id': fmt.get('format_id', ''),
                                'url': fmt.get('url', ''),
                                'ext': fmt.get('ext', ''),
                                'quality': fmt.get('quality') or 0,
                                'resolution': fmt.get('resolution') or 'unknown',
                                'height': fmt.get('height') or 0,
                                'width': fmt.get('width') or 0,
                                'fps': fmt.get('fps') or 0,
                                'filesize': fmt.get('filesize') or 0,
                                'tbr': fmt.get('tbr') or 0,
                                'vbr': fmt.get('vbr') or 0,
                                'abr': fmt.get('abr') or 0,
                                'acodec': fmt.get('acodec') or 'none',
                                'vcodec': fmt.get('vcodec') or 'none',
                                'format_note': fmt.get('format_note', ''),
                                'protocol': fmt.get('protocol', ''),
                            }
                            
                            # Categorize as video or audio
                            if fmt.get('vcodec') and fmt.get('vcodec') != 'none':
                                video_formats.append(format_data)
                            elif fmt.get('acodec') and fmt.get('acodec') != 'none':
                                audio_formats.append(format_data)
                
                # Sort by quality - handle None values properly
                video_formats.sort(key=lambda x: (x.get('height') or 0, x.get('tbr') or 0), reverse=True)
                audio_formats.sort(key=lambda x: x.get('abr') or 0, reverse=True)
                
                return {
                    'video_id': video_info.get('id'),
                    'title': video_info.get('title'),
                    'channel_name': video_info.get('uploader'),
                    'channel_id': video_info.get('channel_id'),
                    'duration': video_info.get('duration'),
                    'view_count': video_info.get('view_count'),
                    'upload_date': video_info.get('upload_date'),
                    'thumbnail': video_info.get('thumbnail'),
                    'description': video_info.get('description', ''),
                    'video_formats': video_formats,
                    'audio_formats': audio_formats,
                    'webpage_url': video_info.get('webpage_url'),
                    'timestamp': datetime.now().isoformat(),
                    'total_formats': len(video_formats) + len(audio_formats)
                }
                
        except Exception as e:
            return {"error": f"Error getting video details: {str(e)}"}
    
    def get_best_format_urls(self, video_id: str) -> Dict:
        """
        Get the best available video and audio format URLs
        """
        try:
            url = f"https://www.youtube.com/watch?v={video_id}"
            
            # Try to get best video + audio combination
            search_opts = {
                **self.ydl_opts,
                'extract_flat': False,
                'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/bestvideo+bestaudio/best[ext=mp4]/best',
                'listformats': False,
            }
            
            with yt_dlp.YoutubeDL(search_opts) as ydl:
                video_info = ydl.extract_info(url, download=False)
                
                if not video_info:
                    return {"error": "Failed to extract video info"}
                
                result = {
                    'video_id': video_info.get('id'),
                    'title': video_info.get('title'),
                    'best_video_url': None,
                    'best_audio_url': None,
                    'best_combined_url': None,
                    'formats_available': 0
                }
                
                # Look for requested formats
                formats = video_info.get('formats', [])
                if formats:
                    result['formats_available'] = len(formats)
                    
                    # Find best video format
                    video_formats = [f for f in formats if f.get('vcodec') and f.get('vcodec') != 'none']
                    if video_formats:
                        best_video = max(video_formats, key=lambda x: (x.get('height') or 0, x.get('tbr') or 0))
                        result['best_video_url'] = best_video.get('url')
                    
                    # Find best audio format
                    audio_formats = [f for f in formats if f.get('acodec') and f.get('acodec') != 'none']
                    if audio_formats:
                        best_audio = max(audio_formats, key=lambda x: x.get('abr') or 0)
                        result['best_audio_url'] = best_audio.get('url')
                    
                    # Find best combined format
                    combined_formats = [f for f in formats if f.get('vcodec') and f.get('vcodec') != 'none' and f.get('acodec') and f.get('acodec') != 'none']
                    if combined_formats:
                        best_combined = max(combined_formats, key=lambda x: (x.get('height') or 0, x.get('tbr') or 0))
                        result['best_combined_url'] = best_combined.get('url')
                
                # If no separate formats found, try the direct URL
                if video_info.get('url'):
                    result['best_combined_url'] = video_info.get('url')
                
                return result
                
        except Exception as e:
            return {"error": f"Error getting best format URLs: {str(e)}"}
    
    def _clean_search_query(self, query: str) -> str:
        """Clean and optimize search query"""
        # Remove special characters that might interfere
        clean_query = re.sub(r'[^\w\s-]', '', query)
        # Remove extra whitespace
        clean_query = ' '.join(clean_query.split())
        return clean_query
    
    def _is_title_match(self, search_title: str, video_title: str) -> bool:
        """Helper method to check if titles match"""
        if not search_title or not video_title:
            return False
            
        search_words = set(search_title.lower().split())
        video_words = set(video_title.lower().split())
        
        # Check if at least 60% of search words are in video title
        common_words = search_words.intersection(video_words)
        return len(common_words) / len(search_words) >= 0.6
    
    def _extract_keywords(self, title: str) -> List[str]:
        """Extract keywords from title for better related content"""
        # Remove common words
        stop_words = {
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for', 
            'of', 'with', 'by', 'is', 'are', 'was', 'were', 'be', 'been', 'being',
            'have', 'has', 'had', 'do', 'does', 'did', 'will', 'would', 'could',
            'should', 'may', 'might', 'must', 'can', 'this', 'that', 'these', 'those',
            'how', 'what', 'where', 'when', 'why', 'who', 'which'
        }
        
        words = re.findall(r'\b[a-zA-Z]+\b', title.lower())
        keywords = [word for word in words if word not in stop_words and len(word) > 2]
        
        return keywords[:5]  # Return top 5 keywords

# Usage example and testing
# if __name__ == "__main__":
#     library = YouTubeStreamLibrary()
    
#     # Test Method 1 - Simple version
#     print("Testing Method 1 - Get Streaming Links (Simple):")
#     try:
#         count = 0
#         for result in library.get_streaming_links_simple("Python programming tutorial"):
#             if 'error' in result:
#                 print(f"Error: {result['error']}")
#             else:
#                 print(f"Found: {result.get('title', 'Unknown Title')}")
#                 print(f"Channel: {result.get('channel_name', 'Unknown Channel')}")
#                 print(f"Duration: {result.get('duration', 'Unknown')} seconds")
#                 print(f"Video formats: {len(result.get('video_formats', []))}")
#                 print(f"Audio formats: {len(result.get('audio_formats', []))}")
#             count += 1
#             if count >= 1:  # Just test first result
#                 break
#     except Exception as e:
#         print(f"Exception in Method 1: {e}")
    
#     print("\n" + "="*50 + "\n")
    
#     # Test Method 5
#     print("Testing Method 5 - Search Videos:")
#     try:
#         count = 0
#         for result in library.search_videos("Python tutorial", 3):
#             if 'error' in result:
#                 print(f"Error: {result['error']}")
#             else:
#                 print(f"Video {count + 1}: {result.get('title', 'Unknown')}")
#                 print(f"Channel: {result.get('channel_name', 'Unknown')}")
#                 print(f"Duration: {result.get('duration', 'Unknown')} seconds")
#             count += 1
#             if count >= 3:
#                 break
#     except Exception as e:
#         print(f"Exception in Method 5: {e}")
    
#     print("\n" + "="*50 + "\n")
    
    # Test getting detailed formats for a specific video
    # print("Testing Get Video Details with Formats:")
    # try:
    #     # Get a video ID first
    #     video_id = None
    #     for result in library.search_videos("Python tutorial", 1):
    #         if 'error' not in result:
    #             video_id = result.get('video_id')
    #             break
        
    #     if video_id:
    #         print(f"Getting detailed info for video ID: {video_id}")
    #         detailed_info = library.get_video_details_with_formats(video_id)
    #         if 'error' not in detailed_info:
    #             print(f"Title: {detailed_info.get('title', 'Unknown')}")
    #             print(f"Video formats available: {len(detailed_info.get('video_formats', []))}")
    #             print(f"Audio formats available: {len(detailed_info.get('audio_formats', []))}")
                
    #             # Show some format examples
    #             if detailed_info.get('video_formats'):
    #                 print("Top 3 video formats:")
    #                 for i, fmt in enumerate(detailed_info['video_formats'][:3]):
    #                     print(f"  {i+1}. {fmt.get('resolution', 'Unknown')} - {fmt.get('ext', 'Unknown')} - {fmt.get('tbr', 'Unknown')} kbps")
                
    #             if detailed_info.get('audio_formats'):
    #                 print("Top 3 audio formats:")
    #                 for i, fmt in enumerate(detailed_info['audio_formats'][:3]):
    #                     print(f"  {i+1}. {fmt.get('abr', 'Unknown')} kbps - {fmt.get('ext', 'Unknown')}")
    #         else:
    #             print(f"Error getting details: {detailed_info.get('error', 'Unknown error')}")
    #     else:
    #         print("No video ID found for detailed testing")
    # except Exception as e:
    #     print(f"Exception in detailed format testing: {e}")
    
    # print("\n" + "="*50 + "\n")
    
    # # Test Method 3
    # print("Testing Method 3 - Random Videos:")
    # try:
    #     count = 0
    #     for result in library.get_random_videos("coding", 3):
    #         if 'error' in result:
    #             print(f"Error: {result['error']}")
    #         else:
    #             print(f"Random Video {count + 1}: {result.get('title', 'Unknown')}")
    #             print(f"Channel: {result.get('channel_name', 'Unknown')}")
    #             print(f"Search term: {result.get('search_term', 'Unknown')}")
    #         count += 1
    #         if count >= 3:
    #             break
    # except Exception as e:
    #     print(f"Exception in Method 3: {e}")
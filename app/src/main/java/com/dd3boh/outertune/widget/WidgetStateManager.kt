/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class WidgetStateManager private constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "WidgetStateManager"
        private const val PREFS_NAME = "widget_state"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST = "artist"
        private const val KEY_ALBUM_ART_URI = "album_art_uri"
        private const val KEY_IS_PLAYING = "is_playing"
        private const val ALBUM_ART_FILENAME = "widget_album_art.png"
        
        @Volatile
        private var instance: WidgetStateManager? = null
        
        fun getInstance(context: Context): WidgetStateManager {
            return instance ?: synchronized(this) {
                instance ?: WidgetStateManager(context.applicationContext).also { instance = it }
            }
        }
        
        /**
         * Reset the singleton instance. Only used for testing.
         */
        @Suppress("unused")
        internal fun resetInstance() {
            instance = null
        }
        
        fun updateState(context: Context, title: String?, artist: String?, albumArtUri: String?, isPlaying: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_TITLE, title ?: "No track playing")
                putString(KEY_ARTIST, artist ?: "OuterTune")
                putString(KEY_ALBUM_ART_URI, albumArtUri)
                putBoolean(KEY_IS_PLAYING, isPlaying)
                apply()
            }
            Log.d(TAG, "State updated: title=$title, artist=$artist, isPlaying=$isPlaying, artUri=$albumArtUri")
        }
        
        /**
         * Get the file path for the cached album art.
         */
        fun getAlbumArtFile(context: Context): File {
            return File(context.cacheDir, ALBUM_ART_FILENAME)
        }
    }
    
    fun getCurrentState(): WidgetState {
        return WidgetState(
            title = prefs.getString(KEY_TITLE, "No track playing") ?: "No track playing",
            artist = prefs.getString(KEY_ARTIST, "OuterTune") ?: "OuterTune",
            albumArtUri = prefs.getString(KEY_ALBUM_ART_URI, null),
            isPlaying = prefs.getBoolean(KEY_IS_PLAYING, false)
        )
    }
    
    /**
     * Load album art bitmap from the given URI and save it to cache for widget use.
     * This should be called from a coroutine context.
     */
    suspend fun loadAndCacheAlbumArt(uri: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (uri.isNullOrEmpty()) {
            Log.d(TAG, "No album art URI provided")
            return@withContext null
        }
        
        try {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false) // Required for software bitmap manipulation
                .size(256, 256) // Reasonable size for widget
                .build()
            
            val result = context.imageLoader.execute(request)
            
            if (result is ErrorResult) {
                Log.w(TAG, "Failed to load album art: ${result.throwable.message}")
                return@withContext null
            }
            
            val bitmap = result.image?.toBitmap()
            if (bitmap != null) {
                // Save to cache file for widget to use
                saveBitmapToCache(bitmap)
                Log.d(TAG, "Album art loaded and cached successfully")
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading album art: ${e.message}", e)
            null
        }
    }
    
    /**
     * Save a bitmap to the cache file for the widget to use.
     */
    private fun saveBitmapToCache(bitmap: Bitmap) {
        try {
            val file = getAlbumArtFile(context)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            Log.d(TAG, "Album art saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save album art to cache: ${e.message}", e)
        }
    }
    
    /**
     * Get the cached album art bitmap if it exists.
     */
    fun getCachedAlbumArt(): Bitmap? {
        val file = getAlbumArtFile(context)
        return if (file.exists()) {
            try {
                BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read cached album art: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }
}

data class WidgetState(
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val isPlaying: Boolean
)

/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

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
            Log.d(TAG, "State updated: title=$title, artist=$artist, isPlaying=$isPlaying")
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
}

data class WidgetState(
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val isPlaying: Boolean
)

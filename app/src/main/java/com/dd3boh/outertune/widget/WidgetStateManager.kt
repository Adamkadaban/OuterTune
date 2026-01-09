/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dd3boh.outertune.playback.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Manages the state and updates for the music player widget
 */
class WidgetStateManager private constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaController: MediaController? = null
    private var playerListener: Player.Listener? = null
    private var albumArtBitmap: Bitmap? = null
    
    companion object {
        private const val TAG = "WidgetStateManager"
        
        @Volatile
        private var instance: WidgetStateManager? = null
        
        fun getInstance(context: Context): WidgetStateManager {
            return instance ?: synchronized(this) {
                instance ?: WidgetStateManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Initialize the widget state manager and connect to MusicService.
     * This should only be called from an Activity or Service context, not from a BroadcastReceiver.
     */
    suspend fun initialize() {
        if (mediaController?.isConnected == true) return
        
        try {
            val controller = withTimeoutOrNull(3000L) {
                getMediaController(context)
            } ?: return
            
            mediaController = controller
            Log.d(TAG, "MediaController connected successfully")
            
            // Listen for playback state changes
            playerListener = object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                    updateWidget()
                }
                
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    Log.d(TAG, "onMediaMetadataChanged: ${mediaMetadata.title}")
                    updateWidget()
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                    updateWidget()
                }
            }
            
            controller.addListener(playerListener!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaController: ${e.message}", e)
        }
    }
    
    /**
     * Try to initialize, but don't throw if it fails (e.g., when called from a restricted context).
     * This is safe to call from widget updates triggered by broadcasts.
     */
    fun tryInitialize() {
        // Don't try to connect from here - just check if we have an existing connection
        // The actual connection should only happen from Activity/Service contexts
        val controller = mediaController
        if (controller != null && !controller.isConnected) {
            Log.d(TAG, "Controller disconnected, clearing reference")
            mediaController = null
            playerListener = null
        }
    }
    
    /**
     * Refresh the state by reconnecting to the MediaController if needed.
     * WARNING: This should only be called from an Activity or Service context,
     * not from a BroadcastReceiver (which cannot bind to services).
     */
    suspend fun refreshState() {
        // Check if controller is still connected
        val controller = mediaController
        if (controller == null || !controller.isConnected) {
            Log.d(TAG, "Controller disconnected, reinitializing...")
            mediaController = null
            playerListener = null
            initialize()
        }
    }
    
    /**
     * Get current playback state
     */
    fun getCurrentState(): WidgetState {
        val controller = mediaController
        
        return if (controller != null) {
            val metadata = controller.mediaMetadata
            WidgetState(
                title = metadata?.title?.toString() ?: "No track playing",
                artist = metadata?.artist?.toString() ?: "OuterTune",
                albumArtUri = metadata?.artworkUri?.toString(),
                isPlaying = controller.isPlaying
            )
        } else {
            WidgetState(
                title = "No track playing",
                artist = "OuterTune",
                albumArtUri = null,
                isPlaying = false
            )
        }
    }
    
    /**
     * Get the MediaController instance for direct control operations
     * Returns null if not initialized
     */
    fun getController(): MediaController? = mediaController
    
    /**
     * Update all widget instances
     */
    private fun updateWidget() {
        scope.launch {
            try {
                // Send explicit broadcast to update widgets
                val intent = android.content.Intent(WidgetUpdateReceiver.ACTION_UPDATE_WIDGET).apply {
                    setClass(context, WidgetUpdateReceiver::class.java)
                }
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Get or create a MediaController connected to the MusicService
     */
    private suspend fun getMediaController(context: Context): MediaController? {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        
        return suspendCancellableCoroutine { continuation ->
            val controllerFuture: ListenableFuture<MediaController> =
                MediaController.Builder(context, sessionToken).buildAsync()
            
            controllerFuture.addListener(
                {
                    try {
                        continuation.resume(controllerFuture.get())
                    } catch (e: Exception) {
                        continuation.resume(null)
                    }
                },
                MoreExecutors.directExecutor()
            )
            
            continuation.invokeOnCancellation {
                controllerFuture.cancel(true)
            }
        }
    }
    
    /**
     * Release resources
     * Note: The singleton instance persists for the app lifetime. Resources are 
     * automatically released when the app process terminates. This method is 
     * provided for explicit cleanup if needed, but in practice, maintaining the
     * connection is beneficial for responsive widget updates.
     */
    fun release() {
        playerListener?.let { mediaController?.removeListener(it) }
        mediaController?.release()
        mediaController = null
        playerListener = null
        scope.cancel()
    }
}

/**
 * Data class representing the widget state
 */
data class WidgetState(
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val isPlaying: Boolean
)

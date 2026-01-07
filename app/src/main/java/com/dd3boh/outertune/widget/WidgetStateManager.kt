/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.ComponentName
import android.content.Context
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
    
    companion object {
        @Volatile
        private var instance: WidgetStateManager? = null
        
        fun getInstance(context: Context): WidgetStateManager {
            return instance ?: synchronized(this) {
                instance ?: WidgetStateManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Initialize the widget state manager and connect to MusicService
     */
    suspend fun initialize() {
        if (mediaController != null) return
        
        try {
            val controller = getMediaController(context) ?: return
            mediaController = controller
            
            // Listen for playback state changes
            playerListener = object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateWidget()
                }
                
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    updateWidget()
                }
            }
            
            controller.addListener(playerListener!!)
        } catch (e: Exception) {
            e.printStackTrace()
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
     * Update all widget instances
     */
    private fun updateWidget() {
        scope.launch {
            try {
                // Send broadcast to update widgets
                val intent = android.content.Intent(WidgetUpdateReceiver.ACTION_UPDATE_WIDGET)
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

/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dd3boh.outertune.MainActivity
import com.dd3boh.outertune.playback.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Callback to handle playback actions from the widget
 */
class PlaybackActionCallback : ActionCallback {
    
    companion object {
        private const val TAG = "PlaybackActionCallback"
        val ACTION_KEY = ActionParameters.Key<String>("action")
        const val ACTION_PLAY_PAUSE = "play_pause"
        const val ACTION_PREVIOUS = "previous"
        const val ACTION_NEXT = "next"
    }
    
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val action = parameters[ACTION_KEY] ?: return
        Log.d(TAG, "Widget action received: $action")
        
        // Get controller with timeout - if it takes too long, the service might not be running
        val controller = withTimeoutOrNull(3000L) {
            getMediaController(context)
        }
        
        if (controller == null) {
            Log.w(TAG, "Could not get MediaController, launching app instead")
            launchApp(context)
            return
        }
        
        // Check if there's any media to control
        if (controller.mediaItemCount == 0) {
            Log.w(TAG, "No media items in player, launching app")
            launchApp(context)
            return
        }
        
        try {
            withContext(Dispatchers.Main) {
                when (action) {
                    ACTION_PLAY_PAUSE -> {
                        if (controller.isPlaying) {
                            Log.d(TAG, "Pausing playback")
                            controller.pause()
                        } else {
                            Log.d(TAG, "Starting playback")
                            controller.play()
                        }
                    }
                    ACTION_PREVIOUS -> {
                        Log.d(TAG, "Seeking to previous track")
                        controller.seekToPrevious()
                    }
                    ACTION_NEXT -> {
                        Log.d(TAG, "Seeking to next track")
                        controller.seekToNext()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing playback action: ${e.message}", e)
        }
        
        // Update widget after action
        MusicPlayerWidget().update(context, glanceId)
    }
    
    /**
     * Launch the main app when no media is available
     */
    private fun launchApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
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
}

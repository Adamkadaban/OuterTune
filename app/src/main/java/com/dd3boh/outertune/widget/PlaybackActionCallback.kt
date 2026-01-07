/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.ComponentName
import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dd3boh.outertune.playback.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Callback to handle playback actions from the widget
 */
class PlaybackActionCallback : ActionCallback {
    
    companion object {
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
        
        // Use the centralized WidgetStateManager's controller if available
        val stateManager = WidgetStateManager.getInstance(context)
        val controller = stateManager.getController() ?: getMediaController(context) ?: return
        
        try {
            when (action) {
                ACTION_PLAY_PAUSE -> {
                    if (controller.isPlaying) {
                        controller.pause()
                    } else {
                        controller.play()
                    }
                }
                ACTION_PREVIOUS -> controller.seekToPrevious()
                ACTION_NEXT -> controller.seekToNext()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Update widget after action
        MusicPlayerWidget().update(context, glanceId)
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

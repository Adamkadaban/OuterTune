/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.dd3boh.outertune.playback.MusicService

/**
 * Callback to handle playback actions from the widget.
 * 
 * Uses startService to send commands to MusicService, which is the most
 * reliable way to control playback from a widget context.
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
        
        val serviceAction = when (action) {
            ACTION_PLAY_PAUSE -> MusicService.ACTION_WIDGET_PLAY_PAUSE
            ACTION_PREVIOUS -> MusicService.ACTION_WIDGET_PREVIOUS
            ACTION_NEXT -> MusicService.ACTION_WIDGET_NEXT
            else -> return
        }
        
        sendCommandToService(context, serviceAction)
    }
    
    /**
     * Send a command to MusicService via startService.
     * This is the most reliable way to control playback from a widget.
     */
    private fun sendCommandToService(context: Context, action: String) {
        val intent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        
        try {
            // Use startForegroundService on API 26+ since MusicService is a foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Sent command to service: $action")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send command to service", e)
        }
    }
}

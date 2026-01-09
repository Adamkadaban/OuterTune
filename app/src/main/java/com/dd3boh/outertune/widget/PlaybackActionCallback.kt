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
import android.view.KeyEvent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.media3.session.MediaButtonReceiver
import com.dd3boh.outertune.playback.MusicService

/**
 * Callback to handle playback actions from the widget.
 * 
 * Uses media button intents to control playback, which works from any context
 * including BroadcastReceivers and Glance widget callbacks.
 * This avoids the ReceiverCallNotAllowedException that occurs when trying
 * to create a MediaController from restricted contexts.
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
        
        val keyCode = when (action) {
            ACTION_PLAY_PAUSE -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            ACTION_PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            ACTION_NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
            else -> return
        }
        
        sendMediaButtonEvent(context, keyCode)
        Log.d(TAG, "Sent media button event: $keyCode")
    }
    
    /**
     * Send a media button key event to control playback.
     * This uses the standard Android media button mechanism which is handled
     * by MediaButtonReceiver and forwarded to the MediaSession.
     */
    private fun sendMediaButtonEvent(context: Context, keyCode: Int) {
        // Create key events (down and up)
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        
        // Create intent for MediaButtonReceiver
        val component = ComponentName(context, MusicService::class.java)
        
        // Send down event
        val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            setComponent(component)
            putExtra(Intent.EXTRA_KEY_EVENT, downEvent)
        }
        context.sendBroadcast(downIntent)
        
        // Send up event
        val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            setComponent(component)
            putExtra(Intent.EXTRA_KEY_EVENT, upEvent)
        }
        context.sendBroadcast(upIntent)
    }
}

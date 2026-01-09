/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.dd3boh.outertune.playback.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to update the widget when playback state changes.
 * 
 * Note: BroadcastReceivers cannot bind to services, so we cannot create a new
 * MediaController here. Instead, we just trigger a widget update - the widget's
 * provideGlance will handle state retrieval.
 */
class WidgetUpdateReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "WidgetUpdateReceiver"
        const val ACTION_UPDATE_WIDGET = MusicService.ACTION_UPDATE_WIDGET
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        if (intent.action == ACTION_UPDATE_WIDGET) {
            // Use goAsync() for broadcast receiver to allow async work
            val pendingResult = goAsync()
            
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            scope.launch {
                try {
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(MusicPlayerWidget::class.java)
                    Log.d(TAG, "Updating ${glanceIds.size} widget instances")
                    glanceIds.forEach { glanceId ->
                        MusicPlayerWidget().update(context, glanceId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating widgets: ${e.message}", e)
                } finally {
                    // Finish async work
                    pendingResult.finish()
                }
            }
        }
    }
}

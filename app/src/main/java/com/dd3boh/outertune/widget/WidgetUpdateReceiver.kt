/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to update the widget when playback state changes
 */
class WidgetUpdateReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_UPDATE_WIDGET = "com.dd3boh.outertune.widget.UPDATE"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_WIDGET) {
            // Use goAsync() for broadcast receiver to allow async work
            val pendingResult = goAsync()
            
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            scope.launch {
                try {
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(MusicPlayerWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        MusicPlayerWidget().update(context, glanceId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // Finish async work
                    pendingResult.finish()
                }
            }
        }
    }
}

/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Music player widget that displays currently playing track and playback controls
 */
class MusicPlayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Initialize state manager
        withContext(Dispatchers.Main) {
            WidgetStateManager.getInstance(context).initialize()
        }
        
        provideContent {
            MusicPlayerWidgetContent()
        }
    }
    
    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        // Note: WidgetStateManager is a singleton that persists for the app lifetime
        // This provides responsive updates when widgets are active
        // Resources are automatically cleaned up when the app process terminates
    }
}

/**
 * Receiver for the music player widget
 */
class MusicPlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicPlayerWidget()
}

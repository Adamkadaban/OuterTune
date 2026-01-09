/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent

/**
 * Music player widget that displays currently playing track and playback controls.
 * 
 * The widget reads playback state from SharedPreferences (written by MusicService)
 * and uses media button intents for playback control. This avoids binding to services
 * from restricted contexts.
 */
class MusicPlayerWidget : GlanceAppWidget() {
    
    companion object {
        private const val TAG = "MusicPlayerWidget"
    }
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for widget $id")
        
        provideContent {
            MusicPlayerWidgetContent()
        }
    }
    
    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        Log.d(TAG, "Widget deleted: $glanceId")
    }
}

/**
 * Receiver for the music player widget
 */
class MusicPlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicPlayerWidget()
}

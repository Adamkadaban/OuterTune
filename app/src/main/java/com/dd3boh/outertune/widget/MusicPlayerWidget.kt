/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition

/**
 * Music player widget that displays currently playing track and playback controls.
 * 
 * Uses Glance's PreferencesGlanceStateDefinition to properly trigger recomposition
 * when state changes.
 */
class MusicPlayerWidget : GlanceAppWidget() {
    
    companion object {
        private const val TAG = "MusicPlayerWidget"
        
        // Glance state keys
        val TITLE_KEY = stringPreferencesKey("widget_title")
        val ARTIST_KEY = stringPreferencesKey("widget_artist")
        val ALBUM_ART_URI_KEY = stringPreferencesKey("widget_album_art_uri")
        val IS_PLAYING_KEY = booleanPreferencesKey("widget_is_playing")
    }
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
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

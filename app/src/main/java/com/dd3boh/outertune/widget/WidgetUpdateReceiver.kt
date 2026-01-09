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
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.dd3boh.outertune.playback.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to update the widget when playback state changes.
 * 
 * Uses Glance's updateAppWidgetState to properly trigger recomposition when
 * the playback state changes. Also loads and caches album art for display.
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
                    // Get current state from SharedPreferences
                    val stateManager = WidgetStateManager.getInstance(context)
                    val state = stateManager.getCurrentState()
                    Log.d(TAG, "Current state: title=${state.title}, isPlaying=${state.isPlaying}, artUri=${state.albumArtUri}")
                    
                    // Load and cache album art (runs on IO dispatcher internally)
                    stateManager.loadAndCacheAlbumArt(state.albumArtUri)
                    
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(MusicPlayerWidget::class.java)
                    Log.d(TAG, "Updating ${glanceIds.size} widget instances")
                    
                    glanceIds.forEach { glanceId ->
                        // Update Glance state to trigger recomposition
                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                            prefs.toMutablePreferences().apply {
                                this[MusicPlayerWidget.TITLE_KEY] = state.title
                                this[MusicPlayerWidget.ARTIST_KEY] = state.artist
                                state.albumArtUri?.let { this[MusicPlayerWidget.ALBUM_ART_URI_KEY] = it }
                                    ?: this.remove(MusicPlayerWidget.ALBUM_ART_URI_KEY)
                                this[MusicPlayerWidget.IS_PLAYING_KEY] = state.isPlaying
                                // Add a timestamp to force update even if values are the same
                                this[MusicPlayerWidget.UPDATE_TIMESTAMP_KEY] = System.currentTimeMillis()
                            }
                        }
                        // Now update the widget UI
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

/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.text.TextAlign
import com.dd3boh.outertune.MainActivity
import com.dd3boh.outertune.R

/**
 * Widget content composable that displays the music player UI
 * Compact horizontal layout similar to Spotify/YouTube Music widgets
 */
@Composable
fun MusicPlayerWidgetContent() {
    val context = LocalContext.current
    val widgetState = getWidgetState(context)
    
    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art - compact square on the left
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .cornerRadius(8.dp)
                        .background(GlanceTheme.colors.surfaceVariant)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.launcher_foreground),
                        contentDescription = "Album art",
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                // Song info - title and artist in center, takes remaining space
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Song title
                    Text(
                        text = widgetState.title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1
                    )
                    
                    // Artist name
                    Text(
                        text = widgetState.artist,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                // Playback controls - compact row on the right
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.skip_previous),
                        contentDescription = "Previous",
                        onClick = actionRunCallback<PlaybackActionCallback>(
                            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_PREVIOUS)
                        ),
                        backgroundColor = null,
                        contentColor = GlanceTheme.colors.onSurfaceVariant,
                        modifier = GlanceModifier.size(36.dp)
                    )
                    
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    
                    // Play/Pause button - larger and more prominent
                    CircleIconButton(
                        imageProvider = ImageProvider(
                            if (widgetState.isPlaying) R.drawable.pause else R.drawable.play
                        ),
                        contentDescription = if (widgetState.isPlaying) "Pause" else "Play",
                        onClick = actionRunCallback<PlaybackActionCallback>(
                            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_PLAY_PAUSE)
                        ),
                        backgroundColor = GlanceTheme.colors.primary,
                        contentColor = GlanceTheme.colors.onPrimary,
                        modifier = GlanceModifier.size(40.dp)
                    )
                    
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    
                    // Next button
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.skip_next),
                        contentDescription = "Next",
                        onClick = actionRunCallback<PlaybackActionCallback>(
                            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_NEXT)
                        ),
                        backgroundColor = null,
                        contentColor = GlanceTheme.colors.onSurfaceVariant,
                        modifier = GlanceModifier.size(36.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get current widget state from Glance state
 * Uses currentState() to read from Glance's DataStore which triggers recomposition
 */
@Composable
fun getWidgetState(context: Context): WidgetState {
    val prefs = currentState<Preferences>()
    
    val title = prefs[MusicPlayerWidget.TITLE_KEY] ?: "No track playing"
    val artist = prefs[MusicPlayerWidget.ARTIST_KEY] ?: "OuterTune"
    val albumArtUri = prefs[MusicPlayerWidget.ALBUM_ART_URI_KEY]
    val isPlaying = prefs[MusicPlayerWidget.IS_PLAYING_KEY] ?: false
    
    android.util.Log.d("MusicPlayerWidget", "getWidgetState: title=$title, artist=$artist, isPlaying=$isPlaying")
    
    return WidgetState(
        title = title,
        artist = artist,
        albumArtUri = albumArtUri,
        isPlaying = isPlaying
    )
}

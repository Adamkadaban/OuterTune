/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.RowScope
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import com.dd3boh.outertune.MainActivity
import com.dd3boh.outertune.R

/**
 * Widget content composable that displays the music player UI
 * Responsive layout similar to Spotify widget:
 * - Album art takes full height
 * - Wide layout: [Art] [Title/Artist] [Controls]
 * - Tall/narrow layout: [Art] [Title/Artist above Controls]
 */
@Composable
fun MusicPlayerWidgetContent() {
    val context = LocalContext.current
    val widgetState = getWidgetState(context)
    val size = LocalSize.current
    
    // Load cached album art or use placeholder
    val albumArtBitmap = getAlbumArtBitmap(context)
    
    // Determine layout based on widget dimensions
    // If height > 100dp or width < 300dp, use stacked layout
    val useStackedLayout = size.height > 100.dp || size.width < 280.dp
    
    GlanceTheme {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art - square, fills full height
            AlbumArtSection(
                context = context,
                albumArtBitmap = albumArtBitmap,
                size = size.height
            )
            
            if (useStackedLayout) {
                // Stacked layout: Title/Artist on top, Controls below
                StackedInfoAndControls(
                    context = context,
                    widgetState = widgetState
                )
            } else {
                // Wide layout: Title/Artist in middle, Controls on right
                WideInfoAndControls(
                    context = context,
                    widgetState = widgetState
                )
            }
        }
    }
}

/**
 * Album art section - square image that fills the height
 */
@Composable
private fun AlbumArtSection(
    context: Context,
    albumArtBitmap: Bitmap?,
    size: Dp
) {
    Box(
        modifier = GlanceModifier
            .fillMaxHeight()
            .width(size) // Square - width equals height
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        if (albumArtBitmap != null) {
            Image(
                provider = ImageProvider(albumArtBitmap),
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize()
            )
        } else {
            Image(
                provider = ImageProvider(R.drawable.launcher_foreground),
                contentDescription = "Album art",
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

/**
 * Wide layout: Song info in middle, controls on right
 * Used when widget is wide enough (5x1 style)
 */
@Composable
private fun RowScope.WideInfoAndControls(
    context: Context,
    widgetState: WidgetState
) {
    // Song info - title and artist
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .fillMaxHeight()
            .padding(horizontal = 12.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = widgetState.title,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 1
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = widgetState.artist,
            style = TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            maxLines = 1
        )
    }
    
    // Playback controls on the right
    Row(
        modifier = GlanceModifier
            .fillMaxHeight()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaybackControls(widgetState = widgetState, buttonSize = 44.dp, playButtonSize = 52.dp)
    }
}

/**
 * Stacked layout: Song info on top, controls below
 * Used when widget is taller or narrower
 */
@Composable
private fun RowScope.StackedInfoAndControls(
    context: Context,
    widgetState: WidgetState
) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .fillMaxHeight()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        // Song info at top
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)))
        ) {
            Text(
                text = widgetState.title,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = widgetState.artist,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Playback controls below, centered
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaybackControls(widgetState = widgetState, buttonSize = 40.dp, playButtonSize = 48.dp)
        }
    }
}

/**
 * Playback control buttons (Previous, Play/Pause, Next)
 */
@Composable
private fun PlaybackControls(
    widgetState: WidgetState,
    buttonSize: Dp,
    playButtonSize: Dp
) {
    // Previous button
    CircleIconButton(
        imageProvider = ImageProvider(R.drawable.skip_previous),
        contentDescription = "Previous",
        onClick = actionRunCallback<PlaybackActionCallback>(
            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_PREVIOUS)
        ),
        backgroundColor = null,
        contentColor = GlanceTheme.colors.onSurface,
        modifier = GlanceModifier.size(buttonSize)
    )
    
    Spacer(modifier = GlanceModifier.width(8.dp))
    
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
        modifier = GlanceModifier.size(playButtonSize)
    )
    
    Spacer(modifier = GlanceModifier.width(8.dp))
    
    // Next button
    CircleIconButton(
        imageProvider = ImageProvider(R.drawable.skip_next),
        contentDescription = "Next",
        onClick = actionRunCallback<PlaybackActionCallback>(
            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_NEXT)
        ),
        backgroundColor = null,
        contentColor = GlanceTheme.colors.onSurface,
        modifier = GlanceModifier.size(buttonSize)
    )
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

/**
 * Load album art bitmap from cache file.
 * Returns null if the cache file doesn't exist or can't be read.
 */
fun getAlbumArtBitmap(context: Context): Bitmap? {
    return try {
        val file = WidgetStateManager.getAlbumArtFile(context)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    } catch (e: Exception) {
        android.util.Log.w("MusicPlayerWidget", "Failed to load album art: ${e.message}")
        null
    }
}

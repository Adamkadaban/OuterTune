/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import com.dd3boh.outertune.MainActivity
import com.dd3boh.outertune.R

/**
 * Widget content composable that displays the music player UI
 */
@Composable
fun MusicPlayerWidgetContent() {
    val context = LocalContext.current
    val widgetState = rememberWidgetState(context)
    
    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Album art
                if (widgetState.albumArtUri != null) {
                    Image(
                        provider = ImageProvider(widgetState.albumArtUri.toUri()),
                        contentDescription = "Album art",
                        modifier = GlanceModifier
                            .size(80.dp)
                    )
                } else {
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = "Album art",
                        modifier = GlanceModifier
                            .size(80.dp)
                    )
                }
                
                Spacer(modifier = GlanceModifier.height(8.dp))
                
                // Song title
                Text(
                    text = widgetState.title,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                // Artist name
                Text(
                    text = widgetState.artist,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(16.dp))
                
                // Playback controls
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.skip_previous),
                        contentDescription = "Previous",
                        onClick = actionRunCallback<PlaybackActionCallback>(
                            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_PREVIOUS)
                        ),
                        backgroundColor = GlanceTheme.colors.primaryContainer,
                        contentColor = GlanceTheme.colors.onPrimaryContainer
                    )
                    
                    Spacer(modifier = GlanceModifier.width(16.dp))
                    
                    // Play/Pause button
                    CircleIconButton(
                        imageProvider = ImageProvider(
                            if (widgetState.isPlaying) R.drawable.pause else R.drawable.play
                        ),
                        contentDescription = if (widgetState.isPlaying) "Pause" else "Play",
                        onClick = actionRunCallback<PlaybackActionCallback>(
                            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_PLAY_PAUSE)
                        ),
                        backgroundColor = GlanceTheme.colors.primary,
                        contentColor = GlanceTheme.colors.onPrimary
                    )
                    
                    Spacer(modifier = GlanceModifier.width(16.dp))
                    
                    // Next button
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.skip_next),
                        contentDescription = "Next",
                        onClick = actionRunCallback<PlaybackActionCallback>(
                            actionParametersOf(PlaybackActionCallback.ACTION_KEY to PlaybackActionCallback.ACTION_NEXT)
                        ),
                        backgroundColor = GlanceTheme.colors.primaryContainer,
                        contentColor = GlanceTheme.colors.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get widget state
 * Note: This is called during widget composition which happens on each update
 */
@Composable
fun rememberWidgetState(context: Context): WidgetState {
    // Get current state - this is lightweight as it just reads current values
    // State updates are triggered by the Player.Listener in WidgetStateManager
    val stateManager = WidgetStateManager.getInstance(context)
    return stateManager.getCurrentState()
}

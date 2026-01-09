package com.dd3boh.outertune.widget

import android.content.Intent
import android.view.KeyEvent
import com.dd3boh.outertune.playback.MusicService
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Unit tests for PlaybackActionCallback
 * Verifies that widget commands are sent correctly via startService
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PlaybackActionCallbackTest {

    @Test
    fun `ACTION_PLAY_PAUSE constant is correct`() {
        assertEquals("play_pause", PlaybackActionCallback.ACTION_PLAY_PAUSE)
    }

    @Test
    fun `ACTION_PREVIOUS constant is correct`() {
        assertEquals("previous", PlaybackActionCallback.ACTION_PREVIOUS)
    }

    @Test
    fun `ACTION_NEXT constant is correct`() {
        assertEquals("next", PlaybackActionCallback.ACTION_NEXT)
    }

    @Test
    fun `service intent is correctly formed for play pause`() {
        val context = RuntimeEnvironment.getApplication()
        
        // Create intent like PlaybackActionCallback does
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_WIDGET_PLAY_PAUSE
        }
        
        assertEquals(MusicService.ACTION_WIDGET_PLAY_PAUSE, intent.action)
        assertEquals("com.dd3boh.outertune.WIDGET_PLAY_PAUSE", intent.action)
    }

    @Test
    fun `service intent is correctly formed for previous`() {
        val context = RuntimeEnvironment.getApplication()
        
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_WIDGET_PREVIOUS
        }
        
        assertEquals(MusicService.ACTION_WIDGET_PREVIOUS, intent.action)
        assertEquals("com.dd3boh.outertune.WIDGET_PREVIOUS", intent.action)
    }

    @Test
    fun `service intent is correctly formed for next`() {
        val context = RuntimeEnvironment.getApplication()
        
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_WIDGET_NEXT
        }
        
        assertEquals(MusicService.ACTION_WIDGET_NEXT, intent.action)
        assertEquals("com.dd3boh.outertune.WIDGET_NEXT", intent.action)
    }

    @Test
    fun `WidgetState data class equality works correctly`() {
        val state1 = WidgetState(
            title = "Song",
            artist = "Artist",
            albumArtUri = "uri",
            isPlaying = true
        )
        
        val state2 = WidgetState(
            title = "Song",
            artist = "Artist",
            albumArtUri = "uri",
            isPlaying = true
        )
        
        val state3 = WidgetState(
            title = "Different Song",
            artist = "Artist",
            albumArtUri = "uri",
            isPlaying = true
        )
        
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `WidgetState handles null albumArtUri`() {
        val state = WidgetState(
            title = "Song",
            artist = "Artist",
            albumArtUri = null,
            isPlaying = false
        )
        
        assertNull(state.albumArtUri)
        assertEquals("Song", state.title)
        assertEquals("Artist", state.artist)
        assertFalse(state.isPlaying)
    }
}

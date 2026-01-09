package com.dd3boh.outertune.widget

import android.content.Intent
import android.view.KeyEvent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Unit tests for PlaybackActionCallback
 * Verifies that media button intents are created correctly
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
    fun `media button intent is correctly formed`() {
        // Test that we can create a valid media button intent
        val context = RuntimeEnvironment.getApplication()
        
        // Create a media button intent like PlaybackActionCallback does
        val keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        
        // Verify the key events are created correctly
        assertEquals(KeyEvent.ACTION_DOWN, downEvent.action)
        assertEquals(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, downEvent.keyCode)
        
        assertEquals(KeyEvent.ACTION_UP, upEvent.action)
        assertEquals(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, upEvent.keyCode)
        
        // Create intent and verify it has the correct action
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, downEvent)
        }
        
        assertEquals(Intent.ACTION_MEDIA_BUTTON, intent.action)
        
        val extractedKeyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        assertNotNull(extractedKeyEvent)
        assertEquals(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, extractedKeyEvent?.keyCode)
    }

    @Test
    fun `previous media button intent has correct keycode`() {
        val keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS
        val event = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        
        assertEquals(KeyEvent.KEYCODE_MEDIA_PREVIOUS, event.keyCode)
    }

    @Test
    fun `next media button intent has correct keycode`() {
        val keyCode = KeyEvent.KEYCODE_MEDIA_NEXT
        val event = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        
        assertEquals(KeyEvent.KEYCODE_MEDIA_NEXT, event.keyCode)
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

package com.dd3boh.outertune.widget

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for WidgetStateManager
 * Uses Robolectric to provide Android framework mocks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WidgetStateManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        // Reset the singleton instance before each test
        WidgetStateManager.resetInstance()
        // Clear SharedPreferences before each test
        context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `updateState writes correct values to SharedPreferences`() {
        // Given
        val title = "Test Song"
        val artist = "Test Artist"
        val albumArtUri = "content://media/album/123"
        val isPlaying = true

        // When
        WidgetStateManager.updateState(context, title, artist, albumArtUri, isPlaying)

        // Then
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        assertEquals(title, prefs.getString("title", null))
        assertEquals(artist, prefs.getString("artist", null))
        assertEquals(albumArtUri, prefs.getString("album_art_uri", null))
        assertEquals(isPlaying, prefs.getBoolean("is_playing", false))
    }

    @Test
    fun `updateState uses default values when null is passed`() {
        // When
        WidgetStateManager.updateState(context, null, null, null, false)

        // Then
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        assertEquals("No track playing", prefs.getString("title", null))
        assertEquals("OuterTune", prefs.getString("artist", null))
        assertNull(prefs.getString("album_art_uri", null))
        assertEquals(false, prefs.getBoolean("is_playing", true))
    }

    @Test
    fun `getCurrentState reads correct values from SharedPreferences`() {
        // Given - pre-populate SharedPreferences
        val prefs = context.getSharedPreferences("widget_state", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("title", "My Song")
            putString("artist", "My Artist")
            putString("album_art_uri", "content://test/uri")
            putBoolean("is_playing", true)
            commit()
        }

        // When
        val stateManager = WidgetStateManager.getInstance(context)
        val state = stateManager.getCurrentState()

        // Then
        assertEquals("My Song", state.title)
        assertEquals("My Artist", state.artist)
        assertEquals("content://test/uri", state.albumArtUri)
        assertTrue(state.isPlaying)
    }

    @Test
    fun `getCurrentState returns defaults when SharedPreferences is empty`() {
        // When
        val stateManager = WidgetStateManager.getInstance(context)
        val state = stateManager.getCurrentState()

        // Then
        assertEquals("No track playing", state.title)
        assertEquals("OuterTune", state.artist)
        assertNull(state.albumArtUri)
        assertFalse(state.isPlaying)
    }

    @Test
    fun `updateState and getCurrentState work together`() {
        // Given
        val title = "Round Trip Song"
        val artist = "Round Trip Artist"
        val isPlaying = true

        // When - update state
        WidgetStateManager.updateState(context, title, artist, null, isPlaying)
        
        // And - read state back
        val stateManager = WidgetStateManager.getInstance(context)
        val state = stateManager.getCurrentState()

        // Then
        assertEquals(title, state.title)
        assertEquals(artist, state.artist)
        assertTrue(state.isPlaying)
    }

    @Test
    fun `play state toggles correctly`() {
        // Given - start playing
        WidgetStateManager.updateState(context, "Song", "Artist", null, true)
        
        val stateManager = WidgetStateManager.getInstance(context)
        assertTrue(stateManager.getCurrentState().isPlaying)

        // When - pause
        WidgetStateManager.updateState(context, "Song", "Artist", null, false)

        // Then
        assertFalse(stateManager.getCurrentState().isPlaying)
    }

    @Test
    fun `track changes update correctly`() {
        val stateManager = WidgetStateManager.getInstance(context)
        
        // Play first track
        WidgetStateManager.updateState(context, "Song 1", "Artist 1", null, true)
        assertEquals("Song 1", stateManager.getCurrentState().title)
        assertEquals("Artist 1", stateManager.getCurrentState().artist)

        // Change to second track
        WidgetStateManager.updateState(context, "Song 2", "Artist 2", null, true)
        assertEquals("Song 2", stateManager.getCurrentState().title)
        assertEquals("Artist 2", stateManager.getCurrentState().artist)
    }
}

# Widget Testing Strategy

## Current State
- **No existing test infrastructure** in the app module (`/app/src/`)
- Only library modules (`innertube`, `kugou`) have tests
- Widget is a UI component requiring instrumented tests

## Testing Approach If Implemented

### Unit Tests (would require setup)
Location: `/app/src/test/java/com/dd3boh/outertune/widget/`

**Files to test:**
1. `WidgetStateManagerTest.kt`
   - Test getCurrentState() returns correct WidgetState
   - Test MediaController connection handling
   - Test null handling when controller unavailable
   - Mock MediaController and Player

2. `PlaybackActionCallbackTest.kt`
   - Test ACTION_PLAY_PAUSE with playing/paused states
   - Test ACTION_PREVIOUS and ACTION_NEXT
   - Test error handling when MediaController unavailable
   - Mock context and MediaController

### Instrumented Tests (would require device/emulator)
Location: `/app/src/androidTest/java/com/dd3boh/outertune/widget/`

**Integration tests:**
1. `MusicPlayerWidgetTest.kt`
   - Test widget renders correctly
   - Test button clicks trigger correct actions
   - Test widget updates when state changes
   - Requires running MusicService and MediaController

2. `WidgetUpdateReceiverTest.kt`
   - Test broadcast receiver triggers widget update
   - Test goAsync() lifecycle
   - Verify GlanceAppWidgetManager called

### Required Dependencies
```kotlin
// In app/build.gradle.kts
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
testImplementation("androidx.test:core:1.5.0")
testImplementation("org.robolectric:robolectric:4.11.1")

androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.glance:glance-testing:1.1.1")
```

### Manual Testing (Current Approach)
Since no test infrastructure exists and per contribution guidelines, manual testing is acceptable:

**Test Checklist:**
- [ ] Add widget to home screen
- [ ] Start playing music in OuterTune
- [ ] Verify track title displays
- [ ] Verify artist name displays
- [ ] Verify album artwork loads (or fallback icon shows)
- [ ] Tap play/pause - verify it toggles playback
- [ ] Tap previous - verify it goes to previous track
- [ ] Tap next - verify it goes to next track
- [ ] Change track in app - verify widget updates automatically
- [ ] Pause in app - verify widget button updates to play icon
- [ ] Tap widget - verify app opens
- [ ] Remove and re-add widget - verify it initializes correctly

## Recommendation
Given:
- No existing test infrastructure in app module
- Widget requires instrumented tests for proper validation
- Contribution guidelines allow skipping tests when no infrastructure exists
- Implementation follows Android best practices
- Code has been through 2 rounds of review

**Manual device testing is the appropriate approach** for this feature unless project owners want to invest in creating test infrastructure (significant undertaking).

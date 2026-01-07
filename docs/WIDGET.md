# Music Player Widget

OuterTune now includes a home screen widget that allows you to control music playback directly from your home screen.

## Features

- **Current Track Display**: Shows the currently playing song title and artist
- **Album Artwork**: Displays the album art for the current track
- **Playback Controls**: 
  - Play/Pause button
  - Skip to previous track
  - Skip to next track
- **Real-time Updates**: Widget automatically updates when tracks change or playback state changes
- **Quick Access**: Tap the widget to open the full OuterTune app

## How to Add the Widget

1. Long-press on your home screen
2. Select "Widgets" from the menu
3. Find "OuterTune" in the widget list
4. Drag the "Music Player" widget to your home screen
5. Position and resize as desired

## Widget Sizes

The widget is resizable and works best with:
- Minimum size: 4x2 cells (250dp x 150dp)
- Recommended size: 4x2 to 5x3 cells
- The widget adapts to both horizontal and vertical resizing

## Technical Details

The widget is built using:
- **Jetpack Glance**: Modern Compose-based widget framework
- **Media3**: Integration with the MusicService for real-time state
- **Material 3**: Consistent design with the main app

## Troubleshooting

### Widget not updating
- Ensure the OuterTune app has proper notification permissions
- Try removing and re-adding the widget
- Check that background app refresh is enabled for OuterTune

### Controls not working
- Make sure a track is loaded in the queue
- Verify that the MusicService is running
- Try opening the main app to initialize the service

### Album artwork not showing
- Artwork requires an active internet connection for online tracks
- Local tracks need properly embedded album art
- Fallback to app icon if artwork is unavailable

## Privacy

The widget only accesses playback information from the MusicService running on your device. No data is transmitted externally.

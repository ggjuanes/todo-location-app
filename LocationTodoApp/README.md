# Location TODO App

A React Native (Expo) mobile app for managing location-based tasks. Create tasks associated with cities/towns and see which ones are available near your current location.

## Prerequisites

- [Node.js](https://nodejs.org/) v14+
- [Expo CLI](https://docs.expo.dev/get-started/installation/) (`npx expo` works without global install)
- iOS Simulator (requires Xcode on macOS) or Android Emulator (requires Android Studio)
- Alternatively, the **Expo Go** app on a physical device

## Setup

1. Install dependencies:

   ```bash
   cd LocationTodoApp
   npm install
   ```

2. Configure the geocoding API key:

   ```bash
   cp .env.example .env
   ```

   Edit `.env` and replace `your_api_key_here` with your API key from [geocode.maps.co](https://geocode.maps.co/).

## Running the App

```bash
npx expo start
```

Then choose a target:

- (Recommended for this project) Scan the **QR code** with Expo Go on a physical device
- Press **`i`** to open in iOS Simulator
- Press **`a`** to open in Android Emulator

### Testing Location on Simulators

Simulators don't have real GPS. To test the "Available Tasks" tab:

- **iOS Simulator**: Features > Location > Custom Location > enter coordinates (e.g., `37.7749, -122.4194` for San Francisco)
- **Android Emulator**: Extended Controls (three dots) > Location > enter coordinates

On a **physical device** via Expo Go, GPS works automatically.

## Project Structure

```
src/
├── components/       # Reusable UI components (TaskItem, TaskForm, EmptyState)
├── context/          # TaskContext (state management with useReducer)
├── hooks/            # useCurrentLocation custom hook
├── navigation/       # Bottom tabs + stack navigators
├── screens/          # AllTasks, AvailableTasks, CreateTask, EditTask
├── services/         # storageService, locationService, geocodingService
└── utils/            # Validation helpers, UUID generation
```

## Environment Variables

| Variable | Description |
|---|---|
| `EXPO_PUBLIC_GEOCODING_API_KEY` | API key for geocode.maps.co reverse geocoding |

After changing `.env`, restart Metro (`npx expo start`) for changes to take effect.

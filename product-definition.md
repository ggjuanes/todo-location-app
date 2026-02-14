# Location-Based TODO App - Product Plan

## 1. Product Overview

### 1.1 Product Vision
A mobile TODO application that helps users manage location-specific tasks by organizing them based on geographic proximity. Users can create tasks associated with specific cities or towns and quickly view which tasks are available near their current location.

### 1.2 Target Platforms
- iOS (via React Native)
- Android (via React Native)

### 1.3 Core Value Proposition
Unlike traditional TODO apps, this application enables users to:
- Associate tasks with specific geographic locations
- Automatically discover tasks near their current position
- Optimize their time by seeing what needs to be done when they're in a particular area

---

## 2. Feature Specifications

### 2.1 Task Management (CRUD Operations)

#### 2.1.1 Create Task
**User Story:** As a user, I want to create a new task with a location so I can remember what to do when I'm in that area.

**Requirements:**
- Task title (required, max 100 characters)
- Task description (optional, max 500 characters)
- Location (required): city/town name
- Status: pending/completed (default: pending)
- Created timestamp (auto-generated)

**UI Elements:**
- "Add Task" button (floating action button or header button)
- Form with fields for title, description, and location
- Location field should support manual text input
- Save and Cancel buttons

#### 2.1.2 Read/List Tasks
**User Story:** As a user, I want to see all my tasks organized in a list so I can review everything I need to do.

**Requirements:**
- Display all tasks in a scrollable list
- Show task title, location, and status
- Support pull-to-refresh
- Sort by created date (newest first)
- Visual distinction between pending and completed tasks

**UI Elements:**
- Scrollable list/FlatList component
- Task card showing: title, location icon + city name, completion checkbox
- Empty state message when no tasks exist

#### 2.1.3 Update Task
**User Story:** As a user, I want to edit my tasks so I can update details or mark them as completed.

**Requirements:**
- Allow editing all task fields (title, description, location)
- Toggle completion status
- Save updated timestamp
- Validation on required fields

**UI Elements:**
- Tap on task to open edit screen
- Same form as create, pre-populated with existing data
- Checkbox or toggle for completion status
- Update and Cancel buttons

#### 2.1.4 Delete Task
**User Story:** As a user, I want to delete tasks I no longer need.

**Requirements:**
- Swipe-to-delete gesture on task items
- Confirmation dialog before deletion
- Permanent deletion from storage

**UI Elements:**
- Swipe gesture reveals delete button
- Confirmation modal: "Are you sure you want to delete this task?"

---

### 2.2 Available Tasks (Location-Based Filtering)

**User Story:** As a user, I want to see which tasks are near my current location so I can efficiently complete them when I'm in the area.

**Requirements:**
- Display a separate "Available Tasks" tab/section
- Request device location permission on first use
- Fetch current GPS coordinates
- Perform reverse geocoding to determine current city/town
- Filter tasks where task.location matches current city/town (case-insensitive)
- Update available tasks when location significantly changes
- Handle location permission denial gracefully

**Matching Logic:**
- Exact match: task location "San Francisco" matches current location "San Francisco"
- Case-insensitive comparison
- Trim whitespace from both values

**UI Elements:**
- Tab navigation: "All Tasks" and "Available Tasks"
- Location permission request dialog (system-provided)
- Loading indicator while fetching location
- Display current detected location: "You are in: [City Name]"
- Filtered list of matching tasks
- Empty state: "No tasks available in your current location" with current city display
- Refresh button to re-check location

**Error Handling:**
- Location permission denied: Show message and link to settings
- Location services disabled: Prompt user to enable
- Reverse geocoding API failure: Show error message and retry button
- No GPS signal: Display appropriate message

---

## 3. Technical Specifications

### 3.1 Technology Stack

**Framework:**
- React Native (latest stable version)
- React Navigation for navigation
- React Native CLI or Expo (recommend Expo for easier setup)

**State Management:**
- React Context API + useReducer (for simplicity)
- OR Redux Toolkit (if more complex state management needed)

**Data Persistence:**
- AsyncStorage for local task storage
- JSON format for task data

**Location Services:**
- `expo-location` (if using Expo) or `react-native-geolocation-service`
- Platform-specific permission handling (iOS: Info.plist, Android: AndroidManifest.xml)

**HTTP Client:**
- fetch API or axios for reverse geocoding requests

**UI Components:**
- React Native core components
- Optional: React Native Paper or React Native Elements for pre-built UI components

### 3.2 Data Models

#### Task Object
```javascript
{
  id: string,              // UUID v4
  title: string,           // Max 100 chars
  description: string,     // Max 500 chars, optional
  location: string,        // City/town name
  status: 'pending' | 'completed',
  createdAt: string,       // ISO 8601 timestamp
  updatedAt: string        // ISO 8601 timestamp
}
```

#### Location Object (runtime only, not persisted)
```javascript
{
  latitude: number,
  longitude: number,
  city: string,            // Extracted from reverse geocoding
  timestamp: number
}
```

### 3.3 API Integration

#### Reverse Geocoding API: geocode.maps.co

**Endpoint:**
```
GET https://geocode.maps.co/reverse?lat={latitude}&lon={longitude}
```

**Request Parameters:**
- `lat`: Latitude coordinate (required)
- `lon`: Longitude coordinate (required)

**Response Format (relevant fields):**
```json
{
  "address": {
    "city": "San Francisco",
    "town": "...",
    "village": "...",
    "county": "...",
    "state": "California",
    "country": "United States"
  }
}
```

**Extraction Logic:**
Priority order for location matching:
1. `address.city`
2. `address.town`
3. `address.village`
4. Fall back to `address.county` if none above exist

**Rate Limiting:**
- Free tier: 1 request per second
- Implement request throttling/debouncing
- Cache location results to minimize API calls

**Error Handling:**
- Network errors: Retry with exponential backoff (max 3 attempts)
- Invalid coordinates: Validate before sending request
- API unavailable: Display user-friendly error message

---

## 4. Implementation Plan

### Phase 1: Project Setup & Core Infrastructure
**Estimated Time:** 1-2 hours

**Tasks:**
1. Initialize React Native project
   - Set up React Native CLI or Expo project
   - Configure project structure (screens, components, services, utils)
   
2. Install dependencies
   ```bash
   # Core navigation
   npm install @react-navigation/native @react-navigation/bottom-tabs
   npm install react-native-screens react-native-safe-area-context
   
   # Storage
   npm install @react-native-async-storage/async-storage
   
   # Location (Expo)
   npm install expo-location
   # OR (React Native CLI)
   npm install react-native-geolocation-service
   
   # HTTP client
   npm install axios
   
   # UUID generation
   npm install react-native-uuid
   ```

3. Configure platform permissions
   - iOS: Update `ios/[ProjectName]/Info.plist`
     ```xml
     <key>NSLocationWhenInUseUsageDescription</key>
     <string>We need your location to show tasks near you</string>
     ```
   - Android: Update `android/app/src/main/AndroidManifest.xml`
     ```xml
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     ```

4. Set up folder structure
   ```
   src/
   ├── screens/
   │   ├── AllTasksScreen.js
   │   ├── AvailableTasksScreen.js
   │   ├── CreateTaskScreen.js
   │   └── EditTaskScreen.js
   ├── components/
   │   ├── TaskItem.js
   │   ├── TaskForm.js
   │   └── EmptyState.js
   ├── services/
   │   ├── storageService.js
   │   ├── locationService.js
   │   └── geocodingService.js
   ├── context/
   │   └── TaskContext.js
   ├── utils/
   │   └── helpers.js
   └── navigation/
       └── AppNavigator.js
   ```

### Phase 2: Data Layer & Storage
**Estimated Time:** 2-3 hours

**Tasks:**
1. Create `storageService.js`
   - `getTasks()`: Load all tasks from AsyncStorage
   - `saveTasks(tasks)`: Save tasks array to AsyncStorage
   - `addTask(task)`: Add new task and save
   - `updateTask(id, updates)`: Update existing task
   - `deleteTask(id)`: Remove task by ID
   - Storage key: `@location_todo_tasks`

2. Create TaskContext
   - Set up Context and Provider
   - State: `{ tasks: [], loading: boolean, error: string }`
   - Actions: `createTask`, `updateTask`, `deleteTask`, `loadTasks`, `toggleTaskStatus`
   - Load tasks on app initialization

3. Implement data validation utilities
   - Validate task title (required, max length)
   - Validate location (required, trim whitespace)
   - Generate UUID for new tasks
   - Generate ISO timestamps

### Phase 3: Location Services Integration
**Estimated Time:** 2-3 hours

**Tasks:**
1. Create `locationService.js`
   - `requestLocationPermission()`: Request and handle permissions
   - `getCurrentLocation()`: Get current GPS coordinates
   - `watchLocation(callback)`: Monitor location changes (optional)
   - Handle permission states: granted, denied, blocked

2. Create `geocodingService.js`
   - `reverseGeocode(latitude, longitude)`: Call geocode.maps.co API
   - Extract city/town from response with fallback logic
   - Implement error handling and retry logic
   - Cache results to minimize API calls (cache key: `lat_lon_rounded`)

3. Create location hook: `useCurrentLocation()`
   - Encapsulates permission, GPS fetching, and reverse geocoding
   - Returns: `{ city, loading, error, refetch }`
   - Handles loading states and errors

### Phase 4: UI - Task Management Screens
**Estimated Time:** 4-5 hours

**Tasks:**
1. Create `AllTasksScreen.js`
   - Use FlatList to display all tasks
   - Pull-to-refresh functionality
   - Navigation to Create/Edit screens
   - Swipe-to-delete with confirmation
   - Empty state when no tasks
   - Filter option to show/hide completed tasks

2. Create `TaskItem.js` component
   - Display task title, location, and status
   - Checkbox for completion toggle
   - Tap to navigate to edit screen
   - Swipeable for delete action

3. Create `TaskForm.js` component
   - Reusable form for create/edit
   - Input fields: title, description, location
   - Form validation
   - Props: `initialValues`, `onSubmit`, `submitButtonText`

4. Create `CreateTaskScreen.js`
   - Header with "Create Task" title
   - TaskForm component with empty initial values
   - Save button calls TaskContext.createTask()
   - Navigate back on success

5. Create `EditTaskScreen.js`
   - Header with "Edit Task" title
   - TaskForm pre-populated with task data
   - Update button calls TaskContext.updateTask()
   - Delete button with confirmation
   - Toggle completion status
   - Navigate back on success

### Phase 5: UI - Available Tasks Screen
**Estimated Time:** 3-4 hours

**Tasks:**
1. Create `AvailableTasksScreen.js`
   - Display current detected location: "You are in: [City]"
   - Use `useCurrentLocation()` hook
   - Filter tasks where location matches current city
   - Show loading indicator while fetching location
   - Manual refresh button
   - Handle permission denied state
   - Handle no GPS signal state
   - Empty state when no matching tasks

2. Implement location-based filtering logic
   - Case-insensitive comparison
   - Trim whitespace from both values
   - Exact match only (no fuzzy matching in v1)

3. Create location permission request flow
   - Check permission status on screen mount
   - Request permission if not determined
   - Show settings link if denied
   - Explain why permission is needed

### Phase 6: Navigation Setup
**Estimated Time:** 1-2 hours

**Tasks:**
1. Create `AppNavigator.js`
   - Bottom tab navigation with two tabs:
     - "All Tasks" → AllTasksScreen
     - "Available" → AvailableTasksScreen
   - Stack navigator for task create/edit modals
   - Configure tab icons and labels

2. Configure screen options
   - Header styling
   - Add "Create Task" button in All Tasks header
   - Appropriate back buttons

### Phase 7: Polish & Error Handling
**Estimated Time:** 2-3 hours

**Tasks:**
1. Implement comprehensive error handling
   - Storage errors (disk full, permission issues)
   - Network errors (API failures, no connection)
   - Location errors (GPS disabled, permission denied)
   - Display user-friendly error messages

2. Add loading states
   - Task list loading
   - Location fetching
   - API calls
   - Use ActivityIndicator appropriately

3. Improve UX
   - Add haptic feedback on task completion
   - Smooth animations for list updates
   - Proper keyboard handling in forms
   - Accessibility labels

4. Create `EmptyState.js` component
   - Reusable component for empty lists
   - Props: icon, title, message, action button

### Phase 8: Testing & Refinement
**Estimated Time:** 2-3 hours

**Tasks:**
1. Manual testing scenarios
   - Create, read, update, delete tasks
   - Test location permission flow (allow, deny, block)
   - Test with location services disabled
   - Test offline behavior
   - Test with no tasks
   - Test with tasks in different locations
   - Test location filtering accuracy
   - Test on both iOS and Android
   - Test API failures and recovery

2. Edge cases
   - Very long task titles/descriptions
   - Special characters in location names
   - Rapid task creation/deletion
   - App backgrounding during location fetch
   - Switching between tabs during operations

3. Performance optimization
   - FlatList optimization (keyExtractor, removeClippedSubviews)
   - Minimize unnecessary re-renders
   - Debounce location updates

---

## 5. Data Flow Diagrams

### 5.1 Task Creation Flow
```
User → CreateTaskScreen → TaskForm → Validation
                                      ↓
                               TaskContext.createTask()
                                      ↓
                              Generate UUID & timestamps
                                      ↓
                              Add to tasks array
                                      ↓
                              storageService.saveTasks()
                                      ↓
                              Update Context state
                                      ↓
                              Navigate back → AllTasksScreen (refreshed)
```

### 5.2 Available Tasks Flow
```
User → AvailableTasksScreen → useCurrentLocation()
                                      ↓
                          locationService.requestPermission()
                                      ↓ (if granted)
                          locationService.getCurrentLocation()
                                      ↓
                          geocodingService.reverseGeocode()
                                      ↓
                          Extract city name
                                      ↓
                          Filter tasks by location match
                                      ↓
                          Display filtered tasks
```

---

## 6. Key Considerations & Constraints

### 6.1 Location Accuracy
- GPS accuracy varies (5-50 meters typically)
- Reverse geocoding may return slightly different city names for nearby coordinates
- Use exact string matching only (no fuzzy matching in v1)
- User can manually refresh location to update

### 6.2 Privacy & Permissions
- Only request location "when in use" (not background)
- Clearly explain why location is needed
- App should function without location (All Tasks view always available)
- No location data is sent to external servers (except geocoding API)

### 6.3 Offline Behavior
- Task CRUD operations work offline (local storage)
- Location-based filtering requires GPS and internet (for reverse geocoding)
- Cache reverse geocoding results to reduce API dependency
- Handle offline gracefully with appropriate messaging

### 6.4 API Rate Limiting
- geocode.maps.co free tier: 1 request/second
- Implement request throttling
- Cache results based on rounded coordinates (e.g., 2 decimal places)
- Don't make API call on every location update

### 6.5 Scalability Considerations (Future)
- Current implementation targets <100 tasks (AsyncStorage limitation)
- For larger datasets, consider SQLite or Realm
- For multi-device sync, consider cloud storage (Firebase, Supabase)

---

## 7. Future Enhancements (Out of Scope for V1)

### 7.1 Enhanced Location Features
- Fuzzy location matching (nearby cities)
- Radius-based filtering (show tasks within X km)
- Multiple locations per task
- Location autocomplete when creating tasks
- Map view showing task locations

### 7.2 Advanced Task Management
- Task categories/tags
- Due dates and reminders
- Task priority levels
- Subtasks/checklists
- Recurring tasks
- Task search functionality

### 7.3 User Experience
- Dark mode support
- Task sorting options (by date, location, status)
- Bulk operations (delete multiple, mark multiple complete)
- Task statistics and insights
- Background location updates with notifications

### 7.4 Sync & Collaboration
- Cloud backup
- Multi-device sync
- Shared tasks with other users
- User authentication

---

## 8. Success Metrics

### 8.1 Functional Requirements
- ✅ All CRUD operations work correctly
- ✅ Location permission flow works on iOS and Android
- ✅ Reverse geocoding successfully identifies city/town
- ✅ Available tasks filters correctly based on location
- ✅ Tasks persist across app restarts
- ✅ App handles errors gracefully

### 8.2 Performance Requirements
- Task list loads in < 1 second (for up to 100 tasks)
- Location fetch completes in < 5 seconds (with good GPS signal)
- UI remains responsive during all operations
- No crashes or data loss

### 8.3 User Experience
- Clear visual feedback for all actions
- Intuitive navigation between screens
- Helpful error messages
- Obvious empty states

---

## 9. Development Guidelines for Claude Code

### 9.1 Code Organization
- Follow React Native best practices
- Use functional components with hooks
- Keep components small and focused (< 200 lines)
- Extract reusable logic into custom hooks
- Use PropTypes or TypeScript for type safety (optional)

### 9.2 Naming Conventions
- Components: PascalCase (TaskItem.js)
- Files: camelCase for utilities, PascalCase for components
- Functions: camelCase (getCurrentLocation)
- Constants: UPPER_SNAKE_CASE
- Context: PascalCase with "Context" suffix

### 9.3 Comments & Documentation
- Add JSDoc comments for public functions
- Document complex logic
- Include TODO comments for known limitations
- Add inline comments for non-obvious code

### 9.4 Error Handling Pattern
```javascript
try {
  // Operation
} catch (error) {
  console.error('Context for debugging:', error);
  // Set user-friendly error message
  setError('Something went wrong. Please try again.');
}
```

### 9.5 Testing Approach
- Focus on manual testing for V1
- Test on both iOS simulator and Android emulator
- Test real device with actual GPS
- Verify all permission states
- Test edge cases and error conditions

---

## 10. Deliverables Checklist

### Code Deliverables
- [ ] Complete React Native project structure
- [ ] All screens implemented and functional
- [ ] Navigation configured
- [ ] Storage service with CRUD operations
- [ ] Location service with permission handling
- [ ] Geocoding service with error handling
- [ ] TaskContext with state management
- [ ] Reusable UI components
- [ ] Platform-specific configuration files

### Documentation
- [ ] README.md with setup instructions
- [ ] API documentation (if needed)
- [ ] Known issues and limitations

### Testing
- [ ] Manual test plan executed
- [ ] Verified on iOS
- [ ] Verified on Android
- [ ] Edge cases handled

---

## 11. Getting Started (for Claude Code)

### Prerequisites
- Node.js (v14+)
- React Native development environment set up
- Xcode (for iOS) or Android Studio (for Android)
- Physical device or simulator/emulator

### Initial Setup Steps
1. Create React Native project: `npx react-native init LocationTodoApp`
2. Install all dependencies listed in Phase 1
3. Configure platform permissions
4. Implement in order: Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7 → Phase 8
5. Test thoroughly on both platforms
6. Address any bugs or issues

### Development Priority
1. **Must Have (P0):** CRUD operations, location-based filtering
2. **Should Have (P1):** Error handling, loading states, empty states
3. **Nice to Have (P2):** Animations, haptic feedback, advanced UX polish

---

## 12. Appendix

### A. Example Task Data (for testing)
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Buy groceries",
    "description": "Milk, eggs, bread, coffee",
    "location": "San Francisco",
    "status": "pending",
    "createdAt": "2024-02-14T10:30:00.000Z",
    "updatedAt": "2024-02-14T10:30:00.000Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Visit museum",
    "description": "Check out the new modern art exhibition",
    "location": "New York",
    "status": "pending",
    "createdAt": "2024-02-14T11:00:00.000Z",
    "updatedAt": "2024-02-14T11:00:00.000Z"
  }
]
```

### B. geocode.maps.co API Response Example
```json
{
  "place_id": 235189514,
  "licence": "Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
  "lat": "37.7749295",
  "lon": "-122.4194155",
  "display_name": "San Francisco, California, United States",
  "address": {
    "city": "San Francisco",
    "county": "San Francisco County",
    "state": "California",
    "country": "United States",
    "country_code": "us"
  }
}
```

### C. Useful React Native Libraries Reference
- **Navigation:** https://reactnavigation.org/
- **AsyncStorage:** https://react-native-async-storage.github.io/async-storage/
- **Location (Expo):** https://docs.expo.dev/versions/latest/sdk/location/
- **Geocoding API:** https://geocode.maps.co/

---

**Document Version:** 1.0  
**Last Updated:** February 14, 2026  
**Status:** Ready for Implementation
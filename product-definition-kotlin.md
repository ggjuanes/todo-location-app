# Location-Based TODO App - Product Plan

## 1. Product Overview

### 1.1 Product Vision
A mobile TODO application that helps users manage location-specific tasks by organizing them based on geographic proximity. Users can create tasks associated with specific cities or towns and quickly view which tasks are available near their current location.

### 1.2 Target Platforms
- iOS (via Kotlin Multiplatform + Compose Multiplatform)
- Android (via Kotlin Multiplatform + Compose Multiplatform)

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
- Scrollable list using `LazyColumn` composable
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
- Swipe gesture reveals delete button (using `SwipeToDismiss` composable)
- Confirmation dialog: "Are you sure you want to delete this task?"

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
- Kotlin Multiplatform (KMP) with Compose Multiplatform for shared UI
- KMP Wizard or Fleet template for project scaffolding
- Kotlin 2.0+ with Compose Multiplatform (latest stable)

**Architecture:**
- MVVM (Model-View-ViewModel) pattern
- Shared business logic and UI in `composeApp` (commonMain)
- Platform-specific implementations via `expect`/`actual` declarations
- Kotlin Coroutines + Flow for asynchronous operations and reactive state

**Data Persistence:**
- Room Multiplatform for local task storage (or SQLDelight as alternative)
- Type-safe database schema with Kotlin data classes

**Location Services:**
- Platform-specific location via `expect`/`actual`:
  - Android: Google Play Services `FusedLocationProviderClient`
  - iOS: `CLLocationManager` via Kotlin/Native interop

**HTTP Client:**
- Ktor client (multiplatform) for reverse geocoding requests
- `kotlinx.serialization` for JSON parsing

**Dependency Injection:**
- Koin Multiplatform for dependency injection across shared and platform modules

**UI Components:**
- Compose Multiplatform Material 3 components
- Shared composable functions in `commonMain`

### 3.2 Data Models

#### Task Entity
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String,              // UUID v4
    val title: String,           // Max 100 chars
    val description: String,     // Max 500 chars, optional
    val location: String,        // City/town name
    val status: TaskStatus,      // PENDING or COMPLETED
    val createdAt: String,       // ISO 8601 timestamp
    val updatedAt: String        // ISO 8601 timestamp
)

enum class TaskStatus {
    PENDING,
    COMPLETED
}
```

#### Location Data (runtime only, not persisted)
```kotlin
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val city: String,            // Extracted from reverse geocoding
    val timestamp: Long
)
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

**Serialization Model:**
```kotlin
@Serializable
data class GeocodeResponse(
    val address: Address? = null
)

@Serializable
data class Address(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null
)
```

**Extraction Logic:**
Priority order for location matching:
1. `address.city`
2. `address.town`
3. `address.village`
4. Fall back to `address.county` if none above exist

**Rate Limiting:**
- Free tier: 1 request per second
- Implement request throttling/debouncing via coroutine delay
- Cache location results to minimize API calls

**Error Handling:**
- Network errors: Retry with exponential backoff (max 3 attempts)
- Invalid coordinates: Validate before sending request
- API unavailable: Display user-friendly error message

---

## 4. Implementation Plan

### Phase 1: Project Setup & Core Infrastructure

**Tasks:**
1. Initialize Kotlin Multiplatform project
   - Use the KMP Wizard (kmp.jetbrains.com) or Android Studio KMP template
   - Select Compose Multiplatform for shared UI
   - Configure project for iOS and Android targets

2. Configure Gradle dependencies
   ```kotlin
   // build.gradle.kts (shared module / composeApp)
   kotlin {
       sourceSets {
           commonMain.dependencies {
               // Compose Multiplatform
               implementation(compose.runtime)
               implementation(compose.foundation)
               implementation(compose.material3)
               implementation(compose.components.resources)

               // Navigation
               implementation("org.jetbrains.androidx.navigation:navigation-compose:<version>")

               // Ktor (HTTP client)
               implementation("io.ktor:ktor-client-core:<version>")
               implementation("io.ktor:ktor-client-content-negotiation:<version>")
               implementation("io.ktor:ktor-serialization-kotlinx-json:<version>")

               // Room Multiplatform
               implementation("androidx.room:room-runtime:<version>")

               // Koin (DI)
               implementation("io.insert-koin:koin-core:<version>")
               implementation("io.insert-koin:koin-compose:<version>")

               // Kotlinx
               implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:<version>")
               implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:<version>")
               implementation("org.jetbrains.kotlinx:kotlinx-datetime:<version>")

               // UUID
               implementation("com.benasher44:uuid:<version>")

               // ViewModel
               implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:<version>")
           }

           androidMain.dependencies {
               // Ktor engine
               implementation("io.ktor:ktor-client-okhttp:<version>")
               // Google Play Services Location
               implementation("com.google.android.gms:play-services-location:<version>")
               // Koin Android
               implementation("io.insert-koin:koin-android:<version>")
           }

           iosMain.dependencies {
               // Ktor engine
               implementation("io.ktor:ktor-client-darwin:<version>")
           }
       }
   }
   ```

3. Configure platform permissions
   - iOS: Update `Info.plist`
     ```xml
     <key>NSLocationWhenInUseUsageDescription</key>
     <string>We need your location to show tasks near you</string>
     ```
   - Android: Update `AndroidManifest.xml`
     ```xml
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
     ```

4. Set up project structure
   ```
   composeApp/
   └── src/
       ├── commonMain/kotlin/
       │   └── com/app/locationtodo/
       │       ├── App.kt                          // Root composable
       │       ├── data/
       │       │   ├── local/
       │       │   │   ├── TaskDao.kt               // Room DAO
       │       │   │   └── AppDatabase.kt           // Room database
       │       │   ├── remote/
       │       │   │   └── GeocodingApi.kt          // Ktor API client
       │       │   └── repository/
       │       │       └── TaskRepository.kt
       │       ├── domain/
       │       │   └── model/
       │       │       ├── Task.kt                   // Domain model
       │       │       └── LocationData.kt
       │       ├── presentation/
       │       │   ├── alltasks/
       │       │   │   ├── AllTasksScreen.kt
       │       │   │   └── AllTasksViewModel.kt
       │       │   ├── availabletasks/
       │       │   │   ├── AvailableTasksScreen.kt
       │       │   │   └── AvailableTasksViewModel.kt
       │       │   ├── createtask/
       │       │   │   ├── CreateTaskScreen.kt
       │       │   │   └── CreateTaskViewModel.kt
       │       │   ├── edittask/
       │       │   │   ├── EditTaskScreen.kt
       │       │   │   └── EditTaskViewModel.kt
       │       │   └── components/
       │       │       ├── TaskItem.kt
       │       │       ├── TaskForm.kt
       │       │       └── EmptyState.kt
       │       ├── navigation/
       │       │   └── AppNavigation.kt
       │       ├── platform/
       │       │   └── LocationService.kt            // expect declarations
       │       └── di/
       │           └── AppModule.kt                  // Koin modules
       ├── androidMain/kotlin/
       │   └── com/app/locationtodo/
       │       ├── platform/
       │       │   └── LocationService.android.kt    // actual implementation
       │       └── di/
       │           └── PlatformModule.android.kt
       └── iosMain/kotlin/
           └── com/app/locationtodo/
               ├── platform/
               │   └── LocationService.ios.kt        // actual implementation
               └── di/
                   └── PlatformModule.ios.kt
   ```

### Phase 2: Data Layer & Storage

**Tasks:**
1. Create Room database
   - Define `TaskDao` with CRUD operations:
     - `getAllTasks(): Flow<List<Task>>` — observe all tasks
     - `getTaskById(id: String): Task?`
     - `insertTask(task: Task)`
     - `updateTask(task: Task)`
     - `deleteTask(task: Task)`
   - Define `AppDatabase` with Room `@Database` annotation
   - Create `expect`/`actual` for `getDatabaseBuilder()` per platform

2. Create `TaskRepository`
   - Wraps `TaskDao` with business logic
   - Exposes `Flow<List<Task>>` for reactive task list
   - Handles UUID generation and timestamp creation
   - Validates task fields before persistence

3. Implement data validation utilities
   - Validate task title (required, max length)
   - Validate location (required, trim whitespace)
   - Generate UUID via `com.benasher44:uuid`
   - Generate ISO timestamps via `kotlinx-datetime`

### Phase 3: Location Services Integration

**Tasks:**
1. Create `LocationService` expect/actual
   ```kotlin
   // commonMain — expect declaration
   expect class LocationService {
       suspend fun requestPermission(): PermissionResult
       suspend fun getCurrentLocation(): Coordinates?
   }

   enum class PermissionResult { GRANTED, DENIED, PERMANENTLY_DENIED }

   data class Coordinates(val latitude: Double, val longitude: Double)
   ```

   - **Android actual:** Use `FusedLocationProviderClient` + runtime permission request
   - **iOS actual:** Use `CLLocationManager` with Kotlin/Native interop and `suspendCancellableCoroutine`

2. Create `GeocodingApi` (Ktor client)
   - `suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodeResponse`
   - Extract city/town from response with fallback logic
   - Implement error handling and retry logic with `kotlinx.coroutines` delay
   - Cache results in-memory (`MutableMap` keyed by rounded coordinates)

3. Create `LocationViewModel` or utility
   - Encapsulates permission, GPS fetching, and reverse geocoding
   - Exposes `StateFlow<LocationUiState>` with states: `Loading`, `Success(city)`, `Error(message)`, `PermissionDenied`
   - Handles loading states and errors

### Phase 4: UI - Task Management Screens

**Tasks:**
1. Create `AllTasksScreen.kt`
   - Use `LazyColumn` to display all tasks
   - Pull-to-refresh via `pullToRefresh` modifier (Material 3)
   - Navigation to Create/Edit screens
   - Swipe-to-dismiss with confirmation dialog
   - Empty state when no tasks
   - Filter option to show/hide completed tasks

2. Create `TaskItem.kt` composable
   - Display task title, location, and status
   - `Checkbox` composable for completion toggle
   - Tap (`clickable`) to navigate to edit screen
   - `SwipeToDismiss` for delete action

3. Create `TaskForm.kt` composable
   - Reusable form for create/edit
   - `OutlinedTextField` inputs: title, description, location
   - Form validation with error display
   - Parameters: `initialTask: Task?`, `onSubmit: (Task) -> Unit`, `submitButtonText: String`

4. Create `CreateTaskScreen.kt`
   - Top app bar with "Create Task" title
   - `TaskForm` composable with null initial values
   - Save button triggers `CreateTaskViewModel.createTask()`
   - Navigate back on success

5. Create `EditTaskScreen.kt`
   - Top app bar with "Edit Task" title
   - `TaskForm` pre-populated with task data
   - Update button triggers `EditTaskViewModel.updateTask()`
   - Delete button with confirmation `AlertDialog`
   - Toggle completion status
   - Navigate back on success

### Phase 5: UI - Available Tasks Screen

**Tasks:**
1. Create `AvailableTasksScreen.kt`
   - Display current detected location: "You are in: [City]"
   - Collect `LocationUiState` from ViewModel
   - Filter tasks where location matches current city
   - Show `CircularProgressIndicator` while fetching location
   - Manual refresh `IconButton`
   - Handle permission denied state with settings prompt
   - Handle no GPS signal state
   - Empty state when no matching tasks

2. Implement location-based filtering logic
   - Case-insensitive comparison via `equals(other, ignoreCase = true)`
   - Trim whitespace via `trim()`
   - Exact match only (no fuzzy matching in v1)

3. Create location permission request flow
   - Check permission status on screen composition
   - Request permission if not determined
   - Show settings link if permanently denied
   - Explain why permission is needed via rationale dialog

### Phase 6: Navigation Setup

**Tasks:**
1. Create `AppNavigation.kt`
   - Bottom navigation bar with two tabs (Material 3 `NavigationBar`):
     - "All Tasks" → `AllTasksScreen`
     - "Available" → `AvailableTasksScreen`
   - `NavHost` with nested navigation graph for task create/edit screens
   - Configure tab icons (`Icons.Default.List`, `Icons.Default.LocationOn`) and labels

2. Configure screen options
   - `TopAppBar` styling with Material 3
   - Add "Create Task" FAB in All Tasks screen
   - Back navigation via `NavController.popBackStack()`

### Phase 7: Polish & Error Handling

**Tasks:**
1. Implement comprehensive error handling
   - Storage errors (database failures)
   - Network errors (API failures, no connection)
   - Location errors (GPS disabled, permission denied)
   - Display user-friendly error messages via `Snackbar`

2. Add loading states
   - Task list loading
   - Location fetching
   - API calls
   - Use `CircularProgressIndicator` and skeleton composables

3. Improve UX
   - Smooth animations via Compose `animateContentSize`, `AnimatedVisibility`
   - Proper keyboard handling (`imePadding`, `keyboardOptions`)
   - Accessibility via `contentDescription` and `semantics` modifiers

4. Create `EmptyState.kt` composable
   - Reusable component for empty lists
   - Parameters: `icon: ImageVector`, `title: String`, `message: String`, `actionButton: @Composable (() -> Unit)?`

### Phase 8: Testing & Refinement

**Tasks:**
1. Manual testing scenarios
   - Create, read, update, delete tasks
   - Test location permission flow (allow, deny, permanently deny)
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
   - `LazyColumn` optimization (`key` parameter)
   - Minimize unnecessary recompositions (stable types, `remember`, `derivedStateOf`)
   - Debounce location updates via `Flow.debounce()`

---

## 5. Data Flow Diagrams

### 5.1 Task Creation Flow
```
User → CreateTaskScreen → TaskForm → Validation
                                      ↓
                           CreateTaskViewModel.createTask()
                                      ↓
                           Generate UUID & timestamps
                                      ↓
                           TaskRepository.insertTask()
                                      ↓
                           Room persists to SQLite
                                      ↓
                           Flow emits updated task list
                                      ↓
                           Navigate back → AllTasksScreen (auto-refreshed via Flow)
```

### 5.2 Available Tasks Flow
```
User → AvailableTasksScreen → AvailableTasksViewModel
                                      ↓
                          LocationService.requestPermission()
                                      ↓ (if granted)
                          LocationService.getCurrentLocation()
                                      ↓
                          GeocodingApi.reverseGeocode()
                                      ↓
                          Extract city name
                                      ↓
                          Filter tasks by location match
                                      ↓
                          Emit filtered list via StateFlow
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
- Task CRUD operations work offline (Room/SQLite local storage)
- Location-based filtering requires GPS and internet (for reverse geocoding)
- Cache reverse geocoding results to reduce API dependency
- Handle offline gracefully with appropriate messaging

### 6.4 API Rate Limiting
- geocode.maps.co free tier: 1 request/second
- Implement request throttling via coroutine delay
- Cache results based on rounded coordinates (e.g., 2 decimal places)
- Don't make API call on every location update

### 6.5 KMP-Specific Considerations
- Platform-specific code (location, permissions) should be minimal and isolated behind `expect`/`actual`
- All business logic, data layer, and UI should live in `commonMain`
- Use Koin for wiring platform-specific implementations
- iOS framework is generated via KMP Gradle plugin; test CocoaPods/SPM integration early

### 6.6 Scalability Considerations (Future)
- Room/SQLite handles thousands of tasks efficiently
- For multi-device sync, consider cloud storage (Firebase, Supabase)
- KMP allows adding Desktop (JVM) or Web (Wasm) targets in the future

---

## 7. Future Enhancements (Out of Scope for V1)

### 7.1 Enhanced Location Features
- Fuzzy location matching (nearby cities)
- Radius-based filtering (show tasks within X km)
- Multiple locations per task
- Location autocomplete when creating tasks
- Map view showing task locations (Google Maps / Apple Maps via expect/actual)

### 7.2 Advanced Task Management
- Task categories/tags
- Due dates and reminders
- Task priority levels
- Subtasks/checklists
- Recurring tasks
- Task search functionality

### 7.3 User Experience
- Dark mode support (Material 3 dynamic theming)
- Task sorting options (by date, location, status)
- Bulk operations (delete multiple, mark multiple complete)
- Task statistics and insights
- Background location updates with notifications

### 7.4 Sync & Collaboration
- Cloud backup
- Multi-device sync
- Shared tasks with other users
- User authentication

### 7.5 Additional Platforms
- Desktop (JVM) target via Compose for Desktop
- Web (Wasm) target via Compose for Web

---

## 8. Success Metrics

### 8.1 Functional Requirements
- All CRUD operations work correctly
- Location permission flow works on iOS and Android
- Reverse geocoding successfully identifies city/town
- Available tasks filters correctly based on location
- Tasks persist across app restarts
- App handles errors gracefully

### 8.2 Performance Requirements
- Task list loads in < 1 second (for up to 100 tasks)
- Location fetch completes in < 5 seconds (with good GPS signal)
- UI remains responsive during all operations (no frame drops)
- No crashes or data loss

### 8.3 User Experience
- Clear visual feedback for all actions
- Intuitive navigation between screens
- Helpful error messages
- Obvious empty states

---

## 9. Development Guidelines for Claude Code

### 9.1 Code Organization
- Follow Kotlin Multiplatform best practices
- Use MVVM architecture with ViewModel + StateFlow
- Keep composables small and focused (< 200 lines)
- Extract reusable logic into shared utility functions and ViewModels
- Use Kotlin data classes and sealed classes/interfaces for type safety

### 9.2 Naming Conventions
- Packages: lowercase dot-separated (`com.app.locationtodo.presentation`)
- Classes/Objects: PascalCase (`TaskRepository`, `AllTasksViewModel`)
- Files: PascalCase matching the primary class (`TaskItem.kt`)
- Functions: camelCase (`getCurrentLocation`)
- Composables: PascalCase (`TaskForm`, `EmptyState`)
- Constants: UPPER_SNAKE_CASE
- Properties: camelCase

### 9.3 Comments & Documentation
- Add KDoc comments for public functions and classes
- Document complex logic
- Include TODO comments for known limitations
- Add inline comments for non-obvious code

### 9.4 Error Handling Pattern
```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>
}

// Usage in ViewModel
viewModelScope.launch {
    _uiState.value = UiState.Loading
    try {
        val result = repository.getTasks()
        _uiState.value = UiState.Success(result)
    } catch (e: Exception) {
        _uiState.value = UiState.Error("Something went wrong. Please try again.")
    }
}
```

### 9.5 State Management Pattern
```kotlin
// Sealed interface for UI state
sealed interface TasksUiState {
    data object Loading : TasksUiState
    data class Success(val tasks: List<Task>) : TasksUiState
    data class Error(val message: String) : TasksUiState
}

// ViewModel exposes StateFlow
class AllTasksViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()
}
```

### 9.6 Testing Approach
- Focus on manual testing for V1
- Test on both iOS simulator and Android emulator
- Test real device with actual GPS
- Verify all permission states
- Test edge cases and error conditions

---

## 10. Deliverables Checklist

### Code Deliverables
- [ ] Complete KMP project structure with `composeApp`, `androidApp`, and `iosApp` modules
- [ ] All screens implemented as Compose Multiplatform composables
- [ ] Navigation configured with Compose Navigation
- [ ] Room database with DAO and CRUD operations
- [ ] Location service with `expect`/`actual` platform implementations
- [ ] Geocoding service (Ktor) with error handling
- [ ] ViewModels with StateFlow-based state management
- [ ] Reusable UI composables
- [ ] Koin dependency injection configuration
- [ ] Platform-specific permission and configuration files

### Documentation
- [ ] README.md with setup instructions
- [ ] Known issues and limitations

### Testing
- [ ] Manual test plan executed
- [ ] Verified on iOS
- [ ] Verified on Android
- [ ] Edge cases handled

---

## 11. Getting Started (for Claude Code)

### Prerequisites
- Android Studio (latest stable, with KMP plugin)
- Xcode (latest stable, for iOS builds)
- JDK 17+
- Kotlin 2.0+
- CocoaPods or SPM configured for iOS dependencies

### Initial Setup Steps
1. Create KMP project via KMP Wizard or Android Studio template with Compose Multiplatform
2. Configure all Gradle dependencies listed in Phase 1
3. Configure platform permissions (Info.plist, AndroidManifest.xml)
4. Implement in order: Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 → Phase 7 → Phase 8
5. Test thoroughly on both platforms
6. Address any bugs or issues

### Development Priority
1. **Must Have (P0):** CRUD operations, location-based filtering
2. **Should Have (P1):** Error handling, loading states, empty states
3. **Nice to Have (P2):** Animations, advanced UX polish

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
    "status": "PENDING",
    "createdAt": "2024-02-14T10:30:00.000Z",
    "updatedAt": "2024-02-14T10:30:00.000Z"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "title": "Visit museum",
    "description": "Check out the new modern art exhibition",
    "location": "New York",
    "status": "PENDING",
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

### C. Key KMP Libraries Reference
- **Compose Multiplatform:** https://www.jetbrains.com/compose-multiplatform/
- **KMP Wizard:** https://kmp.jetbrains.com/
- **Ktor Client:** https://ktor.io/docs/client-overview.html
- **Room Multiplatform:** https://developer.android.com/kotlin/multiplatform/room
- **SQLDelight (alternative):** https://cashapp.github.io/sqldelight/
- **Koin Multiplatform:** https://insert-koin.io/docs/reference/koin-mp/kmp/
- **kotlinx.serialization:** https://github.com/Kotlin/kotlinx.serialization
- **kotlinx-datetime:** https://github.com/Kotlin/kotlinx-datetime
- **Geocoding API:** https://geocode.maps.co/

---

**Document Version:** 2.0
**Last Updated:** February 16, 2026
**Status:** Ready for Implementation

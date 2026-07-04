package com.example.tripreminder

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.tripreminder.data.Trip
import com.example.tripreminder.data.TripStorage
import com.example.tripreminder.notifications.TripNotificationScheduler
import com.example.tripreminder.tripcreate.CreateScreen
import com.example.tripreminder.ui.startup.LoadingScreen
import com.example.tripreminder.ui.theme.TripReminderTheme
import com.example.tripreminder.ui.trips.TripDetailsScreen
import com.example.tripreminder.ui.trips.TripListScreen
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TripReminderTheme {
                val context = LocalContext.current
                val nowMillis = remember { System.currentTimeMillis() }
                val tripStorage = remember(context) {
                    TripStorage(context.applicationContext)
                }
                val initialTrips = remember(tripStorage) {
                    tripStorage.loadTrips().orEmpty()
                }
                var trips by remember { mutableStateOf(initialTrips) }
                var nextTripId by remember {
                    mutableStateOf((initialTrips.maxOfOrNull { it.id } ?: 0L) + 1L)
                }
                val notificationScheduler = remember(context) {
                    TripNotificationScheduler(context.applicationContext)
                }

                var isLoading by remember { mutableStateOf(true) }
                var isCreatingTrip by remember { mutableStateOf(false) }
                var editingTrip by remember { mutableStateOf<Trip?>(null) }
                var selectedTrip by remember { mutableStateOf<Trip?>(null) }
                var notificationsAllowed by remember {
                    mutableStateOf(notificationScheduler.canPostNotifications())
                }

                fun updateTrips(updatedTrips: List<Trip>) {
                    trips = updatedTrips
                    tripStorage.saveTrips(updatedTrips)
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    notificationsAllowed = granted
                }

                LaunchedEffect(Unit) {
                    notificationScheduler.createNotificationChannel()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    delay(900)
                    isLoading = false
                }

                LaunchedEffect(notificationsAllowed, trips) {
                    if (notificationsAllowed) {
                        notificationScheduler.showDueTripNotifications(trips, nowMillis)
                        notificationScheduler.scheduleTripNotifications(trips, nowMillis)
                    }
                }

                when {
                    isLoading -> LoadingScreen()
                    isCreatingTrip -> CreateScreen(
                        onBack = { isCreatingTrip = false },
                        onTripCreated = { trip ->
                            updateTrips(trips + trip.copy(id = nextTripId))
                            nextTripId += 1
                            isCreatingTrip = false
                        },
                        placeSearchEnabled = BuildConfig.MAPKIT_API_KEY.isNotBlank(),
                    )
                    editingTrip != null -> {
                        val tripToEdit = editingTrip!!
                        CreateScreen(
                            onBack = {
                                editingTrip = null
                                selectedTrip = tripToEdit
                            },
                            onTripCreated = { trip ->
                                val updatedTrip = trip.copy(id = tripToEdit.id)
                                notificationScheduler.cancelTripNotification(tripToEdit)
                                updateTrips(
                                    trips.map { currentTrip ->
                                        if (currentTrip.id == tripToEdit.id) updatedTrip else currentTrip
                                    },
                                )
                                editingTrip = null
                                selectedTrip = updatedTrip
                            },
                            placeSearchEnabled = BuildConfig.MAPKIT_API_KEY.isNotBlank(),
                            initialTrip = tripToEdit,
                            screenTitle = "Редактирование поездки",
                            screenSubtitle = "Измени параметры маршрута и напоминания",
                            submitButtonText = "Сохранить изменения",
                        )
                    }
                    selectedTrip != null -> TripDetailsScreen(
                        trip = selectedTrip!!,
                        nowMillis = nowMillis,
                        onBack = { selectedTrip = null },
                        onEdit = {
                            selectedTrip?.let { trip ->
                                editingTrip = trip
                                selectedTrip = null
                            }
                        },
                        onDelete = {
                            selectedTrip?.let { trip ->
                                notificationScheduler.cancelTripNotification(trip)
                                updateTrips(trips.filterNot { currentTrip -> currentTrip.id == trip.id })
                                selectedTrip = null
                            }
                        },
                    )
                    else -> TripListScreen(
                        trips = trips,
                        nowMillis = nowMillis,
                        onAddTrip = {
                            isCreatingTrip = true
                        },
                        onTripClick = { trip ->
                            selectedTrip = trip
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.getInstance().onStart()
        }
    }

    override fun onStop() {
        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.getInstance().onStop()
        }
        super.onStop()
    }
}

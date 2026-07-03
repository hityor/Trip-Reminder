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
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.Trip
import com.example.tripreminder.notifications.TripNotificationScheduler
import com.example.tripreminder.tripcreate.CreateScreen
import com.example.tripreminder.ui.startup.LoadingScreen
import com.example.tripreminder.ui.theme.TripReminderTheme
import com.example.tripreminder.ui.trips.TripDetailsScreen
import com.example.tripreminder.ui.trips.TripListScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TripReminderTheme {
                val nowMillis = remember { System.currentTimeMillis() }
                val initialTrips = remember(nowMillis) { demoTrips(nowMillis) }
                var trips by remember { mutableStateOf(initialTrips) }
                var nextTripId by remember {
                    mutableStateOf((initialTrips.maxOfOrNull { it.id } ?: 0L) + 1L)
                }
                val context = LocalContext.current
                val notificationScheduler = remember(context) {
                    TripNotificationScheduler(context.applicationContext)
                }

                var isLoading by remember { mutableStateOf(true) }
                var isCreatingTrip by remember { mutableStateOf(false) }
                var selectedTrip by remember { mutableStateOf<Trip?>(null) }
                var notificationsAllowed by remember {
                    mutableStateOf(notificationScheduler.canPostNotifications())
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
                            trips = trips + trip.copy(id = nextTripId)
                            nextTripId += 1
                            isCreatingTrip = false
                        },
                    )
                    selectedTrip != null -> TripDetailsScreen(
                        trip = selectedTrip!!,
                        nowMillis = nowMillis,
                        onBack = { selectedTrip = null },
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
}

private fun demoTrips(nowMillis: Long): List<Trip> {
    val minute = 60_000L
    return listOf(
        Trip(
            id = 1,
            place = "Аэропорт Красноярск",
            arrivalTimeMillis = nowMillis + 240 * minute,
            transportMode = TransportMode.Car,
            routeDurationMinutes = 45,
            remindBeforeMinutes = 15,
            safetyPercent = 10,
        ),
        Trip(
            id = 2,
            place = "Железнодорожный вокзал",
            arrivalTimeMillis = nowMillis + 90 * minute,
            transportMode = TransportMode.PublicTransport,
            routeDurationMinutes = 45,
            remindBeforeMinutes = 10,
            safetyPercent = 10,
        ),
        Trip(
            id = 3,
            place = "Университет",
            arrivalTimeMillis = nowMillis + 20 * minute,
            transportMode = TransportMode.Walking,
            routeDurationMinutes = 18,
            remindBeforeMinutes = 5,
            safetyPercent = 10,
        ),
        Trip(
            id = 4,
            place = "Кинотеатр",
            arrivalTimeMillis = nowMillis - 60 * minute,
            transportMode = TransportMode.Taxi,
            routeDurationMinutes = 25,
            remindBeforeMinutes = 10,
            safetyPercent = 10,
        ),
    )
}

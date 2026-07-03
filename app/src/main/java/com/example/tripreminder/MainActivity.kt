package com.example.tripreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.Trip
import com.example.tripreminder.ui.theme.TripReminderTheme
import com.example.tripreminder.ui.trips.TripListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TripReminderTheme {
                val nowMillis = remember { System.currentTimeMillis() }
                var selectedTrip by remember { mutableStateOf<Trip?>(null) }

                TripListScreen(
                    trips = demoTrips(nowMillis),
                    nowMillis = nowMillis,
                    onAddTrip = {
                        // Экран создания поездки подключит другой участник команды.
                    },
                    onTripClick = { trip ->
                        selectedTrip = trip
                    },
                )

                selectedTrip?.let { trip ->
                    AlertDialog(
                        onDismissRequest = { selectedTrip = null },
                        title = { Text("Поездка") },
                        text = { Text("Позже здесь откроется экран деталей: ${trip.place}") },
                        confirmButton = {
                            TextButton(onClick = { selectedTrip = null }) {
                                Text("Понятно")
                            }
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

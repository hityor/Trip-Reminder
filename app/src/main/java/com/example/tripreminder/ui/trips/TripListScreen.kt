package com.example.tripreminder.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.Trip
import com.example.tripreminder.data.TripStatus
import com.example.tripreminder.data.statusAt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripListScreen(
    trips: List<Trip>,
    nowMillis: Long,
    onAddTrip: () -> Unit,
    onTripClick: (Trip) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortedTrips = remember(trips, nowMillis) {
        trips.sortedWith(
            compareBy<Trip> { it.statusAt(nowMillis) == TripStatus.Past }
                .thenBy { it.arrivalTimeMillis }
        )
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTrip) {
                Text(text = "+", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(innerPadding)
        ) {
            TripsHeader()

            if (sortedTrips.isEmpty()) {
                EmptyTrips()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sortedTrips, key = { it.id }) { trip ->
                        TripCard(
                            trip = trip,
                            nowMillis = nowMillis,
                            onClick = { onTripClick(trip) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = "Поездки",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Ближайшие маршруты и время выхода",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyTrips() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Пока нет поездок",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Нажми плюс, чтобы добавить первую поездку.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    nowMillis: Long,
    onClick: () -> Unit,
) {
    val status = trip.statusAt(nowMillis)
    val colors = statusColors(status)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = colors.cardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.place,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Прибыть: ${formatDateTime(trip.arrivalTimeMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                StatusBadge(text = statusTitle(status), color = colors.badgeColor)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                InfoBlock(
                    label = "Выйти",
                    value = formatTime(trip.leaveTimeMillis),
                    modifier = Modifier.weight(1f),
                )
                InfoBlock(
                    label = "В пути",
                    value = minutesText(trip.routeDurationMinutes),
                    modifier = Modifier.weight(1f),
                )
                InfoBlock(
                    label = "Транспорт",
                    value = transportTitle(trip.transportMode),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun InfoBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
        }
    }
}

private data class TripCardColors(
    val cardColor: Color,
    val badgeColor: Color,
)

@Composable
private fun statusColors(status: TripStatus): TripCardColors {
    val colorScheme = MaterialTheme.colorScheme
    return when (status) {
        TripStatus.Planned -> TripCardColors(
            cardColor = colorScheme.surface,
            badgeColor = colorScheme.primaryContainer,
        )
        TripStatus.Soon -> TripCardColors(
            cardColor = colorScheme.tertiaryContainer,
            badgeColor = colorScheme.tertiary,
        )
        TripStatus.TimeToLeave -> TripCardColors(
            cardColor = colorScheme.errorContainer,
            badgeColor = colorScheme.error,
        )
        TripStatus.Past -> TripCardColors(
            cardColor = colorScheme.surfaceVariant,
            badgeColor = colorScheme.outlineVariant,
        )
    }
}

private fun statusTitle(status: TripStatus): String = when (status) {
    TripStatus.Planned -> "Запланирована"
    TripStatus.Soon -> "Скоро"
    TripStatus.TimeToLeave -> "Пора выходить"
    TripStatus.Past -> "Прошла"
}

private fun transportTitle(mode: TransportMode): String = when (mode) {
    TransportMode.Car -> "Авто"
    TransportMode.PublicTransport -> "Транспорт"
    TransportMode.Walking -> "Пешком"
    TransportMode.Taxi -> "Такси"
}

private fun minutesText(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours > 0 && rest > 0 -> "${hours} ч ${rest} мин"
        hours > 0 -> "${hours} ч"
        else -> "${rest} мин"
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(millis))

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("dd.MM HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(millis))


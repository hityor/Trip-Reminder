package com.example.tripreminder.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun TripDetailsScreen(
    trip: Trip,
    nowMillis: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = trip.statusAt(nowMillis)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        TextButton(onClick = onBack) {
            Text("Назад")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = trip.place,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Подробности поездки",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(18.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Статус",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    StatusBadge(text = statusTitle(status), color = statusColor(status))
                }

                DetailRow(label = "Прибыть", value = formatDateTime(trip.arrivalTimeMillis))
                DetailRow(label = "Выйти", value = formatDateTime(trip.leaveTimeMillis))
                DetailRow(label = "В пути", value = minutesText(trip.routeDurationMinutes))
                DetailRow(label = "Напомнить за", value = minutesText(trip.remindBeforeMinutes))
                DetailRow(label = "Запас времени", value = "${trip.safetyPercent}% (${minutesText(trip.safetyMinutes)})")
                DetailRow(label = "Транспорт", value = transportTitle(trip.transportMode))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (status == TripStatus.Past) {
            Text(
                text = "Прошедшая поездка доступна только для просмотра.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            ) {
                Text("Изменить поездку")
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            ) {
                Text("Удалить поездку")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
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
private fun statusColor(status: TripStatus): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (status) {
        TripStatus.Planned -> colorScheme.primaryContainer
        TripStatus.Soon -> colorScheme.tertiary
        TripStatus.TimeToLeave -> colorScheme.error
        TripStatus.Past -> colorScheme.outlineVariant
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
    TransportMode.PublicTransport -> "Общественный транспорт"
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

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(millis))

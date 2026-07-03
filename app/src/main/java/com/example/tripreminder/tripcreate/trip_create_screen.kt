package com.example.tripreminder.tripcreate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.Trip
import java.util.Calendar

@Composable
fun CreateScreen(
    onBack: () -> Unit,
    onTripCreated: (Trip) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tripName by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var tripDate by remember { mutableStateOf(Calendar.getInstance()) }
    var tripTime by remember { mutableStateOf(Calendar.getInstance()) }
    var tripTransport by remember { mutableStateOf<TransportMode?>(null) }
    var routeDurationMinutes by remember { mutableStateOf("30") }
    var remindBeforeMinutes by remember { mutableStateOf("15") }
    val parsedRouteDuration = routeDurationMinutes.toIntOrNull()
    val parsedRemindBefore = remindBeforeMinutes.toIntOrNull()
    val canSave = place.isNotBlank() &&
        tripTransport != null &&
        parsedRouteDuration != null &&
        parsedRouteDuration > 0 &&
        parsedRemindBefore != null &&
        parsedRemindBefore >= 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack) {
            Text("Назад")
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Новая поездка",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Параметры маршрута и напоминания",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = tripName,
            onValueChange = { tripName = it },
            label = { Text("Название поездки") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = place,
            onValueChange = { place = it },
            label = { Text("Место назначения") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
        )

        DateField(
            selectedDate = tripDate,
            onDateSelected = { tripDate = it },
            modifier = Modifier.fillMaxWidth(),
        )

        TimeField(
            selectedTime = tripTime,
            onTimeSelected = { tripTime = it },
            modifier = Modifier.fillMaxWidth(),
            title = "Время прибытия",
        )

        TransportField(
            selectedTransport = tripTransport,
            onTransportSelected = { tripTransport = it },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = routeDurationMinutes,
            onValueChange = { value ->
                routeDurationMinutes = value.filter { it.isDigit() }.take(3)
            },
            label = { Text("Время в пути, мин") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = remindBeforeMinutes,
            onValueChange = { value ->
                remindBeforeMinutes = value.filter { it.isDigit() }.take(3)
            },
            label = { Text("Напомнить за, мин") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val transport = tripTransport ?: return@Button
                val routeMinutes = parsedRouteDuration ?: return@Button
                val remindMinutes = parsedRemindBefore ?: return@Button
                onTripCreated(
                    Trip(
                        place = place.trim(),
                        name = tripName.trim().ifBlank { place.trim() },
                        arrivalTimeMillis = arrivalTimeMillis(tripDate, tripTime),
                        transportMode = transport,
                        routeDurationMinutes = routeMinutes,
                        remindBeforeMinutes = remindMinutes,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSave,
        ) {
            Text("Запланировать поездку")
        }
    }
}

private fun arrivalTimeMillis(date: Calendar, time: Calendar): Long =
    Calendar.getInstance().apply {
        set(Calendar.YEAR, date.get(Calendar.YEAR))
        set(Calendar.MONTH, date.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, time.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

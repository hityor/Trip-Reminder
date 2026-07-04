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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.Trip
import com.example.tripreminder.data.UserLocation
import com.example.tripreminder.data.UserLocationProvider
import java.util.Calendar

@Composable
fun CreateScreen(
    onBack: () -> Unit,
    onTripCreated: (Trip) -> Unit,
    placeSearchEnabled: Boolean,
    modifier: Modifier = Modifier,
    initialTrip: Trip? = null,
    screenTitle: String = "Новая поездка",
    screenSubtitle: String = "Параметры маршрута и напоминания",
    submitButtonText: String = "Запланировать поездку",
    userLocationProvider: UserLocationProvider? = null,
    locationPermissionGranted: Boolean = false,
    routeDurationViewModel: RouteDurationViewModel = viewModel(),
) {
    val initialPlace = remember(initialTrip) {
        initialTrip?.let { trip ->
            val latitude = trip.latitude
            val longitude = trip.longitude
            if (latitude != null && longitude != null) {
                PlaceSearchResult(
                    address = trip.place,
                    latitude = latitude,
                    longitude = longitude,
                )
            } else {
                null
            }
        }
    }
    var tripName by remember(initialTrip) { mutableStateOf(initialTrip?.name.orEmpty()) }
    var place by remember(initialTrip) { mutableStateOf(initialTrip?.place.orEmpty()) }
    var selectedPlace by remember(initialTrip) { mutableStateOf(initialPlace) }
    var tripDate by remember(initialTrip) {
        mutableStateOf(initialTrip?.arrivalTimeMillis?.let(::calendarFromMillis) ?: Calendar.getInstance())
    }
    var tripTime by remember(initialTrip) {
        mutableStateOf(initialTrip?.arrivalTimeMillis?.let(::calendarFromMillis) ?: Calendar.getInstance())
    }
    var tripTransport by remember(initialTrip) { mutableStateOf(initialTrip?.transportMode) }
    var routeDurationMinutes by remember(initialTrip) {
        mutableStateOf(initialTrip?.routeDurationMinutes?.toString() ?: "30")
    }
    var remindBeforeMinutes by remember(initialTrip) {
        mutableStateOf(initialTrip?.remindBeforeMinutes?.toString() ?: "15")
    }
    val routeDurationState by routeDurationViewModel.state.collectAsState()
    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var isLocationReady by remember(userLocationProvider, locationPermissionGranted) {
        mutableStateOf(userLocationProvider == null || !locationPermissionGranted)
    }
    var locationStatusMessage by remember(userLocationProvider, locationPermissionGranted) {
        mutableStateOf<String?>(null)
    }
    val parsedRouteDuration = routeDurationMinutes.toIntOrNull()
    val parsedRemindBefore = remindBeforeMinutes.toIntOrNull()
    val hasSelectedPlace = selectedPlace != null && place == selectedPlace?.address
    val canSave = hasSelectedPlace &&
        tripTransport != null &&
        parsedRouteDuration != null &&
        parsedRouteDuration > 0 &&
        parsedRemindBefore != null &&
        parsedRemindBefore >= 0

    LaunchedEffect(userLocationProvider, locationPermissionGranted) {
        if (userLocationProvider == null) {
            userLocation = null
            locationStatusMessage = null
            isLocationReady = true
            return@LaunchedEffect
        }

        if (!locationPermissionGranted) {
            userLocation = null
            locationStatusMessage = "Геолокация не разрешена. Маршрут считается от точки по умолчанию."
            isLocationReady = true
            return@LaunchedEffect
        }

        isLocationReady = false
        locationStatusMessage = "Определяем текущее местоположение..."
        val location = userLocationProvider.currentLocation()
        userLocation = location
        locationStatusMessage = if (location != null) {
            "Маршрут считается от текущего местоположения."
        } else {
            "Не удалось определить геолокацию. Маршрут считается от точки по умолчанию."
        }
        isLocationReady = true
    }

    LaunchedEffect(selectedPlace, tripTransport, userLocation, isLocationReady) {
        val destination = selectedPlace
        val transport = tripTransport
        val shouldCalculateDuration = initialTrip == null ||
            destination?.address != initialTrip.place ||
            transport != initialTrip.transportMode
        val shouldWaitForLocation = !isLocationReady && transport != TransportMode.PublicTransport

        if (shouldWaitForLocation) {
            routeDurationViewModel.clear()
            return@LaunchedEffect
        }

        if (destination != null && transport != null && shouldCalculateDuration) {
            routeDurationViewModel.calculateDuration(destination, transport, userLocation)
        } else {
            routeDurationViewModel.clear()
        }
    }

    LaunchedEffect(routeDurationState.durationMinutes) {
        routeDurationState.durationMinutes?.let { durationMinutes ->
            routeDurationMinutes = durationMinutes.toString()
        }
    }

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
                text = screenTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = screenSubtitle,
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

        PlaceSearchField(
            place = place,
            onPlaceChange = { value ->
                place = value
                selectedPlace = null
            },
            onPlaceSelected = { result ->
                selectedPlace = result
            },
            searchEnabled = placeSearchEnabled,
            hasSelectedPlace = hasSelectedPlace,
            userLocation = userLocation,
            modifier = Modifier.fillMaxWidth(),
        )

        if (!placeSearchEnabled) {
            Text(
                text = "Поиск мест выключен: не найден MAPKIT_API_KEY.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else if (place.isNotBlank() && !hasSelectedPlace) {
            Text(
                text = "Выбери место из найденных вариантов, чтобы сохранить координаты.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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

        locationStatusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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

        if (routeDurationState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        routeDurationState.message?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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
                val destination = selectedPlace ?: return@Button
                val transport = tripTransport ?: return@Button
                val routeMinutes = parsedRouteDuration ?: return@Button
                val remindMinutes = parsedRemindBefore ?: return@Button
                onTripCreated(
                    Trip(
                        id = initialTrip?.id ?: 0L,
                        place = destination.address,
                        name = tripName.trim().ifBlank { destination.address },
                        arrivalTimeMillis = arrivalTimeMillis(tripDate, tripTime),
                        transportMode = transport,
                        routeDurationMinutes = routeMinutes,
                        remindBeforeMinutes = remindMinutes,
                        safetyPercent = initialTrip?.safetyPercent ?: 10,
                        latitude = destination.latitude,
                        longitude = destination.longitude,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSave,
        ) {
            Text(submitButtonText)
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

private fun calendarFromMillis(millis: Long): Calendar =
    Calendar.getInstance().apply {
        timeInMillis = millis
    }

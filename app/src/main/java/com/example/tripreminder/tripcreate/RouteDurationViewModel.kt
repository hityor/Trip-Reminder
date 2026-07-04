package com.example.tripreminder.tripcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.UserLocation
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.runtime.Error
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import com.yandex.mapkit.transport.masstransit.Route as MasstransitRoute
import com.yandex.mapkit.transport.masstransit.Session as MasstransitSession

class RouteDurationViewModel : ViewModel() {
    private var drivingSession: DrivingSession? = null
    private var pedestrianSession: MasstransitSession? = null
    private var timeoutJob: Job? = null
    private var requestId = 0

    private val _state = MutableStateFlow(RouteDurationState())
    val state: StateFlow<RouteDurationState> = _state.asStateFlow()

    fun calculateDuration(
        destination: PlaceSearchResult,
        transportMode: TransportMode,
        userLocation: UserLocation?,
    ) {
        cancelActiveRequest()
        val currentRequestId = ++requestId

        if (transportMode == TransportMode.PublicTransport) {
            _state.value = RouteDurationState(message = "Для общественного транспорта время пока вводится вручную.")
            return
        }

        _state.value = RouteDurationState(isLoading = true)
        startTimeout(currentRequestId)

        val startPoint = userLocation?.let { location ->
            Point(location.latitude, location.longitude)
        } ?: KRASNOYARSK_START_POINT
        val points = listOf(
            RequestPoint(startPoint, RequestPointType.WAYPOINT, null, null),
            RequestPoint(Point(destination.latitude, destination.longitude), RequestPointType.WAYPOINT, null, null),
        )

        when (transportMode) {
            TransportMode.Car,
            TransportMode.Taxi -> requestDrivingDuration(points, currentRequestId)
            TransportMode.Walking -> requestWalkingDuration(points, currentRequestId)
            TransportMode.PublicTransport -> Unit
        }
    }

    fun clear() {
        cancelActiveRequest()
        requestId += 1
        _state.value = RouteDurationState()
    }

    override fun onCleared() {
        cancelActiveRequest()
        super.onCleared()
    }

    private fun requestDrivingDuration(points: List<RequestPoint>, currentRequestId: Int) {
        try {
            val router = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
            drivingSession = router.requestRoutes(
                points,
                DrivingOptions(),
                VehicleOptions(),
                object : DrivingSession.DrivingRouteListener {
                    override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                        val minutes = routes.firstOrNull()?.metadata?.weight?.time?.value?.let(::secondsToMinutes)
                        finishRequest(currentRequestId, minutes)
                    }

                    override fun onDrivingRoutesError(error: Error) {
                        finishRequest(currentRequestId, null, "Не удалось рассчитать время на авто.")
                    }
                },
            )
        } catch (_: RuntimeException) {
            finishRequest(currentRequestId, null, "Не удалось запустить расчет маршрута.")
        }
    }

    private fun requestWalkingDuration(points: List<RequestPoint>, currentRequestId: Int) {
        try {
            val router = TransportFactory.getInstance().createPedestrianRouter()
            pedestrianSession = router.requestRoutes(
                points,
                TimeOptions(),
                false,
                object : MasstransitSession.RouteListener {
                    override fun onMasstransitRoutes(routes: List<MasstransitRoute>) {
                        val minutes = routes.firstOrNull()?.metadata?.weight?.time?.value?.let(::secondsToMinutes)
                        finishRequest(currentRequestId, minutes)
                    }

                    override fun onMasstransitRoutesError(error: Error) {
                        finishRequest(currentRequestId, null, "Не удалось рассчитать время пешком.")
                    }
                },
            )
        } catch (_: RuntimeException) {
            finishRequest(currentRequestId, null, "Не удалось запустить расчет маршрута.")
        }
    }

    private fun startTimeout(currentRequestId: Int) {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(ROUTE_TIMEOUT_MILLIS)
            if (currentRequestId == requestId && _state.value.isLoading) {
                cancelActiveRequest()
                _state.value = RouteDurationState(message = "Расчет маршрута не ответил. Время можно ввести вручную.")
            }
        }
    }

    private fun finishRequest(
        currentRequestId: Int,
        durationMinutes: Int?,
        fallbackMessage: String? = null,
    ) {
        if (currentRequestId != requestId) return

        timeoutJob?.cancel()
        _state.value = if (durationMinutes != null) {
            RouteDurationState(durationMinutes = durationMinutes)
        } else {
            RouteDurationState(message = fallbackMessage ?: "Маршрут не найден. Время можно ввести вручную.")
        }
    }

    private fun cancelActiveRequest() {
        timeoutJob?.cancel()
        drivingSession?.cancel()
        pedestrianSession?.cancel()
        drivingSession = null
        pedestrianSession = null
    }

    private fun secondsToMinutes(seconds: Double): Int =
        ceil(seconds / SECONDS_IN_MINUTE).toInt().coerceAtLeast(1)

    companion object {
        private const val ROUTE_TIMEOUT_MILLIS = 12_000L
        private const val SECONDS_IN_MINUTE = 60.0
        private val KRASNOYARSK_START_POINT = Point(56.0106, 92.8526)
    }
}

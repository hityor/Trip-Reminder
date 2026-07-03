package com.example.tripreminder.tripcreate

import androidx.lifecycle.ViewModel
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.firstOrNull
import com.yandex.mapkit.transport.masstransit.Route as MasstransitRoute

class RouteViewModel : ViewModel() {

    private val drivingRouter: DrivingRouter = DirectionsFactory.getInstance().createDrivingRouter(
        DrivingRouterType.COMBINED)
    private val pedestrianRouter: PedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
    private var drivingSession: DrivingSession? = null
    private var pedestrianSession: com.yandex.mapkit.transport.masstransit.Session? = null
    private val _carTimeMinutes = MutableStateFlow<Int?>(null)
    val carTimeMinutes: StateFlow<Int?> = _carTimeMinutes

    private val _walkTimeMinutes = MutableStateFlow<Int?>(null)
    val walkTimeMinutes: StateFlow<Int?> = _walkTimeMinutes

    fun calculateTime(userLocation: Point, destination: Point) {
        val points = listOf(
            RequestPoint(userLocation, RequestPointType.WAYPOINT, null, null),
            RequestPoint(destination, RequestPointType.WAYPOINT, null, null)
        )


        drivingSession?.cancel()
        drivingSession = drivingRouter.requestRoutes(
            points, DrivingOptions(), VehicleOptions(),
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(routes: List<DrivingRoute>) {
                    routes.firstOrNull()?.let {
                        _carTimeMinutes.value = (it.metadata.weight.time.value / 60).toInt()
                    }
                }
                override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {}
            }
        )


        pedestrianSession?.cancel()
        pedestrianSession = pedestrianRouter.requestRoutes(
            points,
            TimeOptions(),
            false,
            object : com.yandex.mapkit.transport.masstransit.Session.RouteListener {
                override fun onMasstransitRoutes(routes: List<MasstransitRoute>) {
                    routes.firstOrNull()?.let {
                        _walkTimeMinutes.value = (it.metadata.weight.time.value / 60).toInt()
                    }
                }
                override fun onMasstransitRoutesError(error: com.yandex.runtime.Error) {}
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        drivingSession?.cancel()
        pedestrianSession?.cancel()
    }
}
package com.example.tripreminder.data

enum class TransportMode {
    Car,
    PublicTransport,
    Walking,
    Taxi,
}

data class Trip(
    val id: Long = 0,
    val place: String,
    val arrivalTimeMillis: Long,
    val transportMode: TransportMode,
    val routeDurationMinutes: Int,
    val remindBeforeMinutes: Int,
    val safetyPercent: Int = 10,
) {
    val safetyMinutes: Int
        get() = (routeDurationMinutes * safetyPercent + 99) / 100

    val totalBeforeArrivalMinutes: Int
        get() = routeDurationMinutes + remindBeforeMinutes + safetyMinutes

    val leaveTimeMillis: Long
        get() = arrivalTimeMillis - totalBeforeArrivalMinutes * MILLIS_IN_MINUTE

    companion object {
        private const val MILLIS_IN_MINUTE = 60_000L
    }
}

enum class TripStatus {
    Planned,
    Soon,
    TimeToLeave,
    Past,
}

fun Trip.statusAt(nowMillis: Long): TripStatus {
    if (nowMillis >= arrivalTimeMillis) return TripStatus.Past
    if (nowMillis >= leaveTimeMillis) return TripStatus.TimeToLeave

    val soonWindowMillis = 30 * 60_000L
    return if (leaveTimeMillis - nowMillis <= soonWindowMillis) {
        TripStatus.Soon
    } else {
        TripStatus.Planned
    }
}

package com.example.tripreminder.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TripStorage(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadTrips(): List<Trip>? {
        val rawTrips = preferences.getString(KEY_TRIPS, null) ?: return null

        return runCatching {
            val jsonTrips = JSONArray(rawTrips)
            buildList {
                for (index in 0 until jsonTrips.length()) {
                    add(jsonTrips.getJSONObject(index).toTrip())
                }
            }
        }.getOrNull()
    }

    fun saveTrips(trips: List<Trip>) {
        val jsonTrips = JSONArray()
        trips.forEach { trip ->
            jsonTrips.put(trip.toJson())
        }

        preferences.edit()
            .putString(KEY_TRIPS, jsonTrips.toString())
            .apply()
    }

    private fun Trip.toJson(): JSONObject = JSONObject()
        .put(KEY_ID, id)
        .put(KEY_PLACE, place)
        .put(KEY_NAME, name)
        .put(KEY_ARRIVAL_TIME_MILLIS, arrivalTimeMillis)
        .put(KEY_TRANSPORT_MODE, transportMode.name)
        .put(KEY_ROUTE_DURATION_MINUTES, routeDurationMinutes)
        .put(KEY_REMIND_BEFORE_MINUTES, remindBeforeMinutes)
        .put(KEY_SAFETY_PERCENT, safetyPercent)
        .put(KEY_LATITUDE, latitude)
        .put(KEY_LONGITUDE, longitude)

    private fun JSONObject.toTrip(): Trip = Trip(
        id = getLong(KEY_ID),
        place = getString(KEY_PLACE),
        name = optString(KEY_NAME, getString(KEY_PLACE)),
        arrivalTimeMillis = getLong(KEY_ARRIVAL_TIME_MILLIS),
        transportMode = runCatching {
            TransportMode.valueOf(getString(KEY_TRANSPORT_MODE))
        }.getOrDefault(TransportMode.PublicTransport),
        routeDurationMinutes = getInt(KEY_ROUTE_DURATION_MINUTES),
        remindBeforeMinutes = getInt(KEY_REMIND_BEFORE_MINUTES),
        safetyPercent = optInt(KEY_SAFETY_PERCENT, DEFAULT_SAFETY_PERCENT),
        latitude = optionalDouble(KEY_LATITUDE),
        longitude = optionalDouble(KEY_LONGITUDE),
    )

    private fun JSONObject.optionalDouble(key: String): Double? =
        if (has(key) && !isNull(key)) getDouble(key) else null

    private companion object {
        const val PREFERENCES_NAME = "trip_storage"
        const val KEY_TRIPS = "trips"
        const val KEY_ID = "id"
        const val KEY_PLACE = "place"
        const val KEY_NAME = "name"
        const val KEY_ARRIVAL_TIME_MILLIS = "arrivalTimeMillis"
        const val KEY_TRANSPORT_MODE = "transportMode"
        const val KEY_ROUTE_DURATION_MINUTES = "routeDurationMinutes"
        const val KEY_REMIND_BEFORE_MINUTES = "remindBeforeMinutes"
        const val KEY_SAFETY_PERCENT = "safetyPercent"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val DEFAULT_SAFETY_PERCENT = 10
    }
}

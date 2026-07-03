package com.example.tripreminder.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.notifications.TripNotificationScheduler.Companion.EXTRA_ARRIVAL_TIME_MILLIS
import com.example.tripreminder.notifications.TripNotificationScheduler.Companion.EXTRA_PLACE
import com.example.tripreminder.notifications.TripNotificationScheduler.Companion.EXTRA_ROUTE_DURATION_MINUTES
import com.example.tripreminder.notifications.TripNotificationScheduler.Companion.EXTRA_TRANSPORT_MODE
import com.example.tripreminder.notifications.TripNotificationScheduler.Companion.EXTRA_TRIP_ID

class TripNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getLongExtra(EXTRA_TRIP_ID, 0L)
        val place = intent.getStringExtra(EXTRA_PLACE).orEmpty()
        val arrivalTimeMillis = intent.getLongExtra(EXTRA_ARRIVAL_TIME_MILLIS, 0L)
        val routeDurationMinutes = intent.getIntExtra(EXTRA_ROUTE_DURATION_MINUTES, 0)
        val transportMode = intent.getStringExtra(EXTRA_TRANSPORT_MODE)
            ?.let { modeName -> TransportMode.valueOf(modeName) }
            ?: TransportMode.PublicTransport

        TripNotificationScheduler(context.applicationContext).apply {
            createNotificationChannel()
            showTripNotification(
                tripId = tripId,
                place = place,
                arrivalTimeMillis = arrivalTimeMillis,
                routeDurationMinutes = routeDurationMinutes,
                transportMode = transportMode,
            )
        }
    }
}

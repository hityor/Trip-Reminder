package com.example.tripreminder.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.tripreminder.MainActivity
import com.example.tripreminder.R
import com.example.tripreminder.data.TransportMode
import com.example.tripreminder.data.Trip
import com.example.tripreminder.data.TripStatus
import com.example.tripreminder.data.statusAt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripNotificationScheduler(private val context: Context) {
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о поездках",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Сообщает, когда пора выходить на поездку"
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun canPostNotifications(): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun scheduleTripNotifications(trips: List<Trip>, nowMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        trips.forEach { trip ->
            if (trip.leaveTimeMillis <= nowMillis) return@forEach

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                trip.leaveTimeMillis,
                pendingIntentForTrip(trip),
            )
        }
    }

    fun showDueTripNotifications(trips: List<Trip>, nowMillis: Long) {
        trips
            .filter { trip -> trip.statusAt(nowMillis) == TripStatus.TimeToLeave }
            .forEach { trip -> showTripNotification(trip) }
    }

    fun cancelTripNotification(trip: Trip) {
        val pendingIntent = pendingIntentForTrip(trip)
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        pendingIntent.cancel()
        NotificationManagerCompat.from(context).cancel(trip.id.toInt())
    }

    fun showTripNotification(trip: Trip) {
        showTripNotification(
            tripId = trip.id,
            place = trip.place,
            arrivalTimeMillis = trip.arrivalTimeMillis,
            routeDurationMinutes = trip.routeDurationMinutes,
            transportMode = trip.transportMode,
        )
    }

    fun showTripNotification(
        tripId: Long,
        place: String,
        arrivalTimeMillis: Long,
        routeDurationMinutes: Int,
        transportMode: TransportMode,
    ) {
        if (!canPostNotifications()) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            tripId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val arrivalTime = formatTime(arrivalTimeMillis)
        val text = "Прибыть к $arrivalTime, в пути ${minutesText(routeDurationMinutes)}"
        val fullText = "Пора выходить: $place. $text. Транспорт: ${transportTitle(transportMode)}."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trip_notification)
            .setContentTitle("Пора выходить")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(tripId.toInt(), notification)
        } catch (_: SecurityException) {
            // Пользователь мог отозвать разрешение на уведомления после проверки.
        }
    }

    private fun pendingIntentForTrip(trip: Trip): PendingIntent {
        val intent = Intent(context, TripNotificationReceiver::class.java).apply {
            putExtra(EXTRA_TRIP_ID, trip.id)
            putExtra(EXTRA_PLACE, trip.place)
            putExtra(EXTRA_ARRIVAL_TIME_MILLIS, trip.arrivalTimeMillis)
            putExtra(EXTRA_ROUTE_DURATION_MINUTES, trip.routeDurationMinutes)
            putExtra(EXTRA_TRANSPORT_MODE, trip.transportMode.name)
        }

        return PendingIntent.getBroadcast(
            context,
            trip.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "trip_reminders"
        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_PLACE = "place"
        const val EXTRA_ARRIVAL_TIME_MILLIS = "arrival_time_millis"
        const val EXTRA_ROUTE_DURATION_MINUTES = "route_duration_minutes"
        const val EXTRA_TRANSPORT_MODE = "transport_mode"
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU")).format(Date(millis))

private fun minutesText(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours > 0 && rest > 0 -> "${hours} ч ${rest} мин"
        hours > 0 -> "${hours} ч"
        else -> "${rest} мин"
    }
}

private fun transportTitle(mode: TransportMode): String = when (mode) {
    TransportMode.Car -> "авто"
    TransportMode.PublicTransport -> "общественный транспорт"
    TransportMode.Walking -> "пешком"
    TransportMode.Taxi -> "такси"
}

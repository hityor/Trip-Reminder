package com.example.tripreminder

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class TripReminderApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.setLocale("ru_RU")
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
            MapKitFactory.initialize(this)
        }
    }
}

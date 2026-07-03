package com.example.tripreminder

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setLocale("ru_RU")
        MapKitFactory.setApiKey("cf9feb7f-db57-40e7-b940-406caeb58976")

    }
}
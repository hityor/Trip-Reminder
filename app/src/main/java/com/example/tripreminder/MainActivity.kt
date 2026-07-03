package com.example.tripreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.tripreminder.tripcreate.CreateScreen
import com.example.tripreminder.ui.theme.TripReminderTheme
import com.yandex.mapkit.MapKitFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(this)


        enableEdgeToEdge()
        setContent {
            TripReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CreateScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}


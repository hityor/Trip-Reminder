package com.example.tripreminder.tripcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun CreateScreen(modifier: Modifier = Modifier) {
    var tripName by remember { mutableStateOf("") }
    var tripDate by remember { mutableStateOf(Calendar.getInstance()) }
    var tripTime by remember { mutableStateOf(Calendar.getInstance()) }
    var tripTransport by remember { mutableStateOf("") }
    var notificationTime by remember { mutableStateOf(Calendar.getInstance()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = tripName,
            onValueChange = { tripName = it },
            label = { Text("Название поездки") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        DateField(
            selectedDate = tripDate,
            onDateSelected = { tripDate = it },
            modifier = Modifier.fillMaxWidth()
        )

        TimeField(
            selectedTime = tripTime,
            onTimeSelected = { tripTime = it },
            modifier = Modifier.fillMaxWidth(),
            title = "Время поездки"
        )

        TransportField(
            selectedTransport = tripTransport,
            onTransportSelected = { tripTransport = it },
            modifier = Modifier.fillMaxWidth()
        )

        TimeField(
            selectedTime = notificationTime,
            onTimeSelected = { notificationTime = it },
            modifier = Modifier.fillMaxWidth(),
            title = "Время уведомления"
        )

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Запланировать поездку")
        }
    }
}




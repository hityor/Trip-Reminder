package com.example.tripreminder.tripcreate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DateField(selectedDate: Calendar,
              onDateSelected: (Calendar) -> Unit,
              modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate.timeInMillis)
    val formatter = remember { SimpleDateFormat("дд.мм.гггг", Locale.getDefault()) }

    OutlinedTextField(
        value = formatter.format(selectedDate.time),
        onValueChange = {},
        readOnly = true,
        label = { Text("Дата поездки") },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
            }
        },
        modifier = modifier
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val newCalendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        onDateSelected(newCalendar)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
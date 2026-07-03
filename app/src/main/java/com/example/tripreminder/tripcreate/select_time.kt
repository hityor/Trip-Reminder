package com.example.tripreminder.tripcreate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    selectedTime: Calendar,
    onTimeSelected: (Calendar) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Время",
) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberTimePickerState(
        initialHour = selectedTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = selectedTime.get(Calendar.MINUTE),
        is24Hour = true,
    )
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.forLanguageTag("ru-RU")) }

    OutlinedTextField(
        value = formatter.format(selectedTime.time),
        onValueChange = {},
        readOnly = true,
        label = { Text(title) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "Выбрать время")
            }
        },
        modifier = modifier,
    )

    if (showPicker) {
        TimePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, state.hour)
                            set(Calendar.MINUTE, state.minute)
                        }
                        onTimeSelected(newCalendar)
                        showPicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Отмена")
                }
            },
        ) {
            TimePicker(state = state)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content,
    )
}

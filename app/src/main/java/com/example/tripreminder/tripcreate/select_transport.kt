package com.example.tripreminder.tripcreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.tripreminder.data.TransportMode

@Composable
fun TransportField(
    selectedTransport: TransportMode?,
    onTransportSelected: (TransportMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val transports = TransportMode.entries

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedTransport?.let { transportTitle(it) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Тип транспорта") },
            placeholder = { Text("Выберите транспорт") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать транспорт")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
        ) {
            transports.forEach { transport ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = transportTitle(transport),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onTransportSelected(transport)
                    },
                )
            }
        }
    }
}

private fun transportTitle(mode: TransportMode): String = when (mode) {
    TransportMode.Car -> "Автомобиль"
    TransportMode.PublicTransport -> "Общественный транспорт"
    TransportMode.Walking -> "Пешком"
    TransportMode.Taxi -> "Такси"
}

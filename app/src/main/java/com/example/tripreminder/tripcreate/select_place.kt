package com.example.tripreminder.tripcreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripreminder.data.PlaceData

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onPlaceSelected: (PlaceData) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.searchPlace(it) },
            label = { Text("Куда поедем?") },
            modifier = Modifier.fillMaxWidth()
        )

        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        results.forEach { place ->
            Text(
                text = place.address,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        query = place.address
                        onPlaceSelected(place)
                    }
                    .padding(16.dp)
            )
            HorizontalDivider()
        }
    }
}
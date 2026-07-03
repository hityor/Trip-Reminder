package com.example.tripreminder.tripcreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }

    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchPlace(it)
            },
            label = { Text("Куда отправляемся?") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }


        Column(modifier = Modifier.fillMaxWidth()) {
            results.forEach { placeName ->
                Text(
                    text = placeName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            query = placeName
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
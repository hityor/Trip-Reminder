package com.example.tripreminder.tripcreate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PlaceSearchField(
    place: String,
    onPlaceChange: (String) -> Unit,
    onPlaceSelected: (PlaceSearchResult) -> Unit,
    searchEnabled: Boolean,
    hasSelectedPlace: Boolean,
    modifier: Modifier = Modifier,
    viewModel: PlaceSearchViewModel = viewModel(),
) {
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorText by viewModel.errorText.collectAsState()
    val canShowEmptyResult = searchEnabled &&
        !isLoading &&
        errorText == null &&
        !hasSelectedPlace &&
        place.length >= 3 &&
        results.isEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = place,
            onValueChange = { value ->
                onPlaceChange(value)
                if (searchEnabled) {
                    viewModel.searchPlace(value)
                }
            },
            label = { Text("Место назначения") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (searchEnabled && isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (searchEnabled) {
            errorText?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            results.forEach { result ->
                Text(
                    text = result.address,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPlaceChange(result.address)
                            onPlaceSelected(result)
                            viewModel.clearResults()
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider()
            }

            if (canShowEmptyResult) {
                Text(
                    text = "Место не найдено. Попробуй уточнить запрос.",
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

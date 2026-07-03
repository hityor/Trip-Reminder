package com.example.tripreminder.tripcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaceSearchViewModel : ViewModel() {
    private var searchSession: Session? = null
    private var searchTimeoutJob: Job? = null
    private var searchRequestId = 0

    private val _searchResults = MutableStateFlow<List<PlaceSearchResult>>(emptyList())
    val searchResults: StateFlow<List<PlaceSearchResult>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorText = MutableStateFlow<String?>(null)
    val errorText: StateFlow<String?> = _errorText.asStateFlow()

    fun searchPlace(query: String) {
        if (query.length < MIN_QUERY_LENGTH) {
            clearResults()
            return
        }

        _isLoading.value = true
        _errorText.value = null
        searchSession?.cancel()
        val requestId = ++searchRequestId
        startTimeout(requestId)

        try {
            val searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
            val options = SearchOptions().apply {
                resultPageSize = 8
            }

            searchSession = searchManager.submit(
                query,
                KRASNOYARSK_SEARCH_AREA,
                options,
                searchListener(requestId),
            )
        } catch (error: RuntimeException) {
            finishSearch(
                requestId = requestId,
                results = emptyList(),
                errorText = "Поиск места сейчас недоступен",
            )
        }
    }

    fun clearResults() {
        searchRequestId += 1
        searchSession?.cancel()
        searchSession = null
        searchTimeoutJob?.cancel()
        _searchResults.value = emptyList()
        _errorText.value = null
        _isLoading.value = false
    }

    override fun onCleared() {
        searchSession?.cancel()
        searchTimeoutJob?.cancel()
        super.onCleared()
    }

    private fun searchListener(requestId: Int) = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val results = response.collection.children.mapNotNull { child ->
                val geoObject = child.obj ?: return@mapNotNull null
                val point = geoObject.geometry.firstOrNull()?.point ?: return@mapNotNull null
                val name = geoObject.name ?: return@mapNotNull null
                val description = geoObject.descriptionText
                val address = if (description.isNullOrBlank()) name else "$name, $description"

                PlaceSearchResult(
                    address = address,
                    latitude = point.latitude,
                    longitude = point.longitude,
                )
            }
            finishSearch(requestId, results)
        }

        override fun onSearchError(error: Error) {
            finishSearch(
                requestId = requestId,
                results = emptyList(),
                errorText = "Ошибка поиска места: ${error.javaClass.simpleName}: $error",
            )
        }
    }

    private fun startTimeout(requestId: Int) {
        searchTimeoutJob?.cancel()
        searchTimeoutJob = viewModelScope.launch {
            delay(SEARCH_TIMEOUT_MILLIS)
            if (requestId == searchRequestId && _isLoading.value) {
                searchSession?.cancel()
                _errorText.value = "Поиск не ответил. Проверь интернет или API-ключ."
                _isLoading.value = false
            }
        }
    }

    private fun finishSearch(
        requestId: Int,
        results: List<PlaceSearchResult>,
        errorText: String? = null,
    ) {
        if (requestId != searchRequestId) return

        searchTimeoutJob?.cancel()
        _searchResults.value = results
        _errorText.value = errorText
        _isLoading.value = false
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 3
        private const val SEARCH_TIMEOUT_MILLIS = 8_000L
        private val KRASNOYARSK_SEARCH_AREA = Geometry.fromBoundingBox(
            BoundingBox(
                Point(55.70, 92.10),
                Point(56.25, 93.40),
            ),
        )
    }
}

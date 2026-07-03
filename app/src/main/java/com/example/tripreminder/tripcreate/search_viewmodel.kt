package com.example.tripreminder.tripcreate

import androidx.lifecycle.ViewModel
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {

    private val searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    private var searchSession: Session? = null
    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val searchListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val results = response.collection.children.mapNotNull { child ->
                val geoObject = child.obj ?: return@mapNotNull null


                val toponymMetadata = geoObject.metadataContainer.getItem(ToponymObjectMetadata::class.java)
                toponymMetadata?.address?.formattedAddress ?: geoObject.name
            }
            _searchResults.value = results
            _isLoading.value = false
        }

        override fun onSearchError(error: Error) {
            _searchResults.value = emptyList()
            _isLoading.value = false
        }
    }

    fun searchPlace(query: String, userLocation: Point? = null) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        _isLoading.value = true
        searchSession?.cancel()

        val searchPoint = userLocation ?: Point(55.751244, 37.618423)


        val delta = 0.5
        val searchArea = Geometry.fromBoundingBox(
            BoundingBox(
                Point(searchPoint.latitude - delta, searchPoint.longitude - delta),
                Point(searchPoint.latitude + delta, searchPoint.longitude + delta)
            )
        )

        val options = SearchOptions().apply {
            resultPageSize = 10
        }

        searchSession = searchManager.submit(query, searchArea, options, searchListener)
    }

    override fun onCleared() {
        super.onCleared()
        searchSession?.cancel()
    }
}
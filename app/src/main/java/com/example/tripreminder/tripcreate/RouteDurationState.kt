package com.example.tripreminder.tripcreate

data class RouteDurationState(
    val isLoading: Boolean = false,
    val durationMinutes: Int? = null,
    val message: String? = null,
)

package com.max04.carburancenis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteCity(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

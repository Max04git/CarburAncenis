package com.max04.carburancenis.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimSearchItemDto(
    val lat: String? = null,
    val lon: String? = null,
    @SerialName("display_name") val displayName: String? = null,
)

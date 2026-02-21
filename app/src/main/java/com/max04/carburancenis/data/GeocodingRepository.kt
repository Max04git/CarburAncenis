package com.max04.carburancenis.data

import com.max04.carburancenis.data.model.GeocodedCity
import com.max04.carburancenis.data.network.NominatimApi

class GeocodingRepository(
    private val api: NominatimApi,
) {

    suspend fun suggestCities(query: String, limit: Int = 5): List<GeocodedCity> {
        val q = query.trim()
        if (q.length < 2) return emptyList()

        val result = try {
            api.search(query = q, limit = limit)
        } catch (_: Throwable) {
            return emptyList()
        }

        return result.mapNotNull { item ->
            val lat = item.lat?.toDoubleOrNull() ?: return@mapNotNull null
            val lon = item.lon?.toDoubleOrNull() ?: return@mapNotNull null
            GeocodedCity(
                label = formatLabel(q, item.displayName),
                latitude = lat,
                longitude = lon,
            )
        }
    }

    private fun formatLabel(query: String, displayName: String?): String {
        val dn = displayName?.trim().orEmpty()
        if (dn.isBlank()) return query
        val firstParts = dn.split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(3)

        return if (firstParts.isEmpty()) query else firstParts.joinToString(", ")
    }
}

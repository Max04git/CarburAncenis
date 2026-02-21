package com.max04.carburancenis.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.max04.carburancenis.data.network.PrixCarburantsApi
import com.max04.carburancenis.ui.StationUi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class PrixCarburantsRepository(
    private val api: PrixCarburantsApi,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadStationsAroundWithFuel(
        lat: Double,
        lon: Double,
        rangeMeters: Int,
        fuelShortName: String,
        maxAgeDays: Long,
        maxStations: Int = 25,
    ): List<StationUi> = coroutineScope {
        val latLon = "$lat,$lon"
        val around = fetchStationsAroundChunked(
            latLon = latLon,
            rangeMeters = rangeMeters,
        )
            .sortedBy { it.distance ?: it.distanceObj?.value ?: Int.MAX_VALUE }
            .take(maxStations)

        val details = around.map { station ->
            async {
                val stationDetails = api.getStationDetails(station.id)

                val matchingFuel = stationDetails.fuels.firstOrNull { fuel ->
                    fuel.available &&
                            fuel.price?.value != null &&
                            fuel.shortName.equals(fuelShortName, ignoreCase = true)
                }

                if (matchingFuel == null) return@async null

                val priceValue = matchingFuel.price?.value ?: return@async null

                val freshnessLabel = computeFreshnessLabel(
                    updateIsoInstant = matchingFuel.update?.value,
                    maxAgeDays = maxAgeDays,
                )
                    ?: return@async null

                val city = station.address?.cityLine
                    ?: stationDetails.address?.cityLine
                    ?: ""

                val street = station.address?.streetLine
                    ?: stationDetails.address?.streetLine
                    ?: ""

                val brandName = station.brand?.name
                    ?: stationDetails.brand?.name
                    ?: ""

                val stationNameRaw = station.name ?: stationDetails.name.orEmpty()
                val displayName = buildDisplayStationName(
                    brandName = brandName,
                    stationName = stationNameRaw,
                )

                val distanceMeters = station.distance ?: station.distanceObj?.value
                val distanceKm = if (distanceMeters != null) distanceMeters / 1000.0 else 0.0

                StationUi(
                    name = displayName,
                    brandName = brandName,
                    street = street,
                    city = city,
                    distanceKm = distanceKm,
                    fuelLabel = matchingFuel.shortName ?: fuelShortName,
                    updateLabel = freshnessLabel,
                    priceEuro = formatPriceEuro(priceValue),
                ) to priceValue
            }
        }.awaitAll()

        details
            .filterNotNull()
            .sortedBy { it.second }
            .map { it.first }
    }

    private suspend fun fetchStationsAroundChunked(
        latLon: String,
        rangeMeters: Int,
    ): List<com.max04.carburancenis.data.network.StationAroundDto> {
        if (rangeMeters <= 0) return emptyList()

        val maxChunkSize = 10_000
        val chunks = mutableListOf<com.max04.carburancenis.data.network.StationAroundDto>()

        var start = 0
        while (start <= rangeMeters) {
            val end = minOf(start + maxChunkSize - 1, rangeMeters)
            val rangeHeader = "m=$start-$end"

            Log.d("PrixCarburantsRepository", "Fetching around with Range: $rangeHeader")

            val part = api.getStationsAround(
                latLon = latLon,
                range = rangeHeader,
            )

            chunks += part
            start = end + 1
        }

        return chunks
            .distinctBy { it.id }
    }

    private fun formatPriceEuro(price: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
            minimumFractionDigits = 3
            maximumFractionDigits = 3
        }
        return "${nf.format(price)} €"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun computeFreshnessLabel(updateIsoInstant: String?, maxAgeDays: Long): String? {
        if (updateIsoInstant.isNullOrBlank()) return null

        val updateInstant = try {
            Instant.parse(updateIsoInstant)
        } catch (_: Throwable) {
            return null
        }

        val zone = ZoneId.systemDefault()
        val updateDate = updateInstant.atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)

        val ageDays = kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(updateDate, today))
        if (ageDays > maxAgeDays) return null

        return when (ageDays) {
            0L -> "Auj"
            1L -> "Hier"
            else -> "J-$ageDays"
        }
    }

    private fun buildDisplayStationName(brandName: String, stationName: String): String {
        val brand = brandName.trim()
        val station = stationName.trim()

        val brandIsGeneric = brand.equals("Indépendant sans enseigne", ignoreCase = true)
        if (brand.isBlank() || brandIsGeneric) return station

        val stationContainsBrand = station.contains(brand, ignoreCase = true)
        return if (stationContainsBrand) station else "$brand · $station"
    }
}

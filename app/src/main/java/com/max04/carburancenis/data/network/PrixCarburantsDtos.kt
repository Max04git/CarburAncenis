package com.max04.carburancenis.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrandDto(
    val id: Long,
    val name: String,
    val shortName: String? = null,
    @SerialName("nb_stations") val nbStations: Int? = null,
)

@Serializable
data class AddressDto(
    @SerialName("street_line") val streetLine: String? = null,
    @SerialName("city_line") val cityLine: String? = null,
)

@Serializable
data class CoordinatesDto(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class DistanceValueDto(
    val value: Int? = null,
    val text: String? = null,
)

@Serializable
data class StationAroundDto(
    val id: Long,
    @SerialName("Brand") val brand: BrandDto? = null,
    val type: String? = null,
    val name: String? = null,
    @SerialName("Address") val address: AddressDto? = null,
    @SerialName("Coordinates") val coordinates: CoordinatesDto? = null,
    val distance: Int? = null,
    @SerialName("Distance") val distanceObj: DistanceValueDto? = null,
)

@Serializable
data class StationDetailsDto(
    val id: Long,
    @SerialName("Brand") val brand: BrandDto? = null,
    val type: String? = null,
    val name: String? = null,
    @SerialName("Address") val address: AddressDto? = null,
    @SerialName("Coordinates") val coordinates: CoordinatesDto? = null,
    @SerialName("Fuels") val fuels: List<FuelDto> = emptyList(),
)

@Serializable
data class FuelDto(
    val id: Long,
    val name: String? = null,
    val shortName: String? = null,
    val type: String? = null,
    val picto: String? = null,
    @SerialName("Update") val update: UpdateDto? = null,
    val available: Boolean = false,
    @SerialName("Price") val price: PriceDto? = null,
)

@Serializable
data class UpdateDto(
    val value: String? = null,
    val text: String? = null,
)

@Serializable
data class PriceDto(
    val value: Double? = null,
    val currency: String? = null,
    val unit: String? = null,
    val text: String? = null,
)

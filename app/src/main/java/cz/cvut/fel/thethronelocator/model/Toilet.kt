package cz.cvut.fel.thethronelocator.model

import java.time.LocalDateTime

data class Toilet(
    val name: String? = null,
    val type: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val features: List<Feature>? = emptyList(),
    val openingTime: LocalDateTime? = null,
    val closingTime: LocalDateTime? = null,
)
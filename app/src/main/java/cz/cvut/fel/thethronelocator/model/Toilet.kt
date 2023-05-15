package cz.cvut.fel.thethronelocator.model

import java.time.LocalDateTime

data class Toilet(
    val name:String,
    val type:String,
    val address:String,
    val latitude: Double,
    val longitude: Double,
    val features:List<Feature>,
    val openingTime: LocalDateTime,
    val closingTime: LocalDateTime,
)
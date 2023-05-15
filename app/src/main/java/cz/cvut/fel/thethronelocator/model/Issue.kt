package cz.cvut.fel.thethronelocator.model

data class Issue(
    val issue: Int,
    val author: User,
    val toilet: Toilet
)
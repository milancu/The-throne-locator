package cz.cvut.fel.thethronelocator.model

data class Rating(
    val rating: Float,
    val review: String,
    val author: User,
    val toilet: Toilet
)
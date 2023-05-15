package cz.cvut.fel.thethronelocator.model

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val preferences: List<Feature>,
    val favourites: List<Toilet>
)
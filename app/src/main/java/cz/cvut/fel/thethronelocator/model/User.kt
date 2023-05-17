package cz.cvut.fel.thethronelocator.model

data class User(
    val firstName: String? = "Anonymous",
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val preferences: List<Feature>? = emptyList(),
    val favourites: List<Toilet>? = emptyList()
)
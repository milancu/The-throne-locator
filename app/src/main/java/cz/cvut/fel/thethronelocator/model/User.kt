package cz.cvut.fel.thethronelocator.model

class User(
    var id:String? = null,
    val name: String? = null,
    val record: Int? = null,
    val favourites: List<Toilet>? = null,
    val imgLink: String? = null
)
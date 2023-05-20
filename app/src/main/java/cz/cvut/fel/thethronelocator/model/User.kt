package cz.cvut.fel.thethronelocator.model

class User(
    var id:String? = null,
    val name: String? = null,
    var record: Int? = null,
    var favouritesList: List<String>? = null,
    val imgLink: String? = null
)
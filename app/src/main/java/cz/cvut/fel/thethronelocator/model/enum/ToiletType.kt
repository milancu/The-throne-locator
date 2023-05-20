package cz.cvut.fel.thethronelocator.model.enum

enum class ToiletType (name:String){
    STANDALONE("standalone"),
    IN_A_PARK("inapark"),
    IN_SHOPPING_MALL("inshoppingmall");

    companion object {
        fun getTypeByName(name: String) = valueOf(name.uppercase().replace(" ", "_"))
    }
}
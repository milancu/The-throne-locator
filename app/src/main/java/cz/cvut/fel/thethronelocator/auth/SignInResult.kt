package cz.cvut.fel.thethronelocator.auth

import android.net.Uri

data class SignInResult (
    val user: UserData? = null,
    val errorMessage: String? = null,
)

data class UserData(
    val userId: String,
    val name: String?,
    val profilePicture: Uri?,
    val username: String?,
    val isAnonymous: Boolean,
    val imgUrl:String
)
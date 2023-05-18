package cz.cvut.fel.thethronelocator.auth

import android.graphics.drawable.Drawable

data class SignInResult (
    val user: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val name: String?,
    val profilePicture: Drawable?,
    val username: String?,
    val isAnonymous: Boolean,
)
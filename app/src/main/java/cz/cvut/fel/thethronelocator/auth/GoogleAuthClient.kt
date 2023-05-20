package cz.cvut.fel.thethronelocator.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import cz.cvut.fel.thethronelocator.R
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun singIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    private fun getCredentialFromIntent(intent: Intent): AuthCredential {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken

        return GoogleAuthProvider.getCredential(googleIdToken, null)
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return executeSignIn {
            auth.signInWithCredential(getCredentialFromIntent(intent)).await().user
        }
    }

    suspend fun signInAnonymously(): SignInResult {
        return executeSignIn {
            auth.signInAnonymously().await().user
        }
    }

    suspend fun linkWithIntent(intent: Intent): SignInResult {
        val credential = getCredentialFromIntent(intent)

        return executeSignIn {
            try {
                auth.currentUser!!.linkWithCredential(credential).await().user
            } catch (e: FirebaseAuthUserCollisionException) {
                // If there is another user account associated with the given credential
                // Then login in
                auth.signInWithCredential(credential).await().user
            }
        }
    }


    private fun getUserImageUri(user:FirebaseUser):String{
        return user.photoUrl.toString()
    }



    private suspend fun executeSignIn(signInAction: suspend () -> FirebaseUser?): SignInResult {
        return try {
            val user = signInAction.invoke()
            SignInResult(
                user = user?.run {
                    UserData(
                        userId = uid,
                        username = email,
                        name = displayName,
                        profilePicture = getUserImage(this),
                        isAnonymous = isAnonymous,
                        imgUrl = ""
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                user = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut(): SignInResult {
        return try {
            oneTapClient.signOut().await()
            auth.signOut()
            signInAnonymously()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            Toast.makeText(
                context,
                "Something went wrong.",
                Toast.LENGTH_SHORT,
            ).show()
            SignInResult(
                user = null,
                errorMessage = e.message
            )
        }
    }

    fun getUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = email,
            name = displayName,
            profilePicture = getUserImage(this),
            isAnonymous = isAnonymous,
            imgUrl = getUserImageUri(this)
        )
    }


    private fun getUserImage(user: FirebaseUser): Drawable? {
        var drawable = getDrawable(context, R.drawable.avatar)

        // Set the icon using the photoUrl
        user.photoUrl?.run {
            Glide.with(context)
                .load(this)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        // Set the loaded drawable as the menu item icon
                        drawable = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        drawable = getDrawable(context, R.drawable.avatar)
                    }
                })
        }

        return drawable
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Do not show only accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build())
            // Auto-select if only one google account
            .setAutoSelectEnabled(true)
            .build()
    }
}
package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.WelcomeActivityBinding
import kotlinx.coroutines.launch


open class WelcomeActivity : FragmentActivity() {
    private lateinit var binding: WelcomeActivityBinding
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signInClient = Identity.getSignInClient(this)
        googleAuthClient = GoogleAuthClient(this, signInClient)

        binding.loginGoogle.setOnClickListener {
            signInGoogle()
        }

        binding.loginAnon.setOnClickListener {
            signInAnonymously()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = googleAuthClient.getUser()
        if (currentUser != null) {
            redirect()
        }
    }

    private fun signInGoogle() {
        lifecycleScope.launch {
            val signIntentSender = googleAuthClient.singIn()
            launcher.launch(
                IntentSenderRequest.Builder(
                    signIntentSender ?: return@launch
                ).build()
            )
        }
    }

    private fun signInAnonymously() {
        lifecycleScope.launch {
            val signInResult = googleAuthClient.signInAnonymously()
            if (signInResult.user != null) {
                redirect()
            } else {
                Toast.makeText(this@WelcomeActivity, signInResult.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val signInResult =
                    googleAuthClient.signInWithIntent(result.data ?: return@launch)
                if (signInResult.user != null) {
                    redirect()
                } else {
                    Toast.makeText(this@WelcomeActivity, signInResult.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun redirect() {
        val intent = Intent(this, BaseActivity::class.java)
        startActivity(intent)
        finish()
    }
}
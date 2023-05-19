package cz.cvut.fel.thethronelocator.ui

import UserViewModel
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.FragmentWelcomeBinding
import kotlinx.coroutines.launch


open class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    private lateinit  var binding: FragmentWelcomeBinding
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentWelcomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInClient = Identity.getSignInClient(requireActivity())
        googleAuthClient = GoogleAuthClient(requireActivity(), signInClient)

        binding.loginGoogle.setOnClickListener {
            signInGoogle()
        }

        binding.loginAnon.setOnClickListener {
            signInAnonymously()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = googleAuthClient.getUser()
        if (currentUser != null) {
            userViewModel.updateUserData(currentUser)
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
                userViewModel.updateUserData(signInResult.user)
                redirect()
            } else {
                Toast.makeText(activity, signInResult.errorMessage, Toast.LENGTH_SHORT).show()
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
                    userViewModel.updateUserData(signInResult.user)
                    redirect()
                } else {
                    Toast.makeText(activity, signInResult.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun redirect() {
        findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
    }
}
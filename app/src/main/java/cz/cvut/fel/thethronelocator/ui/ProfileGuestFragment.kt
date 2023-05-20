package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.FragmentProfileGuestBinding
import cz.cvut.fel.thethronelocator.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileGuestFragment: Fragment(R.layout.fragment_profile_guest) {
    private lateinit var binding: FragmentProfileGuestBinding
    private lateinit var signInClient: SignInClient
    private lateinit var navController: NavController
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var savedStateHandle: SavedStateHandle
    private val userRepository = UserRepository()

    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(ProfileFragment.LOGOUT_SUCCESSFUL)
            .observe(currentBackStackEntry) { success ->
                if (!success) {
                    val startDestination = navController.graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileGuestBinding.bind(view)

        savedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false

        signInClient = Identity.getSignInClient(requireActivity())
        googleAuthClient = GoogleAuthClient(requireContext(), signInClient)

        binding.button.setOnClickListener {
            linkWithGoogle()
        }
    }

    private fun linkWithGoogle() {
        lifecycleScope.launch {
            val signIntentSender = googleAuthClient.singIn()
            launcher.launch(
                IntentSenderRequest.Builder(
                    signIntentSender ?: return@launch
                ).build()
            )
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val signInResult =
                    googleAuthClient.linkWithIntent(result.data ?: return@launch)
                if (signInResult.user != null) {
                    savedStateHandle[LOGIN_SUCCESSFUL] = true
                    userRepository.createUser(
                        googleAuthClient = googleAuthClient,
                        onError = {
                            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                        },
                        onSuccess = {

                        }
                    )
                    navController.popBackStack()
                } else {
                    Toast.makeText(activity, signInResult.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.FragmentWelcomeBinding
import cz.cvut.fel.thethronelocator.repository.UserRepository
import kotlinx.coroutines.launch


open class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var database: FirebaseDatabase

    private val userRepository = UserRepository()
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
        database = FirebaseDatabase.getInstance()
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
                    redirect()
                } else {
                    Toast.makeText(activity, signInResult.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun createUser() {
        val currentUser = googleAuthClient.getUser()
        if (currentUser != null) {
            val userDataRef = database.getReference("users/${currentUser.userId}")

            val currentUserRef = userDataRef.child(currentUser.userId)

            currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        userDataRef.child("name").setValue(currentUser.name)
                        userDataRef.child("record").setValue(0)
                        userDataRef.child("profilePicture").setValue(currentUser.profilePicture)
                        userDataRef.child("favourites").setValue(null)

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${databaseError.message}")
                    Toast.makeText(activity, "Failed to login", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun redirect() {
        findNavController().navigate(R.id.action_welcomeFragment_to_mainFragment)
        userRepository.createUser(
            googleAuthClient = googleAuthClient,
            onError = {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
            },
            onSuccess = {

            }
        )
    }
}
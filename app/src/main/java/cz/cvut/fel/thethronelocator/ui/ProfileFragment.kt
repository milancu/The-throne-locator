package cz.cvut.fel.thethronelocator.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.FavouritesListAdapter
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.auth.UserData
import cz.cvut.fel.thethronelocator.databinding.FragmentProfileBinding
import cz.cvut.fel.thethronelocator.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var navController: NavController
    private lateinit var currentUser: UserData
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var favouritesList: RecyclerView
    private val userRepository = UserRepository()

    companion object {
        const val LOGOUT_SUCCESSFUL: String = "LOGOUT_SUCCESSFUL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(ProfileGuestFragment.LOGIN_SUCCESSFUL)
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
        binding = FragmentProfileBinding.bind(view)

        savedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGOUT_SUCCESSFUL] = false

        signInClient = Identity.getSignInClient(requireActivity())
        googleAuthClient = GoogleAuthClient(requireContext(), signInClient)

        currentUser = googleAuthClient.getUser()!!
        if (currentUser.isAnonymous) {
            navController.navigate(R.id.profileGuestFragment)
        } else {
            showUserDetails()
            favouritesList = binding.favouriteList
            userRepository.getUserById(currentUser.userId, callback = {
                val favouritesListAdapter = FavouritesListAdapter(it!!.favouritesList)
                favouritesList.adapter = favouritesListAdapter
                favouritesList.layoutManager = LinearLayoutManager(context)
                val count = it.favouritesList?.size ?: 0
                binding.favouriteListCount.text = "Favourites(${count})"
            })
        }
    }

    private fun showUserDetails() {
        binding.fullName.text = currentUser.name
        binding.username.text = currentUser.username
        currentUser.profilePicture?.run {
            Glide.with(requireContext())
                .load(this)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.profileImage)
        }

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                val signInResult = googleAuthClient.signOut()
                if (signInResult.user != null) {
                    navController.popBackStack()
                } else {
                    Toast.makeText(requireActivity(), signInResult.errorMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
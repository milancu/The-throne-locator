package cz.cvut.fel.thethronelocator.ui

import UserViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.FragmentMainBinding


open class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    private lateinit var navController: NavController
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private val userViewModel: UserViewModel by activityViewModels()
    var clickCount = 0
    var lastClickTime: Long = 0
    val REQUIRED_CLICKS = 7
    val TIME_LIMIT: Long = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentMainBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInClient = Identity.getSignInClient(requireActivity())
        googleAuthClient = GoogleAuthClient(requireActivity(), signInClient)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()

        val mainNavController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.searchBar)
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_bar_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return item.onNavDestinationSelected(navController)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.profileFragment)?.icon = userViewModel.state.value?.profilePicture
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.menu.findItem(R.id.mapFragment).setOnMenuItemClickListener  { item ->
            if (item.itemId == R.id.mapFragment) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > TIME_LIMIT) {
                    clickCount = 1
                } else {
                    clickCount++
                }

                lastClickTime = currentTime

                if (clickCount == REQUIRED_CLICKS) {
                    mainNavController.navigate(R.id.action_mainFragment_to_cookieClickerFragment)
                }
            }
            false
        }

        userViewModel.state.observe(viewLifecycleOwner) {
            requireActivity().invalidateOptionsMenu()
        }
    }
}
package cz.cvut.fel.thethronelocator.ui

import android.content.ContentValues.TAG
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.ToiletListAdapter
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.auth.UserData
import cz.cvut.fel.thethronelocator.databinding.FragmentMainBinding
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.viewmodel.ToiletViewModel
import cz.cvut.fel.thethronelocator.viewmodel.ToiletViewModelFactory


open class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var binding: FragmentMainBinding
    private lateinit var navController: NavController
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var currentUser: UserData
    private lateinit var viewModel: ToiletViewModel

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
        val factory = ToiletViewModelFactory(ToiletRepository())
        viewModel = ViewModelProvider(this, factory)[ToiletViewModel::class.java]

        viewModel.toiletPoints.observe(viewLifecycleOwner) {
            val toiletAdapter = ToiletListAdapter(it)
            binding.toiletList.adapter = toiletAdapter
            binding.toiletList.layoutManager = LinearLayoutManager(requireContext())
        }

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()

        val mainNavController =
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

        currentUser = googleAuthClient.getUser()!!

        val searchBar = binding.searchBar
        val searchView = binding.searchView
        searchView.setupWithSearchBar(searchBar)
        searchBar.setOnMenuItemClickListener { menuItem: MenuItem? ->
            true
        }

        searchView
            .editText
            .setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
                performSearch(searchView.text.toString())
                false
            }

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.searchBar)
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_bar_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return item.onNavDestinationSelected(navController)
            }

            override fun onPrepareMenu(menu: Menu) {
                currentUser.profilePicture?.run {
                    Glide.with(requireContext())
                        .load(this)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(object : CustomTarget<Drawable>(100, 100) {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                menu.findItem(R.id.profileFragment)?.icon = resource
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                menu.findItem(R.id.profileFragment)?.icon =
                                    AppCompatResources.getDrawable(
                                        requireContext(),
                                        R.drawable.avatar
                                    )
                            }
                        })
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.menu.findItem(R.id.mapFragment).setOnMenuItemClickListener { item ->
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
    }

    private fun performSearch(query: String) {
        Log.d(TAG, "performSearch: $query")
        viewModel.getToiletByName(query)
    }
}
package cz.cvut.fel.thethronelocator.ui

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.ActivityBaseBinding

open class BaseActivity : FragmentActivity() {
    private lateinit var binding : ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.mapFragment) {
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                binding.searchBarWrapper.bringToFront()
                binding.fragmentContainerView.layoutParams = params

            } else {
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                params.setMargins(20, 0, 20, 0)
                params.addRule(RelativeLayout.BELOW, binding.searchBarWrapper.id)
                params.addRule(RelativeLayout.ABOVE, binding.bottomNavigationView.id)
                binding.fragmentContainerView.layoutParams = params
            }
        }
    }
}
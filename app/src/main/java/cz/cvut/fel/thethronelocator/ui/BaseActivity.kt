package cz.cvut.fel.thethronelocator.ui

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.ActivityBaseBinding

open class BaseActivity : FragmentActivity() {
    private lateinit var binding: ActivityBaseBinding
    var clickCount = 0
    var lastClickTime: Long = 0
    val REQUIRED_CLICKS = 7
    val TIME_LIMIT: Long = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.mapFragment) {
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
                params.setMargins(60, 0, 60, 0)
                params.addRule(RelativeLayout.BELOW, binding.searchBarWrapper.id)
                params.addRule(RelativeLayout.ABOVE, binding.bottomNavigationView.id)
                binding.fragmentContainerView.layoutParams = params
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.mapFragment) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > TIME_LIMIT) {
                    clickCount = 1
                } else {
                    clickCount++
                }

                lastClickTime = currentTime

                if (clickCount == REQUIRED_CLICKS) {
                    val intent = Intent(this, CookieClicker::class.java)
                    startActivity(intent)
                    true
                }
            }
            false
        }
    }
}
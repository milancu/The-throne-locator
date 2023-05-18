package cz.cvut.fel.thethronelocator.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.auth.UserData
import cz.cvut.fel.thethronelocator.databinding.ActivityBaseBinding


open class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding
    private lateinit var navController: NavController
    private lateinit var signInClient: SignInClient
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var currentUser: UserData
    var clickCount = 0
    var lastClickTime: Long = 0
    val REQUIRED_CLICKS = 7
    val TIME_LIMIT: Long = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signInClient = Identity.getSignInClient(this)
        googleAuthClient = GoogleAuthClient(this, signInClient)

        currentUser = googleAuthClient.getUser()!!

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.findNavController()

        setSupportActionBar(binding.searchBar)

        val bottomNavigationView = binding.bottomNavigationView
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
                    val intent = Intent(this, CookieClicker::class.java)
                    startActivity(intent)
                }
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_bar_menu, menu)

        menu?.findItem(R.id.profileFragment)?.icon = currentUser.profilePicture
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
}
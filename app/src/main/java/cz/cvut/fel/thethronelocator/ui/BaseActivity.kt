package cz.cvut.fel.thethronelocator.ui

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import cz.cvut.fel.thethronelocator.R

open class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_1 -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.item_2 -> {
                    val intent = Intent(this, ToiletList::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    protected fun setContentLayoutOverLappingSearchBar(layoutResId: Int) {
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame_overlap_search_bar)
        layoutInflater.inflate(layoutResId, contentFrame, true)
    }

    protected fun setContentLayout(layoutResId: Int) {
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(layoutResId, contentFrame, true)
    }
}
package cz.cvut.fel.thethronelocator.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.MainBinding
import cz.cvut.fel.thethronelocator.model.ToiletPoint
import cz.cvut.fel.thethronelocator.network.MapoticApi
import cz.cvut.fel.thethronelocator.repository.ToiletPointRepository
import cz.cvut.fel.thethronelocator.viewmodel.MapViewModel
import cz.cvut.fel.thethronelocator.viewmodel.MapViewModelFactory


class MainActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val binding: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    private lateinit var clusterManager: ClusterManager<ToiletPoint>
    private lateinit var viewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentLayout(R.layout.main)
//        setContentView(binding.root)
        setContentLayoutOverLappingSearchBar(R.layout.main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val factory = MapViewModelFactory(ToiletPointRepository(MapoticApi.getInstance()))

        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        viewModel.toiletPoints.observe(this) { toiletPoints ->
            updateMarkers(toiletPoints)
        }
//        viewModel.isLoading.observe(this) { isLoading ->
//            binding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.getToiletPoints()
    }


    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(50.073658, 14.418540),
                10f
            )
        )

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val widthDp = (metrics.widthPixels / metrics.density).toInt()
        val heightDp = (metrics.heightPixels / metrics.density).toInt()

        viewModel.algorithm.updateViewSize(widthDp, heightDp)

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(this@MainActivity, map)
        clusterManager.setAlgorithm(viewModel.algorithm)

        // Set a click listener for the cluster items
        clusterManager.setOnClusterItemClickListener { item ->
            // Create an intent to open the ToiletDetail activity
            val intent = Intent(this@MainActivity, ToiletDetailActivity::class.java)
            intent.putExtra("toiletId", item.id)
            startActivity(intent)
            true
        }

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
    }

    private fun updateMarkers(toiletPoints: List<ToiletPoint>) {
        clusterManager.clearItems()
        clusterManager.addItems(toiletPoints)
        clusterManager.cluster()
    }
}
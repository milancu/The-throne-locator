package cz.cvut.fel.thethronelocator

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import cz.cvut.fel.thethronelocator.databinding.MainBinding
import cz.cvut.fel.thethronelocator.network.MapoticApi
import cz.cvut.fel.thethronelocator.repository.ToiletPointRepository


class MainActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val binding: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    private lateinit var clusterManager: ClusterManager<ToiletPoint>
    private lateinit var viewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentLayout(R.layout.main)
//        setContentView(binding.root)
        setContentLayout(R.layout.main)

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
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val widthDp = (metrics.widthPixels / metrics.density).toInt()
        val heightDp = (metrics.heightPixels / metrics.density).toInt()

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(50.073658, 14.418540),
                10f
            )
        )

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(this@MainActivity, googleMap)
        clusterManager.setAlgorithm(NonHierarchicalViewBasedAlgorithm(widthDp, heightDp))


        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(clusterManager)

        viewModel.getToiletPoints()
    }

    private fun updateMarkers(toiletPoints: List<ToiletPoint>) {
        clusterManager.clearItems()
        clusterManager.addItems(toiletPoints)
        clusterManager.cluster()
    }
}
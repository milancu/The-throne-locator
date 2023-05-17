package cz.cvut.fel.thethronelocator.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.FragmentMapBinding
import cz.cvut.fel.thethronelocator.model.ToiletPoint
import cz.cvut.fel.thethronelocator.network.MapoticApi
import cz.cvut.fel.thethronelocator.repository.ToiletPointRepository
import cz.cvut.fel.thethronelocator.viewmodel.MapViewModel
import cz.cvut.fel.thethronelocator.viewmodel.MapViewModelFactory


class MapFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var map: GoogleMap
    private var _binding: FragmentMapBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var clusterManager: ClusterManager<ToiletPoint>
    private lateinit var viewModel: MapViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        if (!this::map.isInitialized) {
            mapFragment.getMapAsync(this)
        }


        val factory = MapViewModelFactory(ToiletPointRepository(MapoticApi.getInstance()))

        viewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        viewModel.toiletPoints.observe(viewLifecycleOwner) { toiletPoints ->
            updateMarkers(toiletPoints)
        }
//        viewModel.isLoading.observe(this) { isLoading ->
//            binding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
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
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

        val widthDp = (metrics.widthPixels / metrics.density).toInt()
        val heightDp = (metrics.heightPixels / metrics.density).toInt()

        viewModel.algorithm.updateViewSize(widthDp, heightDp)

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(activity, map)
        clusterManager.setAlgorithm(viewModel.algorithm)

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)

        clusterManager.setOnClusterClickListener { cluster ->
            // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
            // inside of bounds, then animate to center of the bounds.

            // Create the builder to collect all essential cluster items for the bounds.
            val builder = LatLngBounds.builder()

            for (item in cluster.items) {
                builder.include(item.position);
            }

            // Get the LatLngBounds
            val bounds = builder.build();

            // Animate camera to the bounds
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (e: Exception) {
                e.printStackTrace();
            }

            true
        }

        clusterManager.setOnClusterItemClickListener {item ->
            val bundle = Bundle().apply {
                putInt("toiletId", item.id)
                putString("name", item.name)
                putFloat("latitude", item.position.latitude.toFloat())
                putFloat("longitude", item.position.longitude.toFloat())
            }

            navController.navigate(R.id.action_to_detail, bundle)
            true
        }
    }

    private fun updateMarkers(toiletPoints: List<ToiletPoint>) {
        clusterManager.clearItems()
        clusterManager.addItems(toiletPoints)
        clusterManager.cluster()
    }
}
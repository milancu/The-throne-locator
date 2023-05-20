package cz.cvut.fel.thethronelocator.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.FragmentMapPickerBinding
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils


private const val DEFAULT_ZOOM = 10f
private const val USER_LOCATION_ZOOM = 15f


class MapPickerFragment : DialogFragment(R.layout.fragment_map_picker), OnMapReadyCallback {
    private lateinit var binding: FragmentMapPickerBinding
    private var selectedLatLng: LatLng? = null
    private lateinit var map: GoogleMap

    private var permissionDenied = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(50.073658, 14.418540)

    companion object {
        const val PICKED_COORDINATES: String = "PICKED_COORDINATES"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    // Enable the my location layer if the permission has been granted.
                    enableMyLocation()
                } else {
                    // Permission was denied. Display an error message
                    // Display the missing permission error dialog when the fragments resume.
                    permissionDenied = true
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentMapPickerBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()


        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_picker) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            googleMap.setOnMapClickListener { latLng ->
                selectedLatLng?.let { googleMap.clear() }

                val markerOptions = MarkerOptions().position(latLng)
                googleMap.addMarker(markerOptions)

                selectedLatLng = latLng
            }
        }
        if (!this::map.isInitialized) {
            mapFragment.getMapAsync(this)
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        val myLocationButton = binding.currentLocationIconButton
        myLocationButton.setOnClickListener {
            if (map.isMyLocationEnabled) {
                getDeviceLocation()
            } else {
                askForLocationPermission()
            }
        }

        val confirmButton = binding.confirmButton
        confirmButton.setOnClickListener {
            if (selectedLatLng != null) {
                val latitude = selectedLatLng!!.latitude
                val longitude = selectedLatLng!!.longitude

                val result = Bundle().apply {
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }

                navController.previousBackStackEntry?.savedStateHandle?.set(
                    PICKED_COORDINATES,
                    result
                )
                navController.popBackStack()
            } else {
                Toast.makeText(context, "Please select a location on the map", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialogTheme
    }

    private fun askForLocationPermission() {
        when {
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                showMissingPermissionError()
            }

            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun showMissingPermissionError() {
        SnackBarUtils.showSnackBarWithAction(
            requireView(),
            "Location access denied. Enable permissions to zoom to your location.",
            "Enable",
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                ), USER_LOCATION_ZOOM
                            )
                        )
                    }
                } else {
                    map.animateCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
                    )
                    map.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun enableMyLocation() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ), ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                map.isMyLocationEnabled = true
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                defaultLocation,
                DEFAULT_ZOOM,
            )
        )

        map.uiSettings.isMyLocationButtonEnabled = false
        enableMyLocation()
    }

}
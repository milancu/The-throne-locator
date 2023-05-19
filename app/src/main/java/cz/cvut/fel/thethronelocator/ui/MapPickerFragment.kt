package cz.cvut.fel.thethronelocator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.FragmentMapPickerBinding


class MapPickerFragment : DialogFragment(R.layout.fragment_map_picker) {
    private lateinit var binding: FragmentMapPickerBinding
    private var selectedLatLng: LatLng? = null

    companion object {
        const val PICKED_COORDINATES: String = "PICKED_COORDINATES"
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

        val confirmButton = binding.confirmButton
        confirmButton.setOnClickListener {
            if (selectedLatLng != null) {
                val latitude = selectedLatLng!!.latitude
                val longitude = selectedLatLng!!.longitude

                val result = Bundle().apply {
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }

                navController.previousBackStackEntry?.savedStateHandle?.set(PICKED_COORDINATES, result)
                navController.popBackStack()
            } else {
                Toast.makeText(context, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.FullScreenDialogTheme
    }
}
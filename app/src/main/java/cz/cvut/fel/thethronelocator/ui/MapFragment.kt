package cz.cvut.fel.thethronelocator.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.maps.android.clustering.ClusterManager
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.FragmentMapBinding
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.model.enum.ToiletType
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.viewmodel.ToiletViewModel
import cz.cvut.fel.thethronelocator.viewmodel.ToiletViewModelFactory


class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var clusterManager: ClusterManager<Toilet>
    private lateinit var viewModel: ToiletViewModel
    private lateinit var navController: NavController

    private lateinit var dialogView: View
    private lateinit var filterDialog: AlertDialog

    //>>>>>>>>>>>>>>>>>>>>>>>>>>TADY HODNOTY PRO FILTRACI<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private var filterByType: MutableList<ToiletType> = listOf(
        ToiletType.IN_A_PARK,
        ToiletType.IN_SHOPPING_MALL,
        ToiletType.STANDALONE
    ).toMutableList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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


        val factory = ToiletViewModelFactory(ToiletRepository())

        viewModel = ViewModelProvider(this, factory)[ToiletViewModel::class.java]
        viewModel.toiletPoints.observe(viewLifecycleOwner) { toiletPoints ->
            updateMarkers(toiletPoints)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.getToiletPoints(filterByType)

        navController.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Bundle>(MapPickerFragment.PICKED_COORDINATES)?.observe(
                viewLifecycleOwner
            ) { result ->
                val latitude = result.getDouble("latitude")
                val longitude = result.getDouble("longitude")

                val latitudeInput =
                    dialogView.findViewById<TextInputEditText>(R.id.input_latitude_text)
                latitudeInput.setText("$latitude")
                val longitudeInput =
                    dialogView.findViewById<TextInputEditText>(R.id.input_longitude_text)
                longitudeInput.setText("$longitude")
            }


        val filterIconButton = binding.filterIconButton
        filterIconButton.setOnClickListener {
            filterDialog.show()
        }

        val addToiletIconButton = binding.addToiletIconButton
        addToiletIconButton.setOnClickListener {
            navController.navigate(R.id.action_to_add_new_toilet)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        filterDialog = createFilterDialog(context)

        filterDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        filterDialog.window?.setGravity(Gravity.CENTER)
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

        val metrics = Resources.getSystem().displayMetrics
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
                builder.include(item.position)
            }

            // Get the LatLngBounds
            val bounds = builder.build()

            // Animate camera to the bounds
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            true
        }

        clusterManager.setOnClusterItemClickListener { item ->
            val bundle = Bundle().apply {
                putString("toiletId", item.id)
                putString("name", item.name)
                putFloat("latitude", item.position.latitude.toFloat())
                putFloat("longitude", item.position.longitude.toFloat())
            }

            navController.navigate(R.id.action_to_detail, bundle)
            true
        }
    }

    private fun updateMarkers(toiletPoints: List<Toilet>) {
        clusterManager.clearItems()
        clusterManager.addItems(toiletPoints)
        clusterManager.cluster()
    }

    private fun createFilterDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)

        val dialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
        val checkBox1 = dialogView.findViewById<CheckBox>(R.id.checkbox_child_1)
        val checkBox2 = dialogView.findViewById<CheckBox>(R.id.checkbox_child_2)
        val checkBox3 = dialogView.findViewById<CheckBox>(R.id.checkbox_child_3)


        checkBox1.isChecked = filterByType.contains(ToiletType.STANDALONE)
        checkBox2.isChecked = filterByType.contains(ToiletType.IN_A_PARK)
        checkBox3.isChecked = filterByType.contains(ToiletType.IN_SHOPPING_MALL)

        checkBox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterByType.add(ToiletType.STANDALONE)
            } else {
                filterByType.remove(ToiletType.STANDALONE)
            }
        }

        checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterByType.add(ToiletType.IN_A_PARK)
            } else {
                filterByType.remove(ToiletType.IN_A_PARK)
            }
        }

        checkBox3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterByType.add(ToiletType.IN_SHOPPING_MALL)
            } else {
                filterByType.remove(ToiletType.IN_SHOPPING_MALL)
            }
        }
        dialog.setTitle("Filter:")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Confirm") { dialog, which ->
                viewModel.getToiletPoints(filterByType)
                dialog.cancel()
            }

        return dialog.create()
    }

//    private fun createAddNewDialog(context: Context): AlertDialog {
//        dialogView = LayoutInflater.from(context).inflate(R.layout.add_new_dialog, null)
//
//        val dialog = MaterialAlertDialogBuilder(context)
//            .setTitle("Add new")
//            .setView(dialogView)
//            .setNegativeButton("Back") { dialog, which ->
//                dialog.cancel()
//            }
//            .setPositiveButton("Add") { dialog, which ->
//                dialog.cancel()
//                //TODO logika
//
//                SnackBarUtils.showSnackBarWithCloseButton(
//                    requireView(),
//                    "New toilet successfully added"
//                )
//            }
//            .create()
//
//        dialog.window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        dialog.window?.setGravity(Gravity.CENTER)
//
////        val button = dialogView.findViewById<MaterialButton>(R.id.button_choose_from_map)
////        button.setOnClickListener {
////            showMapPicker()
////        }
////
////        val openTimeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.time_open_input)
////        openTimeInput.setOnClickListener {
////            showTimePicker(openTimeInput)
////        }
////        val closeTimeInput = dialogView.findViewById<AutoCompleteTextView>(R.id.time_close_input)
////        closeTimeInput.setOnClickListener {
////            showTimePicker(closeTimeInput)
////        }
//
//
//        return dialog
//    }


    //    private fun showTimePicker(timeInput: AutoCompleteTextView) {
//        val timePicker = MaterialTimePicker.Builder()
//            .setTimeFormat(TimeFormat.CLOCK_12H)
//            .build()
//
//        timePicker.addOnPositiveButtonClickListener {
//            val hour = timePicker.hour
//            val minute = timePicker.minute
//            val selectedTime = String.format("%02d:%02d", hour, minute)
//            timeInput.setText(selectedTime)
//        }
//
//        timePicker.show(this.childFragmentManager, "timePicker")
//    }
//
//    private fun showMapPicker() {
//        val intent = Intent(this.requireContext(), MapPickerActivity::class.java)
//        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MAP_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val latitude = data?.getDoubleExtra("latitude", 0.0)
//            val longitude = data?.getDoubleExtra("longitude", 0.0)
//
//            val latitudeInput = dialogView.findViewById<TextInputEditText>(R.id.input_latitude_text)
//            latitudeInput.setText("$latitude")
//            val longitudeInput =
//                dialogView.findViewById<TextInputEditText>(R.id.input_longitude_text)
//            longitudeInput.setText("$longitude")
//        }
//    }
    private fun showMapPicker() {
        navController.navigate(R.id.action_to_map_picker)
    }
}
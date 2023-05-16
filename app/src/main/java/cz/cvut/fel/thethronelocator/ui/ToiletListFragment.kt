package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.ToiletListAdapter
import cz.cvut.fel.thethronelocator.databinding.FragmentMapBinding
import cz.cvut.fel.thethronelocator.databinding.ToiletListBinding
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.repository.ToiletRepository

class ToiletListFragment : Fragment() {
    private var _binding: ToiletListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val toiletRepository = ToiletRepository()
    private lateinit var dialogView: View
    private lateinit var filterDialog: AlertDialog
    private lateinit var sortDialog: AlertDialog
    private lateinit var addNewDialog: AlertDialog

    companion object {
        private const val MAP_PICKER_REQUEST_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ToiletListBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        toiletRepository.loadToilets()

        val toiletList: RecyclerView = binding.toiletList
        toiletList.layoutManager = LinearLayoutManager(activity)
        val toiletListAdapter = ToiletListAdapter()
        toiletList.adapter = toiletListAdapter

        val allToiletsObserver = Observer<List<Toilet>> {
            toiletListAdapter.submitList(it)
        }
        toiletRepository.allToilet.observe(viewLifecycleOwner, allToiletsObserver)

        val floatingFilterActionButton = binding.floatingActionButtonFilter
        floatingFilterActionButton.setOnClickListener {
            filterDialog.show()
        }

        val floatingSortActionButton = binding.floatingActionButtonSort
        floatingSortActionButton.setOnClickListener {
            sortDialog.show()
        }

        val floatingAddNewActionButton = binding.floatingActionButtonAddNew
        floatingAddNewActionButton.setOnClickListener {
            addNewDialog.show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        filterDialog = createFilterDialog(context)
        sortDialog = createSortDialog(context)
        addNewDialog = createAddNewDialog(context)
    }

    private fun createSortDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Sort by:")
            .setView(R.layout.sort_dialog)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Confirm") { dialog, which ->
                dialog.cancel()
                //TODO
            }
            .create()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        return dialog
    }

    private fun createFilterDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Filter:")
            .setView(R.layout.filter_dialog)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Confirm") { dialog, which ->
                dialog.cancel()
                //TODO
            }
            .create()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        return dialog
    }

    private fun createAddNewDialog(context: Context): AlertDialog {
        dialogView = LayoutInflater.from(context).inflate(R.layout.add_new_dialog, null)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Add new")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Add") { dialog, which ->
                dialog.cancel()
                //TODO
            }
            .create()

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        val button = dialogView.findViewById<MaterialButton>(R.id.button_choose_from_map)
        button.setOnClickListener {
            showMapPicker()
        }

        return dialog
    }

    private fun showMapPicker() {
//        val intent = Intent(this, MapPickerActivity::class.java)
//        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            val latitudeInput = dialogView.findViewById<TextInputEditText>(R.id.input_latitude)
            latitudeInput.setText("$latitude")
            val longitudeInput = dialogView.findViewById<TextInputEditText>(R.id.input_longitude)
            longitudeInput.setText("$longitude")
        }
    }

}
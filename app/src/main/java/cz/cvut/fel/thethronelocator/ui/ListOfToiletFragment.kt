package cz.cvut.fel.thethronelocator.ui

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.ToiletListAdapter
import cz.cvut.fel.thethronelocator.databinding.FragmentToiletListBinding
import cz.cvut.fel.thethronelocator.model.enum.SortType
import cz.cvut.fel.thethronelocator.model.enum.ToiletType
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.viewmodel.ToiletViewModel
import cz.cvut.fel.thethronelocator.viewmodel.ToiletViewModelFactory


class ListOfToiletFragment : Fragment(R.layout.fragment_toilet_list) {
    private lateinit var binding: FragmentToiletListBinding
    private lateinit var navController: NavController
    private lateinit var dialogView: View
    private lateinit var filterDialog: AlertDialog

    private lateinit var viewModel: ToiletViewModel


    private var selectedSortBy: SortType = SortType.NEAREST
    private var filterByType: MutableList<ToiletType> =
        listOf(
            ToiletType.IN_A_PARK,
            ToiletType.IN_SHOPPING_MALL,
            ToiletType.STANDALONE
        ).toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentToiletListBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val factory = ToiletViewModelFactory(ToiletRepository())

        viewModel = ViewModelProvider(this, factory)[ToiletViewModel::class.java]
        viewModel.toiletPoints.observe(viewLifecycleOwner) {
            val toiletAdapter = ToiletListAdapter(it)
            binding.toiletList.adapter = toiletAdapter
            binding.toiletList.layoutManager = LinearLayoutManager(requireContext())

        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
        }
        viewModel.getToiletPoints(filterByType, selectedSortBy)


        val floatingFilterActionButton = binding.floatingActionButtonFilter
        floatingFilterActionButton.setOnClickListener {
            filterDialog.show()
        }

        val floatingAddNewActionButton = binding.floatingActionButtonAddNew
        floatingAddNewActionButton.setOnClickListener {
            navController.navigate(R.id.action_to_add_new_toilet)
        }

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
                viewModel.getToiletPoints(filterByType, selectedSortBy)
                dialog.cancel()
            }

        return dialog.create()
    }
}
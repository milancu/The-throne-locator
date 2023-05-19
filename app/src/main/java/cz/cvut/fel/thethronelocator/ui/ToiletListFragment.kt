package cz.cvut.fel.thethronelocator.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
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
import cz.cvut.fel.thethronelocator.databinding.FragmentToiletListBinding
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.model.enum.SortType
import cz.cvut.fel.thethronelocator.model.enum.ToiletType
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils


class ToiletListFragment : Fragment(R.layout.fragment_toilet_list) {
    private lateinit var binding: FragmentToiletListBinding
    private lateinit var navController: NavController
    private val toiletRepository = ToiletRepository()
    private lateinit var dialogView: View
    private lateinit var filterDialog: AlertDialog
    private lateinit var addNewDialog: AlertDialog
    private lateinit var sortDialog: AlertDialog

    private var selectedSortByTmp: SortType = SortType.RATING
    private var filterByTypeTmp: MutableList<ToiletType> =
        listOf(
            ToiletType.IN_A_PARK,
            ToiletType.IN_SHOPPING_MAIL,
            ToiletType.STANDLONE
        ).toMutableList()

    //>>>>>>>>>>>>>>>>>>>>>>>>>>TADY HODNOTY PRO FILTRACI<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private var selectedSortBy: SortType = SortType.RATING
    private var filterByType: List<ToiletType> = emptyList()
    //>>>>>>>>>>>>>>>>>takze neco jako tohle<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    //>>>>>>>>>>>>>>>>>toiletList.filter { filterByType.contains(it.type) }.sortedBy { it.latitude }//.sortedBy { it.rating }<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentToiletListBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
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

        sortDialog = createSortDialog(context)
        filterDialog = createFilterDialog(context)
        addNewDialog = createAddNewDialog(context)

        filterDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        filterDialog.window?.setGravity(Gravity.CENTER)

        sortDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        sortDialog.window?.setGravity(Gravity.CENTER)

        addNewDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        addNewDialog.window?.setGravity(Gravity.CENTER)
    }

    private fun createSortDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)

        val dialogView = layoutInflater.inflate(R.layout.sort_dialog, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)

        val radioButton1 = dialogView.findViewById<RadioButton>(R.id.radio_button_1)
        val radioButton2 = dialogView.findViewById<RadioButton>(R.id.radio_button_2)

        if (selectedSortBy === SortType.NEAREST) {
            radioButton1.isChecked = true
            radioButton2.isChecked = false
        } else {
            radioButton1.isChecked = false
            radioButton2.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedSortByTmp = when (checkedId) {
                R.id.radio_button_1 -> SortType.RATING
                R.id.radio_button_2 -> SortType.NEAREST
                else -> {
                    SortType.RATING
                }
            }
        }

        dialog.setTitle("Sort by:")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Confirm") { dialog, which ->
                selectedSortBy = selectedSortByTmp
                dialog.cancel()
            }

        return dialog.create()
    }

    private fun createFilterDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)

        val dialogView = layoutInflater.inflate(R.layout.filter_dialog, null)
        val checkBox1 = dialogView.findViewById<CheckBox>(R.id.checkbox_child_1)
        val checkBox2 = dialogView.findViewById<CheckBox>(R.id.checkbox_child_2)
        val checkBox3 = dialogView.findViewById<CheckBox>(R.id.checkbox_child_3)


        checkBox1.isChecked = filterByTypeTmp.contains(ToiletType.STANDLONE)
        checkBox2.isChecked = filterByTypeTmp.contains(ToiletType.IN_A_PARK)
        checkBox3.isChecked = filterByTypeTmp.contains(ToiletType.IN_SHOPPING_MAIL)

        checkBox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterByTypeTmp.add(ToiletType.STANDLONE)
            } else {
                filterByTypeTmp.remove(ToiletType.STANDLONE)
            }
        }

        checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterByTypeTmp.add(ToiletType.IN_A_PARK)
            } else {
                filterByTypeTmp.remove(ToiletType.IN_A_PARK)
            }
        }

        checkBox3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                filterByTypeTmp.add(ToiletType.IN_SHOPPING_MAIL)
            } else {
                filterByTypeTmp.remove(ToiletType.IN_SHOPPING_MAIL)
            }
        }
        dialog.setTitle("Filter:")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Confirm") { dialog, which ->
                filterByType = filterByTypeTmp
                dialog.cancel()
            }

        return dialog.create()
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
                //TODO logika

                SnackBarUtils.showSnackBarWithCloseButton(
                    requireView().findViewById<View>(R.id.toiletList),
                    "New toilet successfully added"
                )
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
        navController.navigate(R.id.action_to_map_picker)
    }
}
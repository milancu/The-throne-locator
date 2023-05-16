package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.ToiletListAdapter
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.repository.ToiletRepository

class ToiletList : BaseActivity() {
    private val toiletRepository = ToiletRepository()
    private lateinit var dialogView: View

    companion object {
        private const val MAP_PICKER_REQUEST_CODE = 123
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.toilet_list)
        toiletRepository.loadToilets()

        val toiletList: RecyclerView = findViewById(R.id.toiletList)
        toiletList.layoutManager = LinearLayoutManager(this)
        val toiletListAdapter = ToiletListAdapter()
        toiletList.adapter = toiletListAdapter

        val allToiletsObserver = Observer<List<Toilet>> {
            toiletListAdapter.submitList(it)
        }
        toiletRepository.allToilet.observe(this, allToiletsObserver)


        val floatingFilterActionButton =
            findViewById<FloatingActionButton>(R.id.floating_action_button_filter)
        floatingFilterActionButton.setOnClickListener {
            showFilterDialog()
        }

        val floatingSortActionButton =
            findViewById<FloatingActionButton>(R.id.floating_action_button_sort)
        floatingSortActionButton.setOnClickListener {
            showSortDialog()
        }

        val floatingAddNewActionButton =
            findViewById<ExtendedFloatingActionButton>(R.id.floating_action_button_add_new)
        floatingAddNewActionButton.setOnClickListener {
            showAddNewDialog()
        }
    }

    private fun showSortDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
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

        dialog.show()
    }

    private fun showFilterDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
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

        dialog.show()
    }

    private fun showAddNewDialog() {
        dialogView = LayoutInflater.from(this).inflate(R.layout.add_new_dialog, null)

        val dialog = MaterialAlertDialogBuilder(this)
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

        dialog.show()

        val button = dialogView.findViewById<MaterialButton>(R.id.button_choose_from_map)
        button.setOnClickListener {
            showMapPicker()
        }
    }

    private fun showMapPicker() {
        val intent = Intent(this, MapPickerActivity::class.java)
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE)
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
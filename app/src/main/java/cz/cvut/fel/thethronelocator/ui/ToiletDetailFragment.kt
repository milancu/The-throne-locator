package cz.cvut.fel.thethronelocator.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.RatingAdapter
import cz.cvut.fel.thethronelocator.databinding.FragmentDetailBinding
import cz.cvut.fel.thethronelocator.model.Rating
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.model.User
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils

class ToiletDetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var dialogView: View
    private lateinit var reportDialog: AlertDialog
    private lateinit var reminderDialog: AlertDialog
    private lateinit var ratingDialog: AlertDialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindingData()


        val navigateButton = binding.navigateButton
        navigateButton.setOnClickListener {
            openMapWithLocation(latitude, longitude)
        }

        val reportButton = binding.reportButton
        reportButton.setOnClickListener {
            reportDialog.show()
        }

        val bookmarkButton = binding.bookmarkButton
        bookmarkButton.setOnClickListener {
        }

        val notifyButton = binding.notifyButton
        notifyButton.setOnClickListener {
            reminderDialog.show()
        }

        val addNewButton = binding.addRatingButton
        addNewButton.setOnClickListener {
            ratingDialog.show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        reportDialog = createReportDialog(context)
        reminderDialog = createReminderDialog(context)
        ratingDialog = createRatingDialog(context)

        reportDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        reportDialog.window?.setGravity(Gravity.CENTER)

        reminderDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        reminderDialog.window?.setGravity(Gravity.CENTER)

        ratingDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        ratingDialog.window?.setGravity(Gravity.CENTER)
    }

    private fun bindingData() {
        val toiletId = arguments?.getInt("toiletId")

        //---------Need to bind--------------
        val toiletName: String? = arguments?.getString("name")
        val toiletRating: Float = 0f
        val toiletRatings: List<Rating> = getRatingList()
        latitude = arguments?.getFloat("latitude")!!.toDouble()
        longitude = arguments?.getFloat("longitude")!!.toDouble()
        val openingTime: String? = null
        var distance: String? = null

        binding.textViewDistance.text = "${distance}km"


        //binding for rating list
        val ratingAdapter = RatingAdapter(toiletRatings)
        binding.toiletDetailRatingList.adapter = ratingAdapter
        binding.toiletDetailRatingList.layoutManager = LinearLayoutManager(requireContext())

        //binding for toilet name
        binding.toiletName.text = toiletName

        //binding for distance
        binding.textViewDistance.text = "${distance}km"

        //binding for toiletRating
        binding.overallToiletRating.rating = toiletRating

        //binding for openingTime
        binding.textViewOpeningTime.text = openingTime

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openMapWithLocation(latitude: Double, longitude: Double) {
        val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), "Map application is not installed", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getRatingList(): List<Rating> {
        //dummy data
        //TODO
        return listOf(
            Rating(4.5f, "Great toilet!", User(), Toilet()),
            Rating(4.5f, "Great toilet!", User(), Toilet()),
            Rating(4.5f, "Great toilet!", User(), Toilet()),
            Rating(4.5f, "Great toilet!", User(), Toilet()),
            Rating(4.5f, "Great toilet!", User(), Toilet()),
            Rating(4.5f, "Great toilet!", User(), Toilet()),
        )
    }

    private fun createReportDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
        val dialogView = layoutInflater.inflate(R.layout.report_dialog, null)

        dialog
            .setTitle("Report")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Add") { dialog, which ->
                dialog.cancel()
                //TODO logika

                SnackBarUtils.showSnackBarWithCloseButton(
                    requireView(),
                    "Successfully reported"
                )
            }
            .create()

        return dialog.create()
    }

    private fun createReminderDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
        val dialogView = layoutInflater.inflate(R.layout.reminder_dialog, null)

        dialog
            .setTitle("Reminder")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Add") { dialog, which ->
                dialog.cancel()
                //TODO logika

                SnackBarUtils.showSnackBarWithCloseButton(
                    requireView(),
                    "Successfully add a reminder"
                )
            }
            .create()

        return dialog.create()
    }

    private fun createRatingDialog(context: Context): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
        val dialogView = layoutInflater.inflate(R.layout.rating_dialog, null)

        dialog
            .setTitle("Do You like it?")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Add") { dialog, which ->
                dialog.cancel()
                //TODO logika

                SnackBarUtils.showSnackBarWithCloseButton(
                    requireView(),
                    "Rating successfully added"
                )
            }
            .create()

        return dialog.create()
    }
}
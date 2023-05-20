package cz.cvut.fel.thethronelocator.ui

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.RatingAdapter
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.FragmentDetailBinding
import cz.cvut.fel.thethronelocator.model.Rating
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils
import cz.cvut.fel.thethronelocator.viewmodel.ToiletDetailViewModel
import cz.cvut.fel.thethronelocator.viewmodel.ToiletDetailViewModelFactory

class ToiletDetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var dialogView: View
    private lateinit var reminderDialog: AlertDialog
    private lateinit var ratingDialog: AlertDialog
    private lateinit var viewModel: ToiletDetailViewModel
    private lateinit var toilet: Toilet
    private val toiletRepository = ToiletRepository()
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var signInClient: SignInClient

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInClient = Identity.getSignInClient(requireActivity())
        googleAuthClient = GoogleAuthClient(requireContext(), signInClient)

        val toiletId = arguments?.getString("toiletId")!!
        val factory = ToiletDetailViewModelFactory(ToiletRepository())
        viewModel = ViewModelProvider(this, factory)[ToiletDetailViewModel::class.java]
        viewModel.toilet.observe(viewLifecycleOwner) {
            toilet = it
            bindingData()
        }
        viewModel.getToilet(toiletId)

        val navigateButton = binding.navigateButton
        navigateButton.setOnClickListener {
            openMapWithLocation(latitude, longitude)
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

        reminderDialog = createReminderDialog(context)
        ratingDialog = createRatingDialog(context)

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
        val toiletName: String? = toilet.name
        val toiletRating: Float = toilet.getOverAllRating()
        val toiletRatings: List<Rating>? = toilet.ratingList
        latitude = toilet.latitude!!
        longitude = toilet.longitude!!
        val openingTime: String? = toilet.openingTime
        val img = toilet.img
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
        val overAllRating = toiletRatings?.size ?: 0
        binding.overallToiletRating.rating = toiletRating
        binding.textViewOverallRating.text = "Rating($overAllRating)"

        //binding for openingTime
        binding.textViewOpeningTime.text = openingTime

        val imageView: ImageView = requireView().findViewById(R.id.toilet_image_view)
        Glide.with(this)
            .load(img)
            .apply(RequestOptions().centerCrop())
            .into(imageView)

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
                Log.d(TAG, "createRatingDialog: ${toilet.id}")
                toiletRepository.addToiletRating(
                    toiletId = toilet.id!!,
                    Rating(
                        rating = dialogView.findViewById<RatingBar>(R.id.ratingBar).rating,
                        authorId = googleAuthClient.getUser()?.userId
                    )
                )

                SnackBarUtils.showSnackBarWithCloseButton(
                    requireView(),
                    "Rating successfully added"
                )
            }
            .create()

        return dialog.create()
    }
}
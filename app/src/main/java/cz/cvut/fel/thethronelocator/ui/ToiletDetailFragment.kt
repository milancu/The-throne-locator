package cz.cvut.fel.thethronelocator.ui

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.RatingAdapter
import cz.cvut.fel.thethronelocator.auth.GoogleAuthClient
import cz.cvut.fel.thethronelocator.databinding.FragmentDetailBinding
import cz.cvut.fel.thethronelocator.model.Rating
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.repository.UserRepository
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils
import cz.cvut.fel.thethronelocator.viewmodel.ToiletDetailViewModel
import cz.cvut.fel.thethronelocator.viewmodel.ToiletDetailViewModelFactory


class ToiletDetailFragment : Fragment() {
    private var permissionDenied = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var permissionGranted = false
    private var _binding: FragmentDetailBinding? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var reminderDialog: AlertDialog
    private lateinit var ratingDialog: AlertDialog
    private lateinit var bookMarkButton: CheckBox
    private lateinit var viewModel: ToiletDetailViewModel
    private lateinit var toilet: Toilet
    private val toiletRepository = ToiletRepository()
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var signInClient: SignInClient
    private val userRepository = UserRepository()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val CHANNEL_ID = "reminder"
    val CHANNEL_NAME = "Toiler Reminer"


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


        val notifyButton = binding.notifyButton
        notifyButton.setOnClickListener {
            reminderDialog.show()
        }

        val addNewButton = binding.addRatingButton
        addNewButton.setOnClickListener {
            ratingDialog.show()
        }

        createNotificationChannel()

        bookMarkButton = binding.bookmarkButton
        if (googleAuthClient.getUser()!!.isAnonymous) {
            bookMarkButton.visibility = View.GONE
            bookMarkButton.isClickable = false
        } else {
            userRepository.getUserById(googleAuthClient.getUser()!!.userId) { user ->
                val isToiletIdInFavourites = user?.favouritesList?.contains(toiletId) == true
                bookMarkButton.isChecked = isToiletIdInFavourites

                bookMarkButton.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        user?.id?.let { userId ->
                            userRepository.addFavourites(userId, toiletId)
                        }
                        SnackBarUtils.showSnackBarWithCloseButton(
                            requireView(),
                            "Successfully added to favourites"
                        )
                    } else {
                        user?.id?.let { userId ->
                            userRepository.removeFavourites(userId, toiletId)
                        }
                        SnackBarUtils.showSnackBarWithCloseButton(
                            requireView(),
                            "Successfully removed from favourites"
                        )
                    }
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    enableNotifications()
                } else {
                    permissionDenied = true
                }
            }

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

    override fun onResume() {
        super.onResume()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    private fun enableNotifications() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                permissionGranted = true
            }
        }
    }


    private fun showMissingPermissionError() {
        SnackBarUtils.showSnackBarWithAction(
            requireView(),
            "Notification access denied. Enable permissions to set a reminder.",
            "Enable",
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun bindingData() {
        val toiletName: String? = toilet.name
        val toiletRating: Float = toilet.getOverAllRating()
        val toiletRatings: List<Rating>? = toilet.ratingList
        latitude = toilet.latitude!!
        longitude = toilet.longitude!!
        val openingTime: String? = toilet.openingTime
        val img = toilet.img


        //binding for rating list
        val ratingAdapter = RatingAdapter(toiletRatings)
        binding.toiletDetailRatingList.adapter = ratingAdapter
        binding.toiletDetailRatingList.layoutManager = LinearLayoutManager(requireContext())

        //binding for toilet name
        binding.toiletName.text = toiletName

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
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel.
        val mChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
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
            .setTitle("Set Toilet Reminder")
            .setMessage("Select the number of minutes before you need a reminder for this toilet visit.")
            .setView(dialogView)
            .setNegativeButton("Back") { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Add") { dialog, which ->
                dialog.cancel()
                val slider = dialogView.findViewById<Slider>(R.id.slider)
                val delay = slider.value

                if (permissionGranted) {
                    createNotification(delay)
                } else {
                    askForNotificationPermission()
                }
            }
            .create()

        return dialog.create()
    }


    private fun askForNotificationPermission() {
        when {
            shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                showMissingPermissionError()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotification(delay: Float) {
        val notification: Notification =
            NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle("Toilet Reminder")
                .setContentText("Time to Visit the ${toilet.name}!")
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).build()

        SnackBarUtils.showSnackBarWithCloseButton(
            requireView(),
            "Successfully add a reminder"
        )

        requireView().postDelayed({
            with(NotificationManagerCompat.from(requireContext())) {
                // notificationId is a unique int for each notification that you must define

                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForNotificationPermission()
                }
                notify(1, notification)
            }
        }, (delay * 1000 * 60).toLong())
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
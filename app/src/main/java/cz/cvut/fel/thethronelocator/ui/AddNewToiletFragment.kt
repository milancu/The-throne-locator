package cz.cvut.fel.thethronelocator.ui

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.toLowerCase
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.FragmentAddNewToiletBinding
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.model.enum.ToiletType
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.UUID
import kotlin.math.log

class AddNewToiletFragment : Fragment(R.layout.fragment_add_new_toilet) {
    private lateinit var binding: FragmentAddNewToiletBinding
    private lateinit var navController: NavController
    private lateinit var latitudeInput: TextInputEditText
    private lateinit var longitudeInput: TextInputEditText
    private lateinit var openTimeInput: AutoCompleteTextView
    private lateinit var importImageview: ImageView
    private var openingTime: LocalTime? = null
    private var closingTime: LocalTime? = null
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private lateinit var vFilename: String
    private lateinit var storageDir: File
    private lateinit var btnImportPhoto: MaterialButton
    private lateinit var saveButton: MaterialButton

    private lateinit var closeTimeInput: AutoCompleteTextView
    private lateinit var toiletName: TextInputEditText
    private lateinit var toiletType: AutoCompleteTextView
    private val toiletRepository = ToiletRepository()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentAddNewToiletBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        navController = findNavController()

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(ProfileFragment.LOGOUT_SUCCESSFUL)
            .observe(currentBackStackEntry) { success ->
                if (!success) {
                    val startDestination = navController.graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            }
        return fragmentBinding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        openTimeInput = binding.timeOpenInput
        closeTimeInput = binding.timeCloseInput
        toiletName = binding.inputToiletName
        toiletType = binding.toiletType
        latitudeInput = binding.inputLatitudeText
        longitudeInput = binding.inputLongitudeText
        val openMapButton = binding.buttonGetLocationFromMap
        val backButton = binding.backButton
        saveButton = binding.buttonSaveNewToilet
        btnImportPhoto = binding.buttonAddPhoto
        importImageview = binding.importImageview

        openMapButton.setOnClickListener {
            navController.navigate(R.id.action_to_map_picker)
        }
        openTimeInput.setOnClickListener {
            showTimePicker(openTimeInput, closeTimeInput)
        }
        closeTimeInput.setOnClickListener {
            showTimePicker(closeTimeInput, openTimeInput)
        }
        btnImportPhoto.setOnClickListener {
            openCamera()
        }
        importImageview.setOnClickListener {
            openCamera()
        }
        saveButton.setOnClickListener {
            addToilet()
        }
        backButton.setOnClickListener {
            navController.popBackStack()
        }

        navController.currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Bundle>(MapPickerFragment.PICKED_COORDINATES)?.observe(
                viewLifecycleOwner
            ) { result ->
                val latitude = result.getDouble("latitude")
                val longitude = result.getDouble("longitude")
                latitudeInput.setText("$latitude")
                longitudeInput.setText("$longitude")
            }

    }

    private fun addToilet() {
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference
        val fileName = "photo_${UUID.randomUUID()}.jpg"
        val photoRef = storageRef.child("photos/$fileName")
        val loadingBar = binding.loadingAddNewToilet
        loadingBar.visibility = View.VISIBLE
        saveButton.isEnabled = false
        val file = File(storageDir, vFilename);
        val uri = FileProvider.getUriForFile(
            this.requireContext(),
            this.requireContext().applicationContext.packageName + ".provider",
            file
        );

        val uploadTask = photoRef.putFile(uri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            val downloadUrlTask = taskSnapshot.storage.downloadUrl
            downloadUrlTask.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                val toiletType = ToiletType.getTypeByName(toiletType.text.toString())
                val toilet = Toilet(
                    img = downloadUrl,
                    latitude = latitudeInput.text.toString().trim().toDouble(),
                    longitude = longitudeInput.text.toString().trim().toDouble(),
                    name = toiletName.text.toString(),
                    type = toiletType,
                    openingTime = "${openTimeInput.text} - ${closeTimeInput.text}"
                )
                toiletRepository.saveToilet(toilet)
                navController.popBackStack()
                SnackBarUtils.showSnackBarWithCloseButton(
                    requireView(),
                    "New toilet successfully added"
                )
                Log.d(TAG, "addToilet: $downloadUrl")
                loadingBar.visibility = View.GONE
            }.addOnFailureListener { exception ->
                Log.d(TAG, "addToilet: ${exception.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to retrieve download URL",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "addToilet: ${exception.message}")
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        vFilename = "FOTO_$timeStamp.jpg"
        val file = File(storageDir, vFilename);
        val imageUrl = FileProvider.getUriForFile(
            this.requireContext(),
            this.requireContext().packageName + ".provider",
            file
        );
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openCamera()
                } else {
                    Toast.makeText(this.requireContext(), "Permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker(
        timeInput: AutoCompleteTextView,
        otherTimeInput: AutoCompleteTextView
    ) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val selectedTime = String.format("%02d:%02d", hour, minute)
            timeInput.setText(selectedTime)

            if (timeInput == openTimeInput) {
                openingTime = LocalTime.of(hour, minute)
                val minTime = openingTime?.plusHours(1)
                val minHour = minTime?.hour ?: 0
                val minMinute = minTime?.minute ?: 0
                val minSelectedTime = String.format("%02d:%02d", minHour, minMinute)
                otherTimeInput.setText(minSelectedTime)
            } else {
                val newClosingTime = LocalTime.of(hour, minute)
                if (openingTime != null && newClosingTime.isBefore(openingTime)) {
                    Toast.makeText(
                        this.requireContext(),
                        "Closing time must be later than opening time",
                        Toast.LENGTH_SHORT
                    ).show()
                    timeInput.setText("")
                    return@addOnPositiveButtonClickListener
                }
            }
        }

        timePicker.show(this.childFragmentManager, "timePicker")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            val file = File(storageDir, vFilename);
            //File object of camera image
            val uri = FileProvider.getUriForFile(
                this.requireContext(),
                this.requireContext().applicationContext.packageName + ".provider",
                file
            );
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions().centerCrop())
                .into(importImageview)
        }
    }
}
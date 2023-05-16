package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import cz.cvut.fel.thethronelocator.R

class MapPickerActivity : FragmentActivity() {
    private var selectedLatLng: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_picker) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            googleMap.setOnMapClickListener { latLng ->
                selectedLatLng?.let { googleMap.clear() }

                val markerOptions = MarkerOptions().position(latLng)
                googleMap.addMarker(markerOptions)

                selectedLatLng = latLng
            }
        }

        val confirmButton = findViewById<MaterialButton>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            if (selectedLatLng != null) {
                val latitude = selectedLatLng!!.latitude
                val longitude = selectedLatLng!!.longitude

                val resultIntent = Intent().apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
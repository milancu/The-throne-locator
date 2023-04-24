package cz.cvut.fel.thethronelocator

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.data.Point
import com.google.maps.android.data.geojson.GeoJsonLayer
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity: FragmentActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var geoJsonLayer: GeoJsonLayer
    private lateinit var clusterManager: ClusterManager<ToiletPoint>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        getItems();

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getItems() {
        // Create a Retrofit instance with the desired base URL
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.mapotic.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create a service interface for the API
        val apiService = retrofit.create(MapoticApiService::class.java)

        // Make the API request to get the GeoJSON data
        apiService.getGeoJsonData().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body().toString())
                    geoJsonLayer = GeoJsonLayer(map, jsonObject)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Unauthorized: You are not authorized to access this resource."
                        404 -> "Not Found: The requested resource was not found."
                        500 -> "Internal Server Error: The server encountered an unexpected condition."
                        else -> "Error: Something went wrong. Please try again later."
                    }
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        val widthDp = (metrics.widthPixels / metrics.density).toInt()
        val heightDp = (metrics.heightPixels / metrics.density).toInt()

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(50.073658, 14.418540),
                10f
            )
        )

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(this@MainActivity, googleMap)
        clusterManager.setAlgorithm(NonHierarchicalViewBasedAlgorithm(widthDp, heightDp))


        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(clusterManager)

        addItems()
    }

    private fun addItems() {
        // Create a Retrofit instance with the desired base URL
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.mapotic.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create a service interface for the API
        val apiService = retrofit.create(MapoticApiService::class.java)

        // Make the API request to get the GeoJSON data
        apiService.getGeoJsonData().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body().toString())
                    geoJsonLayer = GeoJsonLayer(map, jsonObject)

                    // Loop through the GeoJsonLayer's features and create a new ToiletPoint for each one
                    for (feature in geoJsonLayer.features) {
                        val geometry = feature.geometry
                        if (geometry is Point) {
                            val toiletPoint = ToiletPoint(feature)
                            clusterManager.addItem(toiletPoint)
                        }
                    }

                    val progressBar: ProgressBar = findViewById<ProgressBar>(R.id.progress)
                    progressBar.visibility = View.GONE
                    clusterManager.cluster()
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Unauthorized: You are not authorized to access this resource."
                        404 -> "Not Found: The requested resource was not found."
                        500 -> "Internal Server Error: The server encountered an unexpected condition."
                        else -> "Error: Something went wrong. Please try again later."
                    }
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
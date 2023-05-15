package cz.cvut.fel.thethronelocator.repository

import com.google.gson.JsonObject
import com.google.maps.android.data.Point
import com.google.maps.android.data.geojson.GeoJsonParser
import cz.cvut.fel.thethronelocator.ToiletPoint
import cz.cvut.fel.thethronelocator.network.MapoticApi
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToiletPointRepository constructor(private val mapoticApi: MapoticApi) {
    fun getToiletPoints(onSuccess: (List<ToiletPoint>) -> Unit, onError: (String) -> Unit) {
        val response = mapoticApi.getGeoJsonData()
        response.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body().toString())
                    val geoJson = GeoJsonParser(jsonObject)

                    val toiletPoints = mutableListOf<ToiletPoint>()
                    geoJson.features.forEach{ feature ->
                        val geometry = feature.geometry
                        if (geometry is Point) {
                            toiletPoints.add(ToiletPoint(feature))
                        }
                    }
                    onSuccess(toiletPoints)
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Unauthorized: You are not authorized to access this resource."
                        404 -> "Not Found: The requested resource was not found."
                        500 -> "Internal Server Error: The server encountered an unexpected condition."
                        else -> "Error: Something went wrong. Please try again later."
                    }
                    onError(errorMessage)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
               onError("Error: ${t.message}")
            }
        })
    }
}
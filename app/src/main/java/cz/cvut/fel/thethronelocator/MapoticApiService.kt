package cz.cvut.fel.thethronelocator

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET

interface MapoticApiService {
    @GET("api/v1/maps/10/pois.geojson/")
    fun getGeoJsonData(): Call<JsonObject>
}
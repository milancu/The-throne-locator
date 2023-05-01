package cz.cvut.fel.thethronelocator.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://www.mapotic.com/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface MapoticApi {
    @GET("api/v1/maps/10/pois.geojson/")
    fun getGeoJsonData(): Call<JsonObject>

    companion object RetrofitService {

        @Volatile
        private var INSTANCE: MapoticApi? = null

        fun getInstance(): MapoticApi {
            return INSTANCE ?: synchronized(this) {
                val instance = retrofit.create(MapoticApi::class.java)
                INSTANCE = instance
                instance
            }
        }
    }
}
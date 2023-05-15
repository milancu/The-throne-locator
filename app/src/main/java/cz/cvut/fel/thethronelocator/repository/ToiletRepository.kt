package cz.cvut.fel.thethronelocator.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.cvut.fel.thethronelocator.model.Toilet
import java.time.LocalDateTime

class ToiletRepository {

    private val _allToilets = MutableLiveData<List<Toilet>>()
    val allToilet: LiveData<List<Toilet>> = _allToilets

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadToilets() {
        val toilets = ArrayList<Toilet>()


        for (i in 0..10) {
            toilets.add(
                Toilet(
                    name = "Toilet $i",
                    type = "Public",
                    address = "Prague 4",
                    latitude = 50.5,
                    longitude = 40.4,
                    features = listOfNotNull(),
                    openingTime = LocalDateTime.now(),
                    closingTime = LocalDateTime.now()
                )
            )
        }

        _allToilets.value = toilets
    }
}
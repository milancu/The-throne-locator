package cz.cvut.fel.thethronelocator.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cz.cvut.fel.thethronelocator.model.Rating
import cz.cvut.fel.thethronelocator.model.Toilet

class RatingRepository {

    private val _allRatings = MutableLiveData<List<Rating>>()
    val allRating: LiveData<List<Rating>> = _allRatings
}


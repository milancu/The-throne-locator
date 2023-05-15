package cz.cvut.fel.thethronelocator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.cvut.fel.thethronelocator.repository.ToiletPointRepository

class MapViewModelFactory constructor(private val repository: ToiletPointRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            MapViewModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
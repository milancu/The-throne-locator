package cz.cvut.fel.thethronelocator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.cvut.fel.thethronelocator.repository.ToiletPointRepository


class MapViewModel(private val repository: ToiletPointRepository) : ViewModel() {
    private val _toiletPoints = MutableLiveData<List<ToiletPoint>>()
    val toiletPoints: LiveData<List<ToiletPoint>> = _toiletPoints

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getToiletPoints() {
        _isLoading.postValue(true)
        repository.getToiletPoints(
            onSuccess = { toiletPoints ->
                _toiletPoints.postValue(toiletPoints)
                _isLoading.postValue(false)
            },
            onError = { message ->
                _errorMessage.postValue(message)
                _isLoading.postValue(false)
            }
        )
    }
}
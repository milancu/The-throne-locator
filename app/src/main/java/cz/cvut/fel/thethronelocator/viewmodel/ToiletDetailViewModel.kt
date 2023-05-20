package cz.cvut.fel.thethronelocator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.repository.ToiletRepository

class ToiletDetailViewModel(private val repository: ToiletRepository) : ViewModel() {
    private var _toilet = MutableLiveData<Toilet>()
    val toilet: LiveData<Toilet> = _toilet

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getToilet(id: String) {
        _isLoading.postValue(true)
        repository.getToiletById(
            id = id,
            onSuccess = {
                _toilet.postValue(it)
                _isLoading.postValue(false)
            },
            onError = { message ->
                _errorMessage.postValue(message)
                _isLoading.postValue(false)
            }
        )
    }

}
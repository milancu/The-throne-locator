package cz.cvut.fel.thethronelocator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.cvut.fel.thethronelocator.repository.ToiletRepository

class ToiletViewModelFactory constructor(private val repository: ToiletRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ToiletViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ToiletViewModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
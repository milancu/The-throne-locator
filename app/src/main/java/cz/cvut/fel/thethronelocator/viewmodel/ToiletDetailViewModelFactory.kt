package cz.cvut.fel.thethronelocator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cz.cvut.fel.thethronelocator.repository.ToiletRepository

class ToiletDetailViewModelFactory (private val repository: ToiletRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ToiletDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ToiletDetailViewModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
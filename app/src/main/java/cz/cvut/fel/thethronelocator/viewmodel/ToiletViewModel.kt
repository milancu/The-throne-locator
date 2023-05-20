package cz.cvut.fel.thethronelocator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.model.enum.SortType
import cz.cvut.fel.thethronelocator.model.enum.ToiletType
import cz.cvut.fel.thethronelocator.repository.ToiletRepository


class ToiletViewModel(private val repository: ToiletRepository) : ViewModel() {
    private val _toilets = MutableLiveData<List<Toilet>>()
    val toiletPoints: LiveData<List<Toilet>> = _toilets

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val algorithm: NonHierarchicalViewBasedAlgorithm<Toilet> =
        NonHierarchicalViewBasedAlgorithm<Toilet>(0, 0)

    fun getToiletPoints(filterByType: List<ToiletType>, sortType: SortType? = null) {
        _isLoading.postValue(true)
        repository.getToilets(
            onSuccess = {
                if (sortType === SortType.RATING) {
                    _toilets.postValue(it.filter { filterByType.contains(it.type) }
                        .sortedByDescending { it.getOverAllRating() })
                } else {
                    _toilets.postValue(it.filter { filterByType.contains(it.type) }) //TODO sort by nearest
                }
                _isLoading.postValue(false)
            },
            onError = { message ->
                _errorMessage.postValue(message)
                _isLoading.postValue(false)
            }
        )
    }

    fun getToiletByName(name: String) {
        _isLoading.postValue(true)
        repository.getToilets(
            onSuccess = {

                val filteredItems = it.filter { toilet ->
                    toilet.name?.lowercase()?.contains(name.lowercase()) ?: false
                }.sortedByDescending { item ->
                    item.name?.countMatches(name.lowercase()) ?: 0
                }
                _toilets.postValue(filteredItems)
                _isLoading.postValue(false)
            },
            onError = { message ->
                _errorMessage.postValue(message)
                _isLoading.postValue(false)
            }
        )
    }

    private fun String.countMatches(query: String): Int {
        return this.count { it.equals(query) }
    }
}
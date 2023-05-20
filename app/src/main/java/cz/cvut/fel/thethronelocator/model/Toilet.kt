package cz.cvut.fel.thethronelocator.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import cz.cvut.fel.thethronelocator.model.enum.ToiletType

class Toilet(
    var id: String? = null,
    val img: String? = null,
    val name: String? = null,
    val type: ToiletType? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val openingTime: String? = null,
    var ratingList: List<Rating>? = null
) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(latitude!!, longitude!!)
    }

    override fun getTitle(): String? {
        return name
    }

    override fun getSnippet(): String {
        return ""
    }

    fun getOverAllRating(): Float {
        ratingList?.let {
            val ratingSum = it.sumOf { rating -> rating.rating?.toInt() ?: 0 }
            return if (it.isNotEmpty()) ratingSum.toFloat() / it.size else 0f
        } ?: return 0f
    }
}
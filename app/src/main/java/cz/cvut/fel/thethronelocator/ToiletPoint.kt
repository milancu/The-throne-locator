package cz.cvut.fel.thethronelocator

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.data.geojson.GeoJsonFeature

class ToiletPoint(private val feature: GeoJsonFeature) : ClusterItem {
    private val position: LatLng = feature.geometry.geometryObject as LatLng
    val id: Int = feature.getProperty("id").toInt()
    val lastUpdate: String? = feature.getProperty("last_update")
    val name: String? = feature.getProperty("name")
    val slug: String? = feature.getProperty("slug")
    val categoryId: Int? = feature.getProperty("category").toInt()
    val isPublished: Boolean? = feature.getProperty("is_published").toBoolean();
    val rating: Double? = feature.getProperty("rating")?.toDouble();

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return name;
    }

    override fun getSnippet(): String? {
        return rating?.let { rating ->
            "Rating: ${"⭐".repeat(rating.toInt())}"
        }
    }
}

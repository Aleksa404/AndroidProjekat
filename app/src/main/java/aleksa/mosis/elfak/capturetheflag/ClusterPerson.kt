package aleksa.mosis.elfak.capturetheflag

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterPerson(
    lat: Double,
    lng: Double,
    title: String,
    snippet: String
) : ClusterItem {


    private val position: LatLng
    private val title: String
    private val snippet: String

    private var iconPicture: Int = 0
    private val user : User
        get() {
           return user
        }


    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return snippet
    }

    init {
        position = LatLng(lat, lng)
        this.title = title
        this.snippet = snippet
    }
}
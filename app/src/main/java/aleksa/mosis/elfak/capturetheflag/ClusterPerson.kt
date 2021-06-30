
import aleksa.mosis.elfak.capturetheflag.data.User
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem




class ClusterPerson(
    position: LatLng,
    title: String,
    snippet: String,
    iconPicture: Int,
    user: User
) : ClusterItem {


    private var position: LatLng
    private var title: String
    private var snippet: String
    var iconPicture: Int
    private var user: User

    fun getUser(): User {
        return user
    }

    fun setUser(user: User) {
        this.user = user
    }

    fun setPosition(position: LatLng) {
        this.position = position
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setSnippet(snippet: String) {
        this.snippet = snippet
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
        this.position = position
        this.title = title
        this.snippet = snippet
        this.iconPicture = iconPicture
        this.user = user
    }
}


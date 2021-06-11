import android.R
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator




class MyClusterManagerRenderer(
    context: Context, googleMap: GoogleMap?,
    clusterManager: ClusterManager<ClusterPerson?>?
) :
    DefaultClusterRenderer<ClusterPerson>(context, googleMap, clusterManager) {
    private val iconGenerator: IconGenerator
    private val imageView: ImageView
    private val markerWidth: Int
    private val markerHeight: Int

    /**
     * Rendering of the individual ClusterItems
     * @param item
     * @param markerOptions
     */
    override fun onBeforeClusterItemRendered(item: ClusterPerson, markerOptions: MarkerOptions) {
        imageView.setImageResource(item.iconPicture)
        val icon = iconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title)
    }

//    protected override fun shouldRenderAsCluster(cluster: Cluster<ClusterPerson>?): Boolean {
//        return false
//    }

    init {

        // initialize cluster item icon generator
        iconGenerator = IconGenerator(context.applicationContext)
        imageView = ImageView(context.applicationContext)
        markerWidth = context.resources.getDimension(R.dimen.app_icon_size).toInt()
        markerHeight = context.resources.getDimension(R.dimen.app_icon_size).toInt()
        imageView.setLayoutParams(ViewGroup.LayoutParams(markerWidth, markerHeight))
        val padding = context.resources.getDimension(R.dimen.app_icon_size).toInt()
        imageView.setPadding(padding, padding, padding, padding)
        iconGenerator.setContentView(imageView)
    }
}
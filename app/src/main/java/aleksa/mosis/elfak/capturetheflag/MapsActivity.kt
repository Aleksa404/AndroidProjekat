package aleksa.mosis.elfak.capturetheflag

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback  {



//    private lateinit var currentLocation: Location
    private lateinit var lastLocation : Location
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private lateinit var clusterManager: ClusterManager<ClusterPerson>
    private lateinit var mMap: GoogleMap

    private fun setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.503186, -0.126446), 10f))

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(this, mMap)

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        // Add cluster items (markers) to the cluster manager.
        addItems()
    }

    private fun loadMarkers(
        manager: ClusterManager<ClusterPerson>, map: GoogleMap, center: LatLng, count: Int,
        minDistance: Double, maxDistance: Double
    ) {
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLon = Double.MAX_VALUE
        var maxLon = Double.MIN_VALUE
        for (i in 0 until count) {
            val distance = minDistance + Math.random() * maxDistance
            val heading = Math.random() * 360 - 180
            val position = SphericalUtil.computeOffset(center, distance, heading)
            val marker = ClusterMarker(MarkerOptions().position(position).title("Item No. $i"))
            manager.addItem(marker)
            minLat = Math.min(minLat, position.latitude)
            minLon = Math.min(minLon, position.longitude)
            maxLat = Math.max(maxLat, position.latitude)
            maxLon = Math.max(maxLon, position.longitude)
        }
        val min = LatLng(minLat, minLon)
        val max = LatLng(maxLat, maxLon)
        val bounds = LatLngBounds(min, max)
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }
    private fun addItems() {

        // Set some lat/lng coordinates to start with.
        var lat = 51.5145160
        var lng = -0.1270060

        // Add ten cluster items in close proximity, for purposes of this example.
        for (i in 0..9) {
            val offset = i / 60.0
            lat += offset
            lng += offset
            val offsetItem =
                ClusterPerson(lat, lng, "Title $i", "Snippet $i")
            clusterManager.addItem(offsetItem)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


    }

    override fun onStart() {
        super.onStart()
        getLastLocation()
    }
    private fun getLastLocation() {
        fusedLocationProviderClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                //what to do on map
                Toast.makeText(this,lastLocation.longitude.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }
//    private fun fetchLocation() {
//
//        val task = fusedLocationProviderClient.lastLocation
//        task.addOnSuccessListener { location ->
//            if (location != null) {
//                currentLocation = location
//                Toast.makeText(
//                    applicationContext, currentLocation.latitude.toString() + "" +
//                            currentLocation.longitude, Toast.LENGTH_SHORT
//                ).show()
//
//            }
//        }
//    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setUpClusterer()
        enableMyLocation()
//
//
//
//        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
//        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
//        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
//        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
//        googleMap?.addMarker(markerOptions)
    }

    //Permissions
    private val REQUEST_LOCATION_PERMISSION = 1

    private fun enableMyLocation() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
             {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                return
            }
        else {
                mMap.isMyLocationEnabled = true
        }
    }

}
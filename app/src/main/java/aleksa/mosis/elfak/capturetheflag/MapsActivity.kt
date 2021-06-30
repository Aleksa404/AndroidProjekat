package aleksa.mosis.elfak.capturetheflag

import  aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.data.UserLocation
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.utsman.smartmarker.moveMarkerSmoothly


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser = auth.currentUser as FirebaseUser


    private var userBitmap: Bitmap? = null
    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef: StorageReference? = null


    private val UPDATE_FREQUENCY: Long = 30000

    private lateinit var locationRequest: LocationRequest

    private var latLng: LatLng? = null


    private lateinit var locationCallback: LocationCallback
    private var myLocationMarker: Marker? = null


    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var friendList: ArrayList<User> = ArrayList()
    private var friendLocations: HashMap<String, UserLocation> = HashMap<String, UserLocation>()


    private lateinit var mMap: GoogleMap


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_FREQUENCY
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        // updateGPS()

        setUpLocationCallback()
        startLocationUpdates()

        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
            .addOnSuccessListener { ds ->
                var me = ds.toObject(User::class.java)
                var friends: List<User> = me?.friends as List<User>
                friends.forEach { user ->

                    friendLocations[user.id] = UserLocation(user.id, 0.0, 0.0, null)
                    friendLocations[user.id]?.username = user.username


                }
                setupMap()

                        //get profile pic
//                    spaceRef = storageRef.child("images/" + user.uid.toString())
//
//                    val ONE_MEGABYTE: Long = 5000 * 5000
//                    spaceRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
//                        // Data for "images/island.jpg" is returned, use this as needed
//                        userBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//
//
//                    }
                }

    }
    private fun setUpLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return


                FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
                    .setValue(
                        GeoPoint(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude
                        )
                    )
               // updateMyLocationMarker(locationResult)

            }
        }
    }
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
                .setValue(GeoPoint(location.latitude, location.longitude))
//            setupMap()
        }
    }

    override fun onStart() {
        super.onStart()

    }
    @SuppressLint("MissingPermission")
    private fun setupMap(){

        friendLocations.forEach {
            FirebaseDatabase.getInstance().reference.child("users").child(it.key)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null) {
                            return
                        }
                        val location: HashMap<String, Double> = snapshot.value as HashMap<String, Double>
                        val friend = friendLocations[it.key]

                        if(friend?.marker == null){


                            var loc = LatLng(location["latitude"]!!,location["longitude"]!!)
                            //var icon = BitmapDescriptorFactory.fromBitmap(userBitmap)

                            var markerOptions = MarkerOptions().position(loc)
                                .title(friend?.username)
                                .snippet("Thinking of finding some thing...").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                                .icon(icon);
                            val marker  = mMap.addMarker(markerOptions)
                            friend?.latitude = location["latitude"]!!
                            friend?.longitude = location["longitude"]!!
                            friend?.marker = marker
                        }
                        friend?.marker!!.moveMarkerSmoothly( LatLng(location["latitude"]!!, location["longitude"]!!),false)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
    private fun getFriends(){
        FirebaseFirestore.getInstance().collection("users").document(user.uid).get().addOnSuccessListener {
            var friendIdList = it.get("friends") as ArrayList<String>
            friendList.forEach{ fr ->
                Log.d(TAG, fr.username)
            }
        }
    }
    private fun moveCamera(location: LatLng,zoom: Float){
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(location))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()

    }

    //Permissions
    private val REQUEST_LOCATION_PERMISSION = 1

    private fun enableMyLocation() {
            if (checkPermission()){
                mMap.isMyLocationEnabled = true
            }
            else return

    }

    override fun onResume() {
        super.onResume()

    }


    private fun checkPermission(): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun startLocationUpdates() {
        if (checkPermission()) {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
            // updateGPS()


        } else
            return

    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }





    private fun updateMyLocationMarker(locationResult: LocationResult){

        var loc =
            LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
        if(myLocationMarker == null){
            // var icon = BitmapDescriptorFactory.fromBitmap(userBitmap)



            var markerOptions = MarkerOptions().position(loc)
                .title("Current Location")
                .snippet("Thinking of finding some thing...")
            // .icon(icon);
            myLocationMarker = mMap.addMarker(markerOptions)


        }
        else{
            myLocationMarker?.moveMarkerSmoothly(loc, false)
        }
    }
    private fun updateGPS() {
        if (checkPermission()) {
            fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location ->
                FirebaseFirestore.getInstance().collection("users").document(user.uid).update(
                    "latitude", location.latitude,
                    "longitude", location.longitude
                )
            }
        } else
            return


    }

    private fun updateUI(locationResult: LocationResult) {
        Log.d(TAG, locationResult.lastLocation.longitude.toString())
        var loc =
            LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)


        //val btmDrawable = BitmapDrawable(resources, userBitmap)
        if(userBitmap!= null){
            // mMap.clear()
            if(myLocationMarker == null){
                var icon = BitmapDescriptorFactory.fromBitmap(userBitmap)

                var markerOptions = MarkerOptions().position(loc)
                    .title("Current Location")
                    .snippet("Thinking of finding some thing...")
                // .icon(icon);
                myLocationMarker = mMap.addMarker(markerOptions)


            }
            else{
                myLocationMarker?.moveMarkerSmoothly(loc, false)
            }



        }
        moveCamera(loc, 15f)


    }

}

//    private fun setUpClusterer() {
//        // Position the map.
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.503186, -0.126446), 10f))
//
//        // Initialize the manager with the context and the map.
//        // (Activity extends context, so we can pass 'this' in the constructor.)
//        clusterManager = ClusterManager(this, mMap)
//
//        // Point the map's listeners at the listeners implemented by the cluster
//        // manager.
//        mMap.setOnCameraIdleListener(clusterManager)
//        mMap.setOnMarkerClickListener(clusterManager)
//
//        // Add cluster items (markers) to the cluster manager.
//        addItems()
//    }

//    private fun loadMarkers(
//        manager: ClusterManager<ClusterPerson>, map: GoogleMap, center: LatLng, count: Int,
//        minDistance: Double, maxDistance: Double
//    ) {
//        var minLat = Double.MAX_VALUE
//        var maxLat = Double.MIN_VALUE
//        var minLon = Double.MAX_VALUE
//        var maxLon = Double.MIN_VALUE
//        for (i in 0 until count) {
//            val distance = minDistance + Math.random() * maxDistance
//            val heading = Math.random() * 360 - 180
//            val position = SphericalUtil.computeOffset(center, distance, heading)
////            val marker = ClusterMarker(MarkerOptions().position(position).title("Item No. $i"))
////            manager.addItem(marker)
//            minLat = Math.min(minLat, position.latitude)
//            minLon = Math.min(minLon, position.longitude)
//            maxLat = Math.max(maxLat, position.latitude)
//            maxLon = Math.max(maxLon, position.longitude)
//        }
//        val min = LatLng(minLat, minLon)
//        val max = LatLng(maxLat, maxLon)
//        val bounds = LatLngBounds(min, max)
//        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
//    }
//    private fun addItems() {
//
//        // Set some lat/lng coordinates to start with.
//        var lat = 51.5145160
//        var lng = -0.1270060
//
//        // Add ten cluster items in close proximity, for purposes of this example.
//        for (i in 0..9) {
//            val offset = i / 60.0
//            lat += offset
//            lng += offset
//            var latLng =LatLng(lat,lng)
////            val offsetItem =
////                ClusterPerson(latLng, "Title $i", "Snippet $i",)
////            clusterManager.addItem(offsetItem)
//        }
//    }
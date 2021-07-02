package aleksa.mosis.elfak.capturetheflag

import  aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.data.UserLocation
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.SphericalUtil
import com.utsman.smartmarker.moveMarkerSmoothly


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser = auth.currentUser as FirebaseUser

    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef: StorageReference? = null

    private val UPDATE_FREQUENCY: Long = 10000

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var friendLocations: HashMap<String, UserLocation> = HashMap<String, UserLocation>()

    private lateinit var mMap: GoogleMap

    private var myLocationMarker: Marker? = null


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
                }
    }

    private fun setUpLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return


                val circle: Circle = mMap.addCircle(CircleOptions()
                        .center(LatLng(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude))
                        .radius(10.0)
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE))
                FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
                        .setValue(
                                GeoPoint(
                                        locationResult.lastLocation.latitude,
                                        locationResult.lastLocation.longitude
                                )
                        )
                // updateMyLocationMarker(locationResult)
                SphericalUtil.computeDistanceBetween(latLngFrom, latLngTo)

            }
        }
    }


    override fun onStart() {
        super.onStart()

    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {

        friendLocations.forEach {
            FirebaseDatabase.getInstance().reference.child("users").child(it.key)
                    .addValueEventListener(object : ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.KITKAT)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null) {
                                return
                            }
                            val location: HashMap<String, Double> = snapshot.value as HashMap<String, Double>
                            val friend = friendLocations[it.key]

                            if (friend?.marker == null) {

                                spaceRef = storageRef.child("images/" + friend?.uid)

                                val ONE_MEGABYTE: Long = 5000 * 5000
                                spaceRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                                    // Data for "images/island.jpg" is returned, use this as needed
                                    var imageBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                                    var bmp = Bitmap.createScaledBitmap(imageBitmap, 120, 120, false)

                                    val icon = BitmapDescriptorFactory.fromBitmap(bmp)

                                    var loc = LatLng(location["latitude"]!!, location["longitude"]!!)

                                    var markerOptions = MarkerOptions().position(loc)
                                            .title(friend?.username)
//                                            .snippet(friend?.)
                                            .icon(icon)

                                    val marker = mMap.addMarker(markerOptions)
                                    friend?.latitude = location["latitude"]!!
                                    friend?.longitude = location["longitude"]!!
                                    friend?.marker = marker
                                }
                            }
                            friend?.marker?.moveMarkerSmoothly(LatLng(location["latitude"]!!, location["longitude"]!!), false)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }


    private fun moveCamera(location: LatLng,zoom: Float){
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(location))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
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
        } else
            return

    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
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
}

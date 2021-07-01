package aleksa.mosis.elfak.capturetheflag

import aleksa.mosis.elfak.capturetheflag.data.Game
import aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.data.UserLocation
import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.utsman.smartmarker.moveMarkerSmoothly
import kotlinx.android.synthetic.main.activity_join_game.*
import kotlinx.android.synthetic.main.activity_new_game.*
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class JoinGameActivity : AppCompatActivity(), OnMapReadyCallback {

    private var auth: FirebaseAuth = Firebase.auth
    private var firebaseUser: FirebaseUser = auth.currentUser as FirebaseUser

    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef: StorageReference? = null

    private val UPDATE_FREQUENCY: Long = 10000

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var players: HashMap<String, User> = HashMap<String, User>()
    private var playersLocation: HashMap<String, UserLocation> = HashMap<String, UserLocation>()




    private lateinit var mMap: GoogleMap
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        startTimer()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.gameMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_FREQUENCY
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        setUpLocationCallback()
        startLocationUpdates()


        addPlayer()



        Handler().postDelayed({
            startGame()
        }, 10000)


    }
    private fun addPlayer(){
        val pw = intent.getStringExtra("password").toString()
        FirebaseFirestore.getInstance().collection("games").document(pw).get()
            .addOnSuccessListener { ds ->

                    FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid).get()
                        .addOnSuccessListener {
                            val user = User(id = it.getString("id")!!,username = it.getString("username")!!)


                                Log.d(TAG, user!!.username)

                                FirebaseFirestore.getInstance().collection("games")
                                    .document(pw).update("players", FieldValue.arrayUnion(user))

                        }

            }

    }

    private fun startGame(){
        startLocationUpdates()


        val pw = intent.getStringExtra("password").toString()

        FirebaseFirestore.getInstance().collection("games").document(pw).get()
            .addOnSuccessListener { ds ->

//                    var game = ds.toObject(Game::class.java)
                    var players = ds["players"] as ArrayList<HashMap<String,User>>

//
//
//                    players?.forEach{
//                            var u = it
//                            playersLocation[u.id] = UserLocation(u.id, 0.0,0.0,null)
//                            playersLocation[u.id]?.username = u.username
//
//                        setupMap()
//                        FirebaseFirestore.getInstance().collection("games").document(pw).update("started", true)


                //manualno ubacivanje za testiranje
                playersLocation["91eXv3zNfVdaZkTKTZueNtGFABJ3"] = UserLocation("91eXv3zNfVdaZkTKTZueNtGFABJ3", 0.0, 0.0,null)
                playersLocation["91eXv3zNfVdaZkTKTZueNtGFABJ3"]?.username = "Colj"
                playersLocation["91eXv3zNfVdaZkTKTZueNtGFABJ3"]?.photoUri = "https://firebasestorage.googleapis.com/v0/b/capturetheflag-86338.appspot.com/o/images%2FGDYdjDwEhTgUXvyJuYMyh7Bzbzu1?alt=media&token=0760ff96-7a7d-45df-9045-6922daa0ba1b"
                setupMap()
                FirebaseFirestore.getInstance().collection("games").document(pw).update("started", true)
                        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableMyLocation()
    }
    private fun setUpLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return


                FirebaseDatabase.getInstance().reference.child("players").child(firebaseUser.uid)
                    .setValue(
                        GeoPoint(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude
                        )
                    )
            }
        }
    }
    private fun setupMap() {

        playersLocation.forEach {
            FirebaseDatabase.getInstance().reference.child("players").child(it.key)
                .addValueEventListener(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.KITKAT)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null) {
                            return
                        }
                        val location: HashMap<String, Double> = snapshot.value as HashMap<String, Double>
                        val player = playersLocation[it.key]

                        var loc = LatLng(location["latitude"]!!, location["longitude"]!!)

                        if (player?.marker == null) {
                            if(player?.photoUri != ""){
                                spaceRef = storageRef.child("images/" + player?.uid)

                                val ONE_MEGABYTE: Long = 5000 * 5000
                                spaceRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                                    // Data for "images/island.jpg" is returned, use this as needed
                                    var imageBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                                    // var drawable = BitmapDrawable(resources, imageBitmap)
                                    //drawable.setBounds(0,0,50,50)

                                    var bmp = Bitmap.createScaledBitmap(imageBitmap, 120, 120, false)

                                    val icon = BitmapDescriptorFactory.fromBitmap(bmp)



                                    var markerOptions = MarkerOptions().position(loc)
                                        .title(player?.username)
//                                            .snippet(friend?.)
                                        .icon(icon)

                                    val marker = mMap.addMarker(markerOptions)
                                    player?.latitude = location["latitude"]!!
                                    player?.longitude = location["longitude"]!!
                                    player?.marker = marker

                                }
                            }
                            else {
                                var markerOptions = MarkerOptions().position(loc)
                                    .title(player?.username)
//                                            .snippet(friend?.)

                                val marker = mMap.addMarker(markerOptions)
                                player?.latitude = location["latitude"]!!
                                player?.longitude = location["longitude"]!!
                                player?.marker = marker
                            }
                        }
                        player?.marker?.moveMarkerSmoothly(loc, false)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTimer(){
//        var start : HashMap<String,Long> ?= null
//        var milisecondsPicked : Long
//        //Odabrano vreme
//        FirebaseFirestore.getInstance().collection("games")
//            .whereEqualTo("password", intent.getStringExtra("password").toString())
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    Log.d(ContentValues.TAG, "NASO SAM TEEEE ")
//                    start = document.get("start") as HashMap<String, Long>
//                }
//                val hour = start!!["hour"]
//                val minute = start!!["minute"]
//                Log.d(ContentValues.TAG, "IZABRANO VREME " + hour.toString() + " " + minute.toString())
//                milisecondsPicked = (hour!! * 60 + minute!!) * 60 * 1000
//                Log.d(ContentValues.TAG, "NISAM TE NASO")
//
//                //Trenutno vreme
//                val time = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    LocalTime.now()
//                } else {
//                    TODO("VERSION.SDK_INT < O")
//                }
//                Log.d(ContentValues.TAG, "TRENUTNO VREME " + time.hour.toString() + " " + time.minute.toString())
//                val milisecondsNow = (time.hour * 60 + time.minute) * 60 * 1000
//
//                //Razlika i setovanje tajmera
//                view_timer.isCountDown = true
//                if(milisecondsPicked > milisecondsNow){
//                    view_timer.base = SystemClock.elapsedRealtime() + milisecondsPicked-milisecondsNow
//                }
//                else{
//                    view_timer.base = SystemClock.elapsedRealtime() + 12*60*60*1000 - (milisecondsNow - milisecondsPicked)
//                }
//                view_timer.start()
//            }
        view_timer.isCountDown = true
        view_timer.base = SystemClock.elapsedRealtime() + 5000

        view_timer.onChronometerTickListener = Chronometer.OnChronometerTickListener { chronometer ->
            val timeElapsed = SystemClock.elapsedRealtime() - chronometer.base
            if (timeElapsed >= 0) {
                println("ISTEKLO")
                view_timer.stop()
            }
        }
        view_timer.start()

    }
}
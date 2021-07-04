package aleksa.mosis.elfak.capturetheflag

import aleksa.mosis.elfak.capturetheflag.data.Flag
import aleksa.mosis.elfak.capturetheflag.data.Game
import aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.data.UserLocation
import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Chronometer
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.SphericalUtil
import com.utsman.smartmarker.moveMarkerSmoothly
import kotlinx.android.synthetic.main.activity_join_game.*
import kotlinx.android.synthetic.main.activity_new_game.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class JoinGameActivity : AppCompatActivity(), OnMapReadyCallback {
//    inner class flagObj(var id: String, var flags: ArrayList<Flag>?){
//
//    }

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

//    private var flagsList : flagObj? = null

    private lateinit var pw : String
    private lateinit var game : Game
    private lateinit var mMap: GoogleMap
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        pw = intent.getStringExtra("password").toString()
        FirebaseFirestore.getInstance().collection("games").document(pw).get()
            .addOnSuccessListener { ds ->
                game = ds.toObject(Game::class.java)!!
                    FirebaseDatabase.getInstance().reference.child("flags").child(pw).setValue(
                        game?.flags
                    )


                startTimer()
            }



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
            endGame()
        }, 20000)


    }
    private fun addPlayer(){
        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid).get()
            .addOnSuccessListener {
                val user = User(id = it.getString("id")!!, username = it.getString("username")!!)
                user.photoUri = it.getString("photoUri").toString()

                Log.d(TAG, user!!.username)

                FirebaseFirestore.getInstance().collection("games")
                    .document(pw).update("players", FieldValue.arrayUnion(user))

            }
    }

    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startGame(){
        startLocationUpdates()

        FirebaseFirestore.getInstance().collection("games").document(pw).get()
            .addOnSuccessListener { ds ->
                    game = ds.toObject(Game::class.java)!!
                    //var gameStart = ds.get("start") as HashMap<String, Long>
                    //game?.start = LocalTime.of(gameStart!!["hour"]!!.toInt() ,gameStart!!["minute"]!!.toInt())
                    var players = game?.players



                    players?.forEach {
                        var u = it
                        playersLocation[u.id] = UserLocation(u.id, 0.0, 0.0, null)
                        playersLocation[u.id]?.username = u.username
                        playersLocation[u.id]?.photoUri = u.photoUri
                    }

                setupMap()




                //manualno ubacivanje za testiranje
//                playersLocation["91eXv3zNfVdaZkTKTZueNtGFABJ3"] = UserLocation("91eXv3zNfVdaZkTKTZueNtGFABJ3", 0.0, 0.0,null)
//                playersLocation["91eXv3zNfVdaZkTKTZueNtGFABJ3"]?.username = "Colj"
//                playersLocation["91eXv3zNfVdaZkTKTZueNtGFABJ3"]?.photoUri = "https://firebasestorage.googleapis.com/v0/b/capturetheflag-86338.appspot.com/o/images%2FGDYdjDwEhTgUXvyJuYMyh7Bzbzu1?alt=media&token=0760ff96-7a7d-45df-9045-6922daa0ba1b"
//                setupMap()
//                FirebaseFirestore.getInstance().collection("games").document(pw).update("started", true)
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


                FirebaseDatabase.getInstance().reference.child("players").child(pw).child(
                    firebaseUser.uid
                )
                    .setValue(
                        GeoPoint(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude
                        )
                    )
                //proveravas CHANGED
                var userLoc : LatLng = LatLng(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                if(game.started == true){
                    var niz = ArrayList(game.flags)
                    niz?.forEach{ flag->
                        if(SphericalUtil.computeDistanceBetween(flag.marker?.position, userLoc)<100.0){
                            flag.marker?.remove()
                            flag.radius?.remove()
                            game.flags?.remove(flag)

                            FirebaseDatabase.getInstance().reference.child("flags").child(pw).setValue(
                                game?.flags
                            ).addOnSuccessListener {
                                Log.d(TAG, "1")
                            }

                            Log.d(TAG, "2")
                            var player = game.players.filter { it -> it.id == firebaseUser.uid }
                            FirebaseFirestore.getInstance().collection("games").document(pw).update(
                                "players", FieldValue.arrayRemove(player[0])
                            ).addOnSuccessListener {
                                Log.d(TAG, "3")
                                player[0].flags += flag.value
                                FirebaseFirestore.getInstance().collection("games").document(pw).update(
                                    "players", FieldValue.arrayUnion(player[0])
                                )
                                Toast.makeText(
                                    this@JoinGameActivity,
                                    "You got " + flag.value + " points!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d(TAG, "4")
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalStdlibApi
    private fun setupMap() {
        setupFlags()
        playersLocation.forEach {
            FirebaseDatabase.getInstance().reference.child("players").child(pw).child(it.key)
                .addValueEventListener(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.KITKAT)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null) {
                            return
                        }
                        val location: HashMap<String, Double> =
                            snapshot.value as HashMap<String, Double>
                        val player = playersLocation[it.key]

                        var loc = LatLng(location["latitude"]!!, location["longitude"]!!)

                        if (player?.marker == null) {
                            if (player?.photoUri != "") {
                                spaceRef = storageRef.child("images/" + player?.uid)

                                val ONE_MEGABYTE: Long = 5000 * 5000
                                spaceRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                                    // Data for "images/island.jpg" is returned, use this as needed
                                    var imageBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)


                                    var bmp = Bitmap.createScaledBitmap(
                                        imageBitmap,
                                        120,
                                        120,
                                        false
                                    )

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
                            } else {
                                var imageBitmap = BitmapFactory.decodeResource(resources,R.drawable.profile_icon)
                                var bmp = Bitmap.createScaledBitmap(
                                        imageBitmap,
                                        120,
                                        120,
                                        false
                                )
                                val icon = BitmapDescriptorFactory.fromBitmap(bmp)
                                var markerOptions = MarkerOptions().position(loc)
                                    .title(player?.username)
                                            .icon(icon)

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

    @ExperimentalStdlibApi
    private fun setupFlags(){
        FirebaseDatabase.getInstance().reference.child("flags").child(pw).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        return
                    }
                    Log.d(TAG, snapshot.toString())
                    var hashMapOfFlags = snapshot.value as ArrayList<HashMap<String,Any>>

                    Log.d(TAG, hashMapOfFlags.toString())

                    Log.d(TAG, game.flags.toString())

                    game?.flags?.forEach{
                        it.marker?.remove()
                        it.radius?.remove()
                    }
                    game?.flags?.clear()
                    Log.d(TAG, "SAD SI KLIROVAO NIZ = "+game.flags.toString())
                    hashMapOfFlags.forEach{
                        if(it!=null)
                            game?.flags?.add(Flag(it.get("longitude").toString().toDouble(),it.get("latitude").toString().toDouble(),it.get("value").toString().toInt()))
                       }
                    Log.d(TAG, game.flags.toString())

                    refreshFlags()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        FirebaseFirestore.getInstance().collection("games").document(pw).update(
            "started",
            true
        ).addOnSuccessListener {
            game.started = true;
        }
    }
    private fun refreshFlags(){
        game.flags?.forEach {
            val circle: Circle = mMap.addCircle(
                CircleOptions()
                    .center(LatLng(it.latitude, it.longitude))
                    .radius(100.0)
                    .strokeColor(Color.RED)
            )
            var loc = LatLng(it.latitude, it.longitude)
            var markerOptions = MarkerOptions().position(loc).title(it.value.toString())
            it.marker = mMap.addMarker(markerOptions)
            it.radius = circle
        }
    }

    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTimer(){
//        var milisecondsPicked : Long
//        //Odabrano vreme
//                val hour = game.start["hour"]
//                val minute = game.start["minute"]
//                Log.d(ContentValues.TAG, "IZABRANO VREME " + hour.toString() + " " + minute.toString())
//                milisecondsPicked = (hour!!.toLong() * 60 + minute!!.toLong()) * 60 * 1000
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

        view_timer.isCountDown = true
        view_timer.base = SystemClock.elapsedRealtime() + 5000

        view_timer.onChronometerTickListener = Chronometer.OnChronometerTickListener { chronometer ->
            val timeElapsed = SystemClock.elapsedRealtime() - chronometer.base
            if (timeElapsed >= 0) {
                println("ISTEKLO")
                view_timer.stop()
                startGame()
            }
        }
        view_timer.start()

    }

    private fun endGame(){
        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid).get()
                .addOnSuccessListener {
                    var us = it.toObject(User::class.java)
                    FirebaseFirestore.getInstance().collection("games").document(pw).get()
                            .addOnSuccessListener { gameSnap ->
                                var currentGame = gameSnap.toObject(Game::class.java)
                                var winner = currentGame?.players?.maxBy{ score -> score.flags }
                                if(winner?.id == firebaseUser.uid){
                                    us?.flags = us?.flags?.plus(winner.flags)!!
                                    us.addWin()
                                    FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid).update(
                                            "flags",us?.flags,
                                    "won", us.won
                                    )
                                    showEndGameDialog(us?.flags)
                                }
                                else {
                                    var us2 = currentGame?.players?.filter { usr -> usr.id == us?.id}
                                    var score = us2?.get(0)?.flags!!.toInt()
                                    us?.flags = us?.flags?.plus(score)!!
                                    FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid).update(
                                            "flags",us?.flags
                                    )
                                    showEndGameDialog(us?.flags)
                                }
                            }
        }
    }

    private fun showEndGameDialog(score: Int){
        var builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Score: $score")
            setTitle("Game over")
            setPositiveButton("Confirm") { dialog, which ->
                finish()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }


}
package aleksa.mosis.elfak.capturetheflag

import aleksa.mosis.elfak.capturetheflag.data.Flag
import aleksa.mosis.elfak.capturetheflag.data.Game
import aleksa.mosis.elfak.capturetheflag.data.UserLocation
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_game.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.time.LocalTime
import kotlin.reflect.typeOf
import kotlin.time.ExperimentalTime


class NewGameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private lateinit var user : FirebaseUser
    private var markers: HashMap<String, Marker> = HashMap<String, Marker>()

    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)
        user = Firebase.auth.currentUser as FirebaseUser



        var mapFragment = supportFragmentManager
            .findFragmentById(R.id.map1) as SupportMapFragment
        mapFragment.getMapAsync(this)


        btn_generate_pass.setOnClickListener{

            var getStartTime = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalTime.of(timePickerStart.hour, timePickerStart.minute)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val duration = et_duration.text.toString().toInt()

            val randomString = (1..10)
                    .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("");

            var value : Int
            var flagArray : ArrayList<Flag> ?= ArrayList()
            markers.forEach { (key, marker) ->
                if(marker.tag == "3points")
                    value  = 3
                else
                    value = 5
                val flag = Flag(latitude = marker.position.latitude , longitude = marker.position.longitude ,value = value, marker = null, radius = null)
                flagArray?.add(flag)
            }
            val docRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                    val owner = documentSnapshot.getString("username").toString()
                    val game = Game(owner = owner, duration = duration,flags = flagArray)
                    game.start["hour"] = getStartTime.hour
                    game.start["minute"] = getStartTime.minute
                    FirebaseFirestore.getInstance().collection("games")
                         .add(game).addOnSuccessListener {
                             game.password = it.id
                            FirebaseFirestore.getInstance().collection("games").document(it.id).update(
                                "password" , it.id
                            )
                            showDialog(it.id)
                        }

            }
        }


    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        mMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

                val location = LatLng(latlng.latitude,latlng.longitude)
                val marker = mMap.addMarker(MarkerOptions().position(location).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                marker.tag = "3points"
                markers[marker.id] = marker
            }
        })
        mMap.setOnMapLongClickListener(object :GoogleMap.OnMapLongClickListener {
            override fun onMapLongClick(latlng :LatLng) {

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

                val location = LatLng(latlng.latitude,latlng.longitude)
                val marker = mMap.addMarker(MarkerOptions().position(location).icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                markers[marker.id] = marker
            }
        })
        mMap.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker) : Boolean{
                markers.remove(p0.id)
                p0.remove()
                return true
            }
        })

    }



    private fun requestPermission(permission: String, name: String, requestCode: Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_DENIED ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }

        }
    }

    private fun showDialog(password: String){
        var builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Your password:")
            setTitle("New game")
        }
        val edittext = EditText(this)
        edittext.setText(password)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        edittext.setLayoutParams(lp)
        builder.setView(edittext)
        val dialog = builder.create()
        dialog.show()
    }
}



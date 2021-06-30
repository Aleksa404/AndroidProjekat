package aleksa.mosis.elfak.capturetheflag

import aleksa.mosis.elfak.capturetheflag.data.Game
import android.content.ContentValues
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_new_game.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.time.LocalTime
import kotlin.time.ExperimentalTime


class NewGameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private lateinit var user : FirebaseUser

    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)
        user = Firebase.auth.currentUser as FirebaseUser

//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync()


        var mapFragment = supportFragmentManager
            .findFragmentById(R.id.map1) as SupportMapFragment
        mapFragment.getMapAsync(this)


        btn_generate_pass.setOnClickListener{
            startTimer()

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
            val docRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                    val owner = documentSnapshot.getString("username").toString()
                    val game = Game(owner = owner, start = getStartTime, duration = duration, password = randomString)
                    FirebaseFirestore.getInstance().collection("games")
                         .document().set(game)
                    showDialog(randomString)
            }
        }


    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private val STORAGE_RQ = 102

    private fun requestPermission(permission: String, name: String, requestCode: Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_DENIED ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }

        }
    }

    private fun startTimer(){
        //Odabrano vreme
        val hour = timePickerStart.currentHour
        val minute = timePickerStart.currentMinute
        Log.d(ContentValues.TAG, "IZABRANO VREME " + hour.toString() + " " + minute.toString())
        val milisecondsPicked = (hour * 60 + minute) * 60 * 1000

        //Trenutno vreme
        val time = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        Log.d(ContentValues.TAG, "TRENUTNO VREME " + time.hour.toString() + " " + time.minute.toString())
        val milisecondsNow = (time.hour * 60 + time.minute) * 60 * 1000

        //Razlika i setovanje tajmera
        view_timer.isCountDown = true
        if(milisecondsPicked > milisecondsNow){
            view_timer.base = SystemClock.elapsedRealtime() + milisecondsPicked-milisecondsNow
        }
        else{
            view_timer.base = SystemClock.elapsedRealtime() + 12*60*60*1000 - (milisecondsNow - milisecondsPicked)
        }
        view_timer.start()
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



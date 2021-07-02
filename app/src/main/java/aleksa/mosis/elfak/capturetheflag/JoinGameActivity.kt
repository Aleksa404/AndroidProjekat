package aleksa.mosis.elfak.capturetheflag

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_join_game.*
import kotlinx.android.synthetic.main.activity_new_game.*
import java.util.*


class JoinGameActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        startTimer()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.gameMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
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
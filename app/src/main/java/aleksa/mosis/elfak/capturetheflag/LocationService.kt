package aleksa.mosis.elfak.capturetheflag

import aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.data.UserLocation
import android.Manifest
import android.R
import android.app.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
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
import com.google.maps.android.SphericalUtil
import kotlin.reflect.typeOf


class LocationService: Service() {

    private var auth: FirebaseAuth = Firebase.auth
    private var user: FirebaseUser = auth.currentUser as FirebaseUser

    private val UPDATE_FREQUENCY: Long = 3000

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private lateinit var runningNotifChanel: NotificationChannel
    private lateinit var closeNotifChannel: NotificationChannel
    private val RUNNING_NOTIF_CHANNEL_ID: String = "running_in_background"
    private val CLOSE_NOTIF_CHANNEL_ID : String = "user_close_notification"

    private var friendLocations: HashMap<String, UserLocation> = HashMap<String, UserLocation>()

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        createNotifChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val notification: Notification = NotificationCompat.Builder(this, RUNNING_NOTIF_CHANNEL_ID)
            .setContentTitle("Capture the flag")
            .setContentText("Location updates")
            .setContentIntent(pendingIntent).build()

        startForeground(1337, notification)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_FREQUENCY
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        createNotifChannel()
        setUpLocationCallback()

        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
            .addOnSuccessListener { ds ->
                var me = ds.toObject(User::class.java)
                var friends: List<User> = me?.friends as List<User>
                friends.forEach { user ->

                    friendLocations[user.id] = UserLocation(user.id, 0.0, 0.0, null)
                    friendLocations[user.id]?.username = user.username

                }
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
                computeDistance(locationResult)
            }
        }
    }

    private fun computeDistance(locationResult: LocationResult){
        FirebaseDatabase.getInstance().reference.child("users").get().addOnSuccessListener {
            var users = it.value as HashMap<String,HashMap<String, Double>>


            for ((userId, map) in users){
                if(userId != user.uid){
                    var loc = LatLng(map["latitude"]!!,map["longitude"]!!)
                    var myLoc = LatLng(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude)
                    if(SphericalUtil.computeDistanceBetween(myLoc,loc)<50.0){
                        createNotif()
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (checkPermission()) {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
        else return
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startLocationUpdates()
        return super.onStartCommand(intent, flags, startId)
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

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
    private fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotifChannel(){

        val channelName : CharSequence = "Capture the flag"
        runningNotifChanel = NotificationChannel(RUNNING_NOTIF_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        var notifManager = getSystemService(NotificationManager::class.java)
        notifManager.createNotificationChannel(runningNotifChanel)

        closeNotifChannel = NotificationChannel(CLOSE_NOTIF_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notifManager.createNotificationChannel(closeNotifChannel)

    }
    private fun createNotif() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this@LocationService, CLOSE_NOTIF_CHANNEL_ID)
                .setContentTitle("A friend is nearby")
                .setSmallIcon(R.drawable.ic_menu_today)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)


        val notificationManager = NotificationManagerCompat.from(this)
        val notificationId = 1
        notificationManager.notify(notificationId, builder.build())
    }
}
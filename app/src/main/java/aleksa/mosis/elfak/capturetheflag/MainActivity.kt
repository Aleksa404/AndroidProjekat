package aleksa.mosis.elfak.capturetheflag

import aleksa.mosis.elfak.capturetheflag.leaderboard.LeaderboardActivity
import aleksa.mosis.elfak.capturetheflag.myFriends.FindFriendsActivity
import aleksa.mosis.elfak.capturetheflag.profile.EditProfileActivity
import aleksa.mosis.elfak.capturetheflag.profile.ProfileActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*


class MainActivity : AppCompatActivity() {

    private lateinit var user : FirebaseUser
    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef : StorageReference? = null

    override fun onStart() {
        super.onStart()
        user = Firebase.auth.currentUser as FirebaseUser
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check permissions

        requestPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            "Location",
            FINE_LOCATION_RQ
        )


        btn_new_game.setOnClickListener {

            val intent = Intent(this@MainActivity, NewGameActivity::class.java)
            startActivity(intent)

        }
        btn_map.setOnClickListener{
            startActivity(Intent(this, MapsActivity::class.java))
        }
        btn_join_game.setOnClickListener {
        //TODO



        }
        btn_find_firends.setOnClickListener {
            startActivity(Intent(this, FindFriendsActivity::class.java))
        }

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
        btn_leaderboard.setOnClickListener {
            startActivity(Intent(this@MainActivity, LeaderboardActivity::class.java))
        }
        btn_join_game.setOnClickListener {
            //startActivity(Intent(this@MainActivity, JoinGameActivity::class.java))
            showDialogForPassword()
        }



    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        if (user != null) {
                    spaceRef = storageRef.child("images/" + user.uid.toString())

                    val ONE_MEGABYTE: Long = 5000 * 5000
                    spaceRef?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
                        // Data for "images/island.jpg" is returned, use this as needed
                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        val btmDrawable = BitmapDrawable(resources,bitmap)
                        menu?.findItem(R.id.menuProfile)?.setIcon(btmDrawable)
                    }?.addOnFailureListener {
                        // Handle any errors
                    }
        }


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menuProfile){
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }


    private val FINE_LOCATION_RQ = 101


    private fun requestPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perm = ContextCompat.checkSelfPermission(this, permission)
            if (perm != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            {
                showDialog(permission, name, requestCode)
            }

        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//
//        fun innerCheck(name: String) {
//            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        when (requestCode) {
//            FINE_LOCATION_RQ -> innerCheck("location")
//            STORAGE_RQ -> innerCheck("Storage")
//        }


//    }

    private fun showDialog(permission: String, name: String, requestCode: Int){
        var builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun showDialogForPassword(){
        var builder = AlertDialog.Builder(this)
        val edittext = EditText(this)
        edittext.setText("")
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        edittext.setLayoutParams(lp)
        builder.apply {
            setMessage("Join game:")
            setTitle("Enter password:")
            setView(edittext)
            setPositiveButton("Confirm") { dialog, which ->
                checkPassword(edittext.text.toString())
            }
            setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun checkPassword(password : String){
        FirebaseFirestore.getInstance().collection("games")
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                if(documents.size()== 0){
                    Toast.makeText(applicationContext, "Wrong password", Toast.LENGTH_SHORT).show()
                }
                else{
                    val intent = Intent(this@MainActivity, JoinGameActivity::class.java)
                    var pass : String = ""
                    for (document in documents) {
                        pass = document.getString("password").toString()
                    }
                    intent.putExtra("password" , pass)
                    startActivity(intent)
                }
            }
    }
}
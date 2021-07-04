package aleksa.mosis.elfak.capturetheflag.profile

import aleksa.mosis.elfak.capturetheflag.R
import aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.myFriends.MyFriendsActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var user : FirebaseUser
    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef : StorageReference? = null

    private val STORAGE_RQ = 102

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)



        user = Firebase.auth.currentUser as FirebaseUser
        if (user != null) {
             // User signed in
             spaceRef = storageRef.child("images/"+user.uid.toString())

             val docRef = getInstance().collection("users").document(user.uid)
             docRef.get().addOnSuccessListener { documentSnapshot ->
                 text_view_username.text = documentSnapshot.getString("username")
                 text_view_name.text = documentSnapshot.getString("name")
                 text_view_surname.text = documentSnapshot.getString("surname")
                 text_view_email.text = documentSnapshot.getString("email")
                 text_view_phone.text = documentSnapshot.getString("phone")
                 text_view_matches.text = documentSnapshot.getLong("matches").toString()
                 text_view_flags.text = documentSnapshot.getLong("flags").toString()
                 text_view_won.text = documentSnapshot.getLong("won").toString()

                 val photo = documentSnapshot.getString("photoUri")
                 if (photo != "") {
                     Glide.with(this).load(photo).centerCrop().override(512, 512).into(profile_image);
                 } else {
                     profile_image.setImageURI(Uri.parse("android.resource://$packageName/${R.drawable.profile_icon}"))
                 }

             }

        }

        profile_image.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 1000)
                }
                else requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, "Storage", STORAGE_RQ)

            }

        }

        btn_edit.setOnClickListener{
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)

            intent.putExtra("username",text_view_username.text.toString())
            intent.putExtra("name",text_view_name.text.toString())
            intent.putExtra("surname",text_view_surname.text.toString())
            intent.putExtra("phone",text_view_phone.text.toString())

            startActivityForResult(intent, 2000)
        }
        btn_friends.setOnClickListener{
            val intent = Intent(this@ProfileActivity, MyFriendsActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000){
            profile_image.setImageURI(data?.data)
            profile_image.layoutParams.height = 512;
            profile_image.layoutParams.width = 512;
            profile_image.scaleType=ImageView.ScaleType.CENTER_CROP

            profile_image.isDrawingCacheEnabled = true
            profile_image.buildDrawingCache()
            val bitmap = (profile_image.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data1 = baos.toByteArray()
            var uploadTask = spaceRef?.putBytes(data1)


            uploadTask?.addOnSuccessListener {
                saveUri()
            }

            uploadTask?.addOnFailureListener {
                Toast.makeText(this@ProfileActivity, "Image did not upload",
                    Toast.LENGTH_LONG).show()
            }

        }
        else if(requestCode == 2000){
            if(resultCode!= RESULT_CANCELED) { //Ako si stiso save dugme
                text_view_username.text = data?.getStringExtra("username").toString()
                text_view_name.text = data?.getStringExtra("name").toString()
                text_view_surname.text = data?.getStringExtra("surname").toString()
                text_view_phone.text = data?.getStringExtra("phone").toString()
                updateProfileInfo()
            }
            //Ako si backovao
        }

    }
    private fun updateProfileInfo() {
        val obj = User(user.uid,
            text_view_username.text.toString(),
            text_view_email.text.toString(),
            text_view_name.text.toString(),
            text_view_surname.text.toString(),
            text_view_phone.text.toString())


        getInstance().collection("users").document(user.uid).update(
        "username", obj.username,
            "name", obj.name,
            "surname", obj.surname,
            "phone", obj.phone
        )
    }

    private fun saveUri() {
            storageRef.child("images/" + user.uid).downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    getInstance().collection("users").document(user.uid)
                        .update("photoUri", downloadUri)

            }
        }
    }


    private fun requestPermission(permission: String, name : String, requestCode : Int) {
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
    private fun showDialog(permission: String, name : String, requestCode: Int){
        var builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(this@ProfileActivity, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

}
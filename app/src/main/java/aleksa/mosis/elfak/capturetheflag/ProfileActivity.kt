package aleksa.mosis.elfak.capturetheflag

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var user : FirebaseUser
    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef : StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        user = Firebase.auth.currentUser as FirebaseUser
        if (user != null) {
             // User signed in
             spaceRef = storageRef.child("images/"+user.uid.toString())
             val docRef = getInstance().collection("users").document(user.uid)
             docRef.get().addOnSuccessListener { documentSnapshot ->
                 text_view_username.setText(documentSnapshot.getString("username"))
                 text_view_name.setText(documentSnapshot.getString("name"))
                 text_view_surname.setText(documentSnapshot.getString("surname"))
                 text_view_email.setText(documentSnapshot.getString("email"))
                 text_view_phone.setText(documentSnapshot.getString("phone"))
                 text_view_matches.setText(documentSnapshot.getLong("matches").toString())
                 text_view_flags.setText(documentSnapshot.getLong("flags").toString())
                 text_view_won.setText(documentSnapshot.getLong("won").toString())
                 var task = storageRef.child("images/"+user.uid.toString()).downloadUrl.addOnCompleteListener{ task ->
                         if (task.isSuccessful) {
                             val downloadUri = task.result
                             Glide.with(this).load(downloadUri).centerCrop().override(512, 512).into(profile_image);
                         } else {
                             Toast.makeText(this@ProfileActivity, "Couldn't find image",
                                 Toast.LENGTH_LONG).show()
                             profile_image.setImageURI(Uri.parse("android.resource://$packageName/${R.drawable.profile_icon}"))
                         }
                 }
             }

        }

        profile_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }

        btn_edit.setOnClickListener{
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)

            intent.putExtra("username",text_view_username.text.toString())
            intent.putExtra("name",text_view_name.text.toString())
            intent.putExtra("surname",text_view_surname.text.toString())
            intent.putExtra("phone",text_view_phone.text.toString())

            startActivityForResult(intent, 2000)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000){
            profile_image.setImageURI(data?.data)
            profile_image.getLayoutParams().height = 512;
            profile_image.getLayoutParams().width = 512;
            profile_image.scaleType=ImageView.ScaleType.CENTER_CROP

            profile_image.isDrawingCacheEnabled = true
            profile_image.buildDrawingCache()
            val bitmap = (profile_image.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            var uploadTask = spaceRef?.putBytes(data)
            uploadTask?.addOnFailureListener {
                Toast.makeText(this@ProfileActivity, "Image did not upload",
                    Toast.LENGTH_LONG).show()
            }
        }
        else if(requestCode == 2000){
            if(resultCode!= RESULT_CANCELED) { //Ako si stiso save dugme
                text_view_username.setText(data?.getStringExtra("username").toString())
                text_view_name.setText(data?.getStringExtra("name").toString())
                text_view_surname.setText(data?.getStringExtra("surname").toString())
                text_view_phone.setText(data?.getStringExtra("phone").toString())
                updateProfileInfo()
            }
            //Ako si backovao
        }

    }
    private fun updateProfileInfo() {
        val obj = User(user.uid, text_view_username.text.toString(), text_view_email.text.toString(), text_view_name.text.toString(), text_view_surname.text.toString(), text_view_phone.text.toString())
        obj.matches = text_view_matches.text.toString().toInt()
        obj.won = text_view_won.text.toString().toInt()
        obj.flags = text_view_flags.text.toString().toInt()
        obj.addFlag()
        getInstance().collection("users").document(user.uid).set(obj)

    }
}
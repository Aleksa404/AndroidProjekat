package aleksa.mosis.elfak.capturetheflag

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_profile.*

class GuestProfileActivity : AppCompatActivity() {

    var storage = Firebase.storage
    var storageRef = storage.reference
    var spaceRef : StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_profile)

        var id : String = intent.getStringExtra("userId").toString()
            spaceRef = storageRef.child("images/"+id)

            val docRef = FirebaseFirestore.getInstance().collection("users").document(id)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                text_view_username.text = documentSnapshot.getString("username")
                text_view_name.text = documentSnapshot.getString("name")
                text_view_surname.text = documentSnapshot.getString("surname")
                text_view_email.text = documentSnapshot.getString("email")
                text_view_phone.text = documentSnapshot.getString("phone")
                text_view_matches.text = documentSnapshot.getLong("matches").toString()
                text_view_flags.text = documentSnapshot.getLong("flags").toString()
                text_view_won.text = documentSnapshot.getLong("won").toString()
//                 var task = storageRef.child("images/"+user.uid.toString()).downloadUrl.addOnCompleteListener{ task ->
//                         if (task.isSuccessful) {
//                             val downloadUri = task.result
//                             Glide.with(this).load(downloadUri).centerCrop().override(512, 512).into(profile_image);
//                         } else {
//                             Toast.makeText(this@ProfileActivity, "Couldn't find image",
//                                 Toast.LENGTH_LONG).show()
//                             profile_image.setImageURI(Uri.parse("android.resource://$packageName/${R.drawable.profile_icon}"))
//                         }
//                 }
                val photo = documentSnapshot.getString("photoUri")
                if (photo != "") {
                    Glide.with(this).load(photo).centerCrop().override(512, 512).into(profile_image);
                } else {
                    Toast.makeText(this@GuestProfileActivity, "Couldn't find image",
                        Toast.LENGTH_LONG).show()
                    profile_image.setImageURI(Uri.parse("android.resource://$packageName/${R.drawable.profile_icon}"))
                }

            }
    }
}
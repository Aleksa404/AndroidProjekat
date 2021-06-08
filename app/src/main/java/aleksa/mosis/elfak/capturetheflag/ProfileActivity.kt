package aleksa.mosis.elfak.capturetheflag

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var user : FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user = Firebase.auth.currentUser as FirebaseUser
        if (user != null) {
             // User signed in
             val docRef = getInstance().collection("users").document(user.uid)
             docRef.get().addOnSuccessListener { documentSnapshot ->
//                 val userObj = documentSnapshot.toObject<User>()
                 text_view_username.setText(documentSnapshot.getString("username"))
                 text_view_name.setText(documentSnapshot.getString("name"))
                 text_view_surname.setText(documentSnapshot.getString("surname"))
                 text_view_email.setText(documentSnapshot.getString("email"))
                 text_view_phone.setText(documentSnapshot.getString("phone"))
                 text_view_matches.setText(documentSnapshot.getLong("matches").toString())
                 text_view_flags.setText(documentSnapshot.getLong("flags").toString())
                 text_view_won.setText(documentSnapshot.getLong("won").toString())
                 if(user.photoUrl != null)
                     profile_image.setImageURI(user.photoUrl)
                 else
                     profile_image.setImageURI(Uri.parse("android.resource://$packageName/${R.drawable.profile_icon}"))
             }

        }

        profile_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000){
            profile_image.setImageURI(data?.data) // handle chosen image
            updateProfile(data?.data)
        }
    }
    private fun updateProfile(img : android.net.Uri?) {
        user?.let { user ->
            val photoURI = img
            val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(photoURI)
                    .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateProfile(profileUpdates).await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "Successfully updated profile",
                                Toast.LENGTH_LONG).show()
                    }
                } catch(e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }
}
package aleksa.mosis.elfak.capturetheflag

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user = Firebase.auth.currentUser
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
             }
        } else {
            // No user is signed in
            Toast.makeText(
                    this@ProfileActivity, "NEED TO SIGN IN!", Toast.LENGTH_SHORT).show()
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
        }
    }
}
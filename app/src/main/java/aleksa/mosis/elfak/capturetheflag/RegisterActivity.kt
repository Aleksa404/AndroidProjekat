package aleksa.mosis.elfak.capturetheflag

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import kotlin.collections.HashMap


class RegisterActivity : AppCompatActivity() {
//    var db = getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        


        btn_register.setOnClickListener {
            when {
                TextUtils.isEmpty(et_register_email.text.toString().trim { it <= ' ' }) ->{
                    Toast.makeText(this@RegisterActivity, "Enter email.", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(et_register_password.text.toString().trim { it <= ' ' }) ->{
                    Toast.makeText(this@RegisterActivity, "Enter password.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val email: String = et_register_email.text.toString().trim { it <= ' '}
                    val password: String = et_register_password.text.toString().trim { it <= ' '}
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "you were registered Successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()

//                                val documentReference : DocumentReference = getInstance().collection("users")
//                                    .document(firebaseUser.uid)
//                                val user : HashMap<String, Any> = HashMap()
//                                user.put("Name", et_register_name.text.toString())
//                                documentReference.set(user).addOnSuccessListener {
//                                    Toast.makeText(
//                                        this@RegisterActivity, "Created user with id: "+firebaseUser.uid, Toast.LENGTH_SHORT)
//                                        .show()
//                                }

                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
        }
        textView_login_here.setOnClickListener(){
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
        }
    }
}
package aleksa.mosis.elfak.capturetheflag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener {
            when {
                TextUtils.isEmpty(et_login_email.text.toString().trim{it <= ' '}) ->{
                    Toast.makeText(this@LoginActivity,"Enter email.", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(et_login_password.text.toString().trim{it <= ' '}) ->{
                    Toast.makeText(this@LoginActivity,"Enter password.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val email: String = et_login_email.text.toString().trim { it <= ' '}
                    val password: String = et_login_password.text.toString().trim { it <= ' '}

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if(task.isSuccessful) {
                                val firebaseUser : FirebaseUser = task.result!!.user!!

                                Toast.makeText(this@LoginActivity,"you were logged in Successfully.",
                                    Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@LoginActivity,MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                Toast.makeText(this@LoginActivity, task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        }
    }
}
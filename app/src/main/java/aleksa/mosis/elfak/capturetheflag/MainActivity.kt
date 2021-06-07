package aleksa.mosis.elfak.capturetheflag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_new_game.setOnClickListener {

            val intent = Intent(this@MainActivity, NewGameActivity::class.java)
            startActivity(intent)

        }
        btn_join_game.setOnClickListener {
        //TODO


        }
        btn_find_firends.setOnClickListener {
        //TODO
        }

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this@MainActivity,LoginActivity::class.java))
            finish()
        }
    }
}
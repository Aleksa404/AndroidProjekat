package aleksa.mosis.elfak.capturetheflag

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.google.firebase.firestore.FirebaseFirestore.getInstance
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_leaderboard.*

class LeaderboardActivity : AppCompatActivity() {

    var storage = Firebase.storage
    var storageRef = storage.reference

    private var mArrayAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)


        mArrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1)
        mArrayAdapter!!.add("cao prvi igrac")
        leaderboard_list.adapter = mArrayAdapter



    }
}
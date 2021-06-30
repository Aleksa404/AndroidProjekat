package aleksa.mosis.elfak.capturetheflag.leaderboard

import aleksa.mosis.elfak.capturetheflag.profile.GuestProfileActivity
import aleksa.mosis.elfak.capturetheflag.profile.ProfileActivity
import aleksa.mosis.elfak.capturetheflag.R
import aleksa.mosis.elfak.capturetheflag.data.User
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore.getInstance
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_leaderboard.*
import kotlinx.android.synthetic.main.activity_profile.*

class LeaderboardActivity : AppCompatActivity(), UserRecyclerAdapter.onItemClickListener {

    var storage = Firebase.storage
    var storageRef = storage.reference
    private lateinit var userAdapter: UserRecyclerAdapter

    private var mArrayAdapter: ArrayAdapter<String>? = null

    var userList: ArrayList<User> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)


        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        mArrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1)

        progressBar_fetching.visibility = View.VISIBLE



        val users = getInstance().collection("users").orderBy("flags", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener { result ->
            result.forEach { user ->
                var us = user.toObject(User::class.java)
                mArrayAdapter!!.add(us.username)
                userList.add(us)
            }
//            leaderboard_list.adapter = mArrayAdapter
            progressBar_fetching.visibility = View.GONE
            initRecyclerView()
            userAdapter.submitList(userList)
        }


    }


    private fun initRecyclerView(){
        recycle_view.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            val spacingItemDecoration = SpacingItemDecoration(30)
            addItemDecoration(spacingItemDecoration)
            userAdapter = UserRecyclerAdapter(this@LeaderboardActivity)
            adapter = userAdapter
        }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this,"item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = userList[position]
        var intent = Intent(this, GuestProfileActivity::class.java)
        intent.putExtra("userId",userList[position].id)
        startActivity(intent)
    }
}
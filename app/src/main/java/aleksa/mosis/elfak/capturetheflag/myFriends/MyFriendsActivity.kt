package aleksa.mosis.elfak.capturetheflag.myFriends

import aleksa.mosis.elfak.capturetheflag.profile.GuestProfileActivity
import aleksa.mosis.elfak.capturetheflag.profile.ProfileActivity
import aleksa.mosis.elfak.capturetheflag.R
import aleksa.mosis.elfak.capturetheflag.data.User
import aleksa.mosis.elfak.capturetheflag.leaderboard.SpacingItemDecoration
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_leaderboard.*
import kotlinx.android.synthetic.main.activity_my_friends.*


class MyFriendsActivity : AppCompatActivity(), UserRecyclerAdapterForFriends.onItemClickListener {
    var storage = Firebase.storage
    var storageRef = storage.reference
    private lateinit var userAdapter: UserRecyclerAdapterForFriends
    private lateinit var user : FirebaseUser

    private var mArrayAdapter: ArrayAdapter<String>? = null

    var userList: ArrayList<User> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        user = Firebase.auth.currentUser as FirebaseUser
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_friends)


        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        mArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        progressBar_fetching_friends.visibility = View.VISIBLE



        val users = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { result ->
                var me = result.toObject(User::class.java)
                me?.friends?.forEach { user ->
                mArrayAdapter!!.add(user.username)
                userList.add(user)
            }

            progressBar_fetching_friends.visibility = View.GONE
            initRecyclerView()
            userAdapter.submitList(userList)
        }


    }



    private fun initRecyclerView(){
        recycle_view_friends.apply {
            layoutManager = LinearLayoutManager(this@MyFriendsActivity)
            val spacingItemDecoration = SpacingItemDecoration(30)
            addItemDecoration(spacingItemDecoration)
            userAdapter = UserRecyclerAdapterForFriends(this@MyFriendsActivity, user.uid)
            adapter = userAdapter
        }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = userList[position]
        var intent = Intent(this, GuestProfileActivity::class.java)
        intent.putExtra("userId", userList[position].id)
        startActivity(intent)
    }

}
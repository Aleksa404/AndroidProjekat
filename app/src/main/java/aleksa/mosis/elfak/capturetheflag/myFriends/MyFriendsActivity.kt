package aleksa.mosis.elfak.capturetheflag.myFriends

import aleksa.mosis.elfak.capturetheflag.GuestProfileActivity
import aleksa.mosis.elfak.capturetheflag.ProfileActivity
import aleksa.mosis.elfak.capturetheflag.R
import aleksa.mosis.elfak.capturetheflag.User
import aleksa.mosis.elfak.capturetheflag.leaderboard.SpacingItemDecoration
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_leaderboard.*
import kotlinx.android.synthetic.main.activity_leaderboard.progressBar_fetching
import kotlinx.android.synthetic.main.activity_leaderboard.recycle_view
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
//            leaderboard_list.adapter = mArrayAdapter
            progressBar_fetching_friends.visibility = View.GONE
            initRecyclerView()
            userAdapter.submitList(userList)
        }


    }
//    var document: DocumentSnapshot = task.getResult()
//    var group = document["dungeon_group"] as List<String>?

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menuProfile){
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
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
package aleksa.mosis.elfak.capturetheflag.myFriends

import aleksa.mosis.elfak.capturetheflag.MainActivity
import aleksa.mosis.elfak.capturetheflag.ProfileActivity
import aleksa.mosis.elfak.capturetheflag.R
import aleksa.mosis.elfak.capturetheflag.User
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.layout_user_list_item.view.*
import kotlinx.android.synthetic.main.layout_user_list_item.view.txt_username
import kotlinx.android.synthetic.main.layout_user_list_item_for_friends.view.*


class UserRecyclerAdapterForFriends(private val listener: MyFriendsActivity, currentUser : String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var currentUser = currentUser
    private var items: List<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_user_list_item_for_friends, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is UserViewHolder -> {
                holder.bind(items.get(position))

            }
        }
    }

    override fun getItemCount(): Int {
       return items.size
    }

    fun submitList(userList : List<User>){
        items = userList
    }


    inner class UserViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView) , View.OnClickListener{

        init {
            itemView.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }

        }

        val userImage = itemView.user_image_friends
        val userUsername = itemView.txt_username_friends
        val button = itemView.btn_remove_friend

        fun bind(user: User){
            userUsername.setText(user.username)

            button.setOnClickListener{
                removeFriend(user.id)
            }

            if(user.photoUri != "") {
                Glide.with(itemView.context).load(Uri.parse(user.photoUri)).centerCrop().override(512, 512).into(userImage)
            }
            else{
                userImage.setImageURI(Uri.parse("android.resource://aleksa.mosis.elfak.capturetheflag/${R.drawable.profile_icon}"))
            }
        }
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun removeFriend(friendId : String){
        //PRVI
        val docRef = FirebaseFirestore.getInstance().collection("users").document(friendId)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            var userObjFriend = documentSnapshot.toObject(User::class.java)
            userObjFriend?.friends = ArrayList()

            //DRUGI
            val docRefOtherSide = FirebaseFirestore.getInstance().collection("users").document(currentUser)
            docRefOtherSide.get().addOnSuccessListener { documentSnapshot ->
                var userObjMe = documentSnapshot.toObject(User::class.java)
                userObjMe?.friends = ArrayList()

                //BRISANJE OBA
                FirebaseFirestore.getInstance().collection("users").document(friendId).update(
                    "friends", FieldValue.arrayRemove(userObjMe)
                )
                FirebaseFirestore.getInstance().collection("users").document(currentUser).update(
                    "friends", FieldValue.arrayRemove(userObjFriend)
                )
            }
        }

    }



}
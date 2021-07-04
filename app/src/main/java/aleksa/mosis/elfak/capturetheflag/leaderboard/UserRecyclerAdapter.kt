package aleksa.mosis.elfak.capturetheflag.leaderboard

import aleksa.mosis.elfak.capturetheflag.R
import aleksa.mosis.elfak.capturetheflag.data.User
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_user_list_item.view.*


class UserRecyclerAdapter(private val listener: LeaderboardActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    private var items: List<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_user_list_item, parent, false)
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

        val userImage = itemView.user_image
        val userUsername = itemView.txt_username
        val userFlags = itemView.txt_flags

        fun bind(user: User){
            userUsername.setText(user.username)
            userFlags.setText("Flags: " +  user.flags.toString())


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


}
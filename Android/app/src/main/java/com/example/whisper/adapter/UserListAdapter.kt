package com.example.whisper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.whisper.R
import com.example.whisper.UserInfoActivity

data class userData(
    val userId : String,
    val userName : String,
    val followCnt : String,
    val followerCnt : String,
    )
class UserListAdapter(private val dataset: MutableList<userData>) : RecyclerView.Adapter<UserListAdapter.ViewHolder>(){

    //private var userList: List<userData> = emptyList()
    inner class ViewHolder(item :View) : RecyclerView.ViewHolder(item){
        val userNameText : TextView = item.findViewById(R.id.userNameText)
        val followCnt : TextView = item.findViewById(R.id.followCntText)
        val followerCnt : TextView = item.findViewById(R.id.followerCntText)
        val userImage : ImageView = item.findViewById(R.id.userImage)
        val context : Context = item.context
        init {
            userImage.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    val intent = Intent(context, UserInfoActivity::class.java)
                    // 対象行のユーザIDを取得してインテントにセット
                    val userId = dataset[position].userId
                    intent.putExtra("userId",userId)
                    context.startActivity(intent)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_user,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.userNameText.text = dataset[position].userName
        holder.followCnt.text = dataset[position].followCnt
        holder.followerCnt.text = dataset[position].followerCnt


    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}
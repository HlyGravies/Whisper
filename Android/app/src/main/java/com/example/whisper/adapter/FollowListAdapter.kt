package com.example.whisper.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.R
import com.example.whisper.fragment.ProfileFragment
import com.example.whisper.model.User

class FollowListAdapter(private val activity: Activity, private val dataset: MutableList<User>) : RecyclerView.Adapter<FollowListAdapter.ViewHolder>(){

    private val myApp = activity.application as MyApplication
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
                    val intent = Intent(context, ProfileFragment::class.java)
                    val userId = dataset[position].userId
                    intent.putExtra("userId",userId)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_follow, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.userNameText.text = dataset[position].userName
        holder.followCnt.text = dataset[position].followCount.toString()
        holder.followerCnt.text = dataset[position].followerCount.toString()
        Glide.with(holder.userImage.context)
            .load(myApp.iconPath) // URL của hình ảnh
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.userImage)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
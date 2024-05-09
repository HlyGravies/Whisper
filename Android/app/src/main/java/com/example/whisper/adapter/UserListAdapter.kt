package com.example.whisper.adapter

import android.service.autofill.Dataset
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class userData(
    val userName : String,
    val followCnt : String,
    val followerCnt : String,
    )
class UserListAdapter(private val dataset: MutableList<userData>) : RecyclerView.Adapter<UserListAdapter.ViewHolder>(){

    private var userList: List<userData> = emptyList()
    class ViewHolder(item :View) : RecyclerView.ViewHolder(item){
        val userNameText : TextView = itemView.findViewById()
        val followCnt : TextView
        val followerCnt : TextView
        val  : ImageView


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.)
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}
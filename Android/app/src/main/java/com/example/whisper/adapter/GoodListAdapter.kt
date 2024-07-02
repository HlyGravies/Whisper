package com.example.whisper

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
import com.example.whisper.fragment.ProfileFragment
import com.example.whisper.model.Good
import com.example.whisper.model.Whisper

class GoodListAdapter(private val activity: Activity,
                      private val dataset: List<Good>,
) : RecyclerView.Adapter<GoodListAdapter.GoodViewHolder>() {


    private val myApp = activity.application as MyApplication
    inner class GoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodCount: TextView = itemView.findViewById(R.id.goodCntText)
        val context : Context = itemView.context

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

    // 2. When creating ViewHolder
    // 2-1. Set the screen design of the good row information.
    // 2-2. Set the set screen design as the return value.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_good, parent, false)
        return GoodViewHolder(view)
    }

    // 3. When binding ViewHolder
    // 3-1. Set the data of the target row to the objects of the ViewHolder.
    override fun onBindViewHolder(holder: GoodViewHolder, position: Int) {
        val whisper = dataset[position]

        holder.userName.text = whisper.userName
        holder.whisperText.text = whisper.content
        holder.goodCount.text = whisper.goodCount.toString()
        Glide.with(holder.userImage.context)
            .load(myApp.apiUrl + whisper.iconPath)
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.userImage)

        

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
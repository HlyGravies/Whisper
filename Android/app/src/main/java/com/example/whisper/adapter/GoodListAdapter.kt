package com.example.whisper

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper.model.Whisper

class GoodListAdapter(
    private val context: Context,
    private val whispers: List<Whisper>,
    private val loginUserId: String
) : RecyclerView.Adapter<GoodListAdapter.GoodViewHolder>() {

    inner class GoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userName: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_whisper, parent, false)
        return GoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoodViewHolder, position: Int) {
        val whisper = whispers[position]

        holder.userName.text = whisper.userName
        holder.whisperText.text = whisper.content
        holder.goodImage.setImageResource(if (whisper.goodFlg) R.drawable.ic_star_filled else R.drawable.ic_star_placeholder)

        // Set click listener for userImage
        holder.userImage.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                putExtra("userId", whisper.userId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return whispers.size
    }
}
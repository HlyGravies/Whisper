package com.example.whisper.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.R
import com.example.whisper.UserInfoActivity
import com.example.whisper.databinding.RecyclerFollowBinding
import com.example.whisper.model.User
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class FollowListAdapter @Inject constructor(
    private val activity: Activity,
    private val dataset: MutableList<User>,
    private val apiUrl: String
) : RecyclerView.Adapter<FollowListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: RecyclerFollowBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        init {
            binding.userImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val intent = Intent(context, UserInfoActivity::class.java)
                    val userId = dataset[position].userId
                    intent.putExtra("userId", userId)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerFollowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = dataset[position]
        holder.binding.userNameText.text = user.userName
        holder.binding.followCntText.text = user.followCount.toString()
        holder.binding.followerCntText.text = user.followerCount.toString()
        Glide.with(holder.binding.userImage.context)
            .load(apiUrl + user.iconPath) // URL của hình ảnh
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
            .skipMemoryCache(true)
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.binding.userImage)
        holder.binding.userImage.setOnClickListener {
            val intent = Intent(activity, UserInfoActivity::class.java).apply {
                putExtra("userId", user.userId)
            }
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}

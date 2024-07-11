package com.example.whisper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.R
import com.example.whisper.UserInfoActivity
import com.example.whisper.databinding.RecycleGoodBinding
import com.example.whisper.model.Good
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class GoodListAdapter @Inject constructor(
    @ActivityContext private val context: Context,
    private val dataset: List<Good>,
    private val apiUrl: String
) : RecyclerView.Adapter<GoodListAdapter.GoodViewHolder>() {

    inner class GoodViewHolder(val binding: RecycleGoodBinding) : RecyclerView.ViewHolder(binding.root) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodViewHolder {
        val binding = RecycleGoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoodViewHolder, position: Int) {
        val whisper = dataset[position]

        holder.binding.userNameText.text = whisper.userName
        holder.binding.whisperText.text = whisper.content
        holder.binding.goodCntText.text = whisper.goodCount.toString()
        Glide.with(holder.binding.userImage.context)
            .load(apiUrl + whisper.iconPath)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
            .skipMemoryCache(true)
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.binding.userImage)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}

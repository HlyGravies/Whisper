package com.example.whisper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.whisper.MyApplication.MyApplication
import com.example.whisper.databinding.RecycleGoodBinding
import com.example.whisper.fragment.ProfileFragment
import com.example.whisper.model.Good

class GoodListAdapter(
    private val activity: Activity,
    private val dataset: List<Good>
) : RecyclerView.Adapter<GoodListAdapter.GoodViewHolder>() {

    private val myApp = activity.application as MyApplication

    inner class GoodViewHolder(val binding: RecycleGoodBinding) : RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        init {
            binding.userImage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val intent = Intent(context, ProfileFragment::class.java)
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
            .load(myApp.apiUrl + whisper.iconPath)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable cache
            .skipMemoryCache(true)
            .placeholder(R.drawable.loading)
            .error(R.drawable.avatar)
            .into(holder.binding.userImage)
        holder.binding.userImage.setOnClickListener {
            val intent = Intent(activity, UserInfoActivity::class.java).apply {
                putExtra("userId", whisper.userId)
            }
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}

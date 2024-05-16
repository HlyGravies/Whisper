package com.example.whisper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whisper.R
import com.example.whisper.UserInfoActivity

class GoodListAdapter(private val data: List<GoodRowData>, private val context: Context) : RecyclerView.Adapter<GoodListAdapter.ViewHolder>() {

    // ネストされたデータクラス
    data class GoodRowData(
        val userId: String,  // ユーザID追加
        val userImage: String,
        val userName: String,
        val whisperText: String,
        val goodCnt: Int
    )

    // ビューホルダー定義
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImageView: ImageView = view.findViewById(R.id.userImage)
        val userNameTextView: TextView = view.findViewById(R.id.userNameText)
        val whisperTextView: TextView = view.findViewById(R.id.whisperText)
        val goodCntTextView: TextView = view.findViewById(R.id.goodCntText)
    }

    // ビューホルダー生成
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_good, parent, false)
        return ViewHolder(view)
    }

    // ビューホルダーバインド時の処理
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.userNameTextView.text = item.userName
        holder.whisperTextView.text = item.whisperText
        holder.goodCntTextView.text = "Likes: ${item.goodCnt}"
        // イメージビューのクリックイベント設定
        holder.userImageView.setOnClickListener {
            // インテントの生成とユーザ情報画面への遷移
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                putExtra("userId", item.userId) // ユーザIDをインテントにセット
            }
            context.startActivity(intent)
        }
        // 画像の読み込み（ここではGlideを想定しています）
        Glide.with(context).load(item.userImage).into(holder.userImageView)
    }

    // 行数取得時の処理
    override fun getItemCount(): Int = data.size
}

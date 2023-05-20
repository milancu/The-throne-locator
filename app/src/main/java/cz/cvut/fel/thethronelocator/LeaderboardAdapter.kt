package cz.cvut.fel.thethronelocator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import cz.cvut.fel.thethronelocator.model.User
import cz.cvut.fel.thethronelocator.repository.UserRepository

class LeaderboardAdapter(private val userList: List<User>?) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    private val userRepository = UserRepository()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.leaderboard_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList?.size ?: 0
    }

    override fun onBindViewHolder(holder: LeaderboardAdapter.ViewHolder, position: Int) {
        val userItem = userList?.get(position)
        userItem?.let { user ->
            holder.bind(user, position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User, position: Int) {
            userRepository.getUserById(user.id!!) { user ->
                itemView.findViewById<TextView>(R.id.rank).text = "#${position + 1}"
                itemView.findViewById<TextView>(R.id.user_leaderboard_fullName).text = user!!.name
                itemView.findViewById<TextView>(R.id.user_record).text = user.record.toString()
                val cardView: MaterialCardView = itemView.findViewById(R.id.card)
                val imageView: ImageView =
                    itemView.findViewById<ImageView>(R.id.rating_image_view)
                if (user.imgLink != null) {
                    Glide.with(this.itemView)
                        .load(user.imgLink)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(imageView)
                } else {
                    Glide.with(this.itemView)
                        .load(R.drawable.avatar)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(imageView)
                }
                if (position == 0) {
                    cardView.strokeWidth = 8
                    cardView.strokeColor =
                        ContextCompat.getColor(itemView.context, R.color.purple_700)
                }
                if (position == 1) {
                    cardView.strokeWidth = 4
                    cardView.strokeColor =
                        ContextCompat.getColor(itemView.context, R.color.purple_500)
                }
                if (position == 2) {
                    cardView.strokeWidth = 4
                    cardView.strokeColor =
                        ContextCompat.getColor(itemView.context, R.color.purple_200)
                }
            }
        }
    }
}
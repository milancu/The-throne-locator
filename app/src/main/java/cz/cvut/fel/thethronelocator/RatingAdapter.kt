package cz.cvut.fel.thethronelocator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import cz.cvut.fel.thethronelocator.model.Rating
import cz.cvut.fel.thethronelocator.repository.UserRepository

class RatingAdapter(private val ratingList: List<Rating>?) :
    RecyclerView.Adapter<RatingAdapter.ViewHolder>() {
    private val userRepository = UserRepository()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rating_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ratingItem = ratingList?.get(position)
        ratingItem?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return ratingList?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(ratingItem: Rating) {
            var fullName = "Anonymous"

            if (ratingItem.authorId != null) {
                userRepository.getUserById(ratingItem.authorId) { user ->
                    if (user != null) {
                        fullName = user.name ?: "Anonymous"
                        itemView.findViewById<TextView>(R.id.user_fullName).text = fullName
                        itemView.findViewById<RatingBar>(R.id.rating_detail_ListView).rating =
                            ratingItem.rating!!

                        val imageView: ImageView =
                            itemView.findViewById<ImageView>(R.id.rating_image_view)
                        Glide.with(this.itemView)
                            .load(user.imgLink)
                            .apply(RequestOptions.bitmapTransform(CircleCrop()))
                            .into(imageView)
                    } else {
                        itemView.findViewById<TextView>(R.id.user_fullName).text = fullName
                        itemView.findViewById<RatingBar>(R.id.rating_detail_ListView).rating =
                            ratingItem.rating!!
                    }
                }
            } else {
                itemView.findViewById<TextView>(R.id.user_fullName).text = fullName
                itemView.findViewById<RatingBar>(R.id.rating_detail_ListView).rating =
                    ratingItem.rating!!
            }
        }
    }
}
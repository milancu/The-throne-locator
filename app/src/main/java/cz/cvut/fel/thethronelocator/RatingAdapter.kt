package cz.cvut.fel.thethronelocator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.cvut.fel.thethronelocator.model.Rating

class RatingAdapter(private val ratingList: List<Rating>) :
    RecyclerView.Adapter<RatingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rating_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ratingItem = ratingList[position]
        holder.bind(ratingItem)
    }

    override fun getItemCount(): Int {
        return ratingList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(ratingItem: Rating) {
            itemView.findViewById<TextView>(R.id.user_fullName).text =
                ratingItem.author.firstName.toString()
            itemView.findViewById<RatingBar>(R.id.rating_detail_ListView).rating = ratingItem.rating
        }
    }
}
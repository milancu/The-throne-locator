package cz.cvut.fel.thethronelocator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import cz.cvut.fel.thethronelocator.model.Toilet

class ToiletListAdapter(private val toiletList: List<Toilet>?) :
    RecyclerView.Adapter<ToiletListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToiletListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.toilet_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToiletListAdapter.ViewHolder, position: Int) {
        val toiletItem = toiletList?.get(position)
        toiletItem.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return toiletList?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tempText: TextView = itemView.findViewById(R.id.toiletName)
        private val description: TextView = itemView.findViewById(R.id.toiletDistance)
        private val imageView: ImageView = itemView.findViewById(R.id.image_toilet_view)
        private val button: MaterialButton = itemView.findViewById(R.id.button_open_detail)
        private val navigateButton: MaterialButton =
            itemView.findViewById(R.id.button_navigate_toilet_list)

        fun bind(toilet: Toilet?) {
            if (toilet != null) {
                tempText.text = toilet.name
                description.text = toilet.openingTime
                Glide.with(this.itemView)
                    .load(toilet.img)
                    .apply(RequestOptions().centerCrop())
                    .into(imageView)
                button.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("toiletId", toilet.id)
                        putString("name", toilet.name)
                        putFloat("latitude", toilet.position.latitude.toFloat())
                        putFloat("longitude", toilet.position.longitude.toFloat())
                    }
                    val navController = Navigation.findNavController(itemView)
                    navController.navigate(R.id.action_to_detail, bundle)
                }
                navigateButton.setOnClickListener {
                    openMapWithLocation(toilet.latitude!!, toilet.longitude!!)
                }
            }
        }

        private fun openMapWithLocation(latitude: Double, longitude: Double) {
            val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(itemView.context.packageManager) != null) {
                itemView.context.startActivity(mapIntent)
            } else {
                Toast.makeText(
                    itemView.context,
                    "Map application is not installed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
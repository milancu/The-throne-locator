package cz.cvut.fel.thethronelocator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import cz.cvut.fel.thethronelocator.repository.ToiletRepository
import cz.cvut.fel.thethronelocator.utils.SnackBarUtils

class FavouritesListAdapter(private val favouriteList: List<String>?) :
    RecyclerView.Adapter<FavouritesListAdapter.ViewHolder>() {
    private val toiletRepository = ToiletRepository()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): FavouritesListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favourites_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favouriteList?.size ?: 0
    }

    override fun onBindViewHolder(holder: FavouritesListAdapter.ViewHolder, position: Int) {
        val userItem = favouriteList?.get(position)
        userItem?.let {
            holder.bind(it)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(toiletId: String) {
            val card = itemView.findViewById<MaterialCardView>(R.id.favouritesCard)
            val toiletName = itemView.findViewById<TextView>(R.id.toilet_name_favourites)
            val navigateButton =
                itemView.findViewById<ExtendedFloatingActionButton>(R.id.navigate_button)

            toiletRepository.getToiletById(id = toiletId, onSuccess = { toilet ->
                toiletName.text = toilet.name
                navigateButton.setOnClickListener {
                    openMapWithLocation(toilet.latitude!!, toilet.longitude!!)
                }
                card.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("toiletId", toilet.id)
                        putString("name", toilet.name)
                        putFloat("latitude", toilet.position.latitude.toFloat())
                        putFloat("longitude", toilet.position.longitude.toFloat())
                    }
                    val navController = Navigation.findNavController(itemView)
                    navController.navigate(R.id.action_to_detail, bundle)
                }
            }, onError = {
            })
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
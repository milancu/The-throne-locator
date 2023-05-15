package cz.cvut.fel.thethronelocator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.cvut.fel.thethronelocator.model.Toilet

class ToiletListviewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tempText: TextView = view.findViewById<TextView>(R.id.tempText)
    private val description: TextView = view.findViewById<TextView>(R.id.textView3)

    fun bind(toilet: Toilet) {
        tempText.text = toilet.name
        description.text = toilet.address
    }
}

class ToiletListAdapter() : ListAdapter<Toilet, ToiletListviewHolder>(DIFF_CONFIG) {

    companion object {
        val DIFF_CONFIG = object : DiffUtil.ItemCallback<Toilet>() {
            override fun areItemsTheSame(oldItem: Toilet, newItem: Toilet): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Toilet, newItem: Toilet): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToiletListviewHolder {
        val toiletView =
            LayoutInflater.from(parent.context).inflate(R.layout.toilet_list_view, parent, false)
        return ToiletListviewHolder(toiletView)
    }

    override fun onBindViewHolder(holder: ToiletListviewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
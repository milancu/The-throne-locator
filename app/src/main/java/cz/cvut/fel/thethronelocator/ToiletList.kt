package cz.cvut.fel.thethronelocator

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cz.cvut.fel.thethronelocator.model.Toilet
import cz.cvut.fel.thethronelocator.repository.ToiletRepository

class ToiletList : BaseActivity() {
    private val toiletRepository = ToiletRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.toilet_list)
        toiletRepository.loadToilets()

        val toiletList: RecyclerView = findViewById(R.id.toiletList)
        toiletList.layoutManager = LinearLayoutManager(this)
        val toiletListAdapter = ToiletListAdapter()
        toiletList.adapter = toiletListAdapter

        val allToiletsObserver = Observer<List<Toilet>> {
            toiletListAdapter.submitList(it)
        }
        toiletRepository.allToilet.observe(this, allToiletsObserver)
    }
}
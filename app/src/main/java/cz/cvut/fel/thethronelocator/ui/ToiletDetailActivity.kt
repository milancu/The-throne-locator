package cz.cvut.fel.thethronelocator.ui

import android.app.Activity
import android.os.Bundle
import cz.cvut.fel.thethronelocator.databinding.DetailBinding

class ToiletDetailActivity : Activity() {
    private val binding: DetailBinding by lazy { DetailBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val toiletId = intent.getIntExtra("toiletId", -1)
    }
}
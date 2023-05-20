package cz.cvut.fel.thethronelocator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cz.cvut.fel.thethronelocator.LeaderboardAdapter
import cz.cvut.fel.thethronelocator.R
import cz.cvut.fel.thethronelocator.databinding.FragmentLeaderboardBinding
import cz.cvut.fel.thethronelocator.repository.UserRepository

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {
    private val userRepository = UserRepository()
    private lateinit var binding: FragmentLeaderboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return // Return if fragment is not attached

        userRepository.getAllUsers {
            val leaderboardAdapter = LeaderboardAdapter(it?.sortedByDescending { it.record })
            val recyclerView = binding.leaderboardList
            recyclerView.adapter = leaderboardAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }
}

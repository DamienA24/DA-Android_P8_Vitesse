package com.quizocr.vitesse.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupFloatingActionButton()
        setupSearchField()
        viewModel.fetchAllCandidatesIfNeeded()
    }

    private fun setupViewPager() {
        val adapter = CandidatePagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.all_candidates_tab)
                1 -> getString(R.string.favorite_candidates_tab)
                else -> ""
            }
        }.attach()
    }

    private fun setupFloatingActionButton() {
        binding.addCandidateFab.setOnClickListener {
            // TODO: Navigation vers l'ajout d'un nouveau candidat
            // findNavController().navigate(R.id.action_home_to_add_candidate)
        }
    }

    private fun setupSearchField() {
        binding.inputSeachCandidate.setEndIconOnClickListener {
            val searchQuery = binding.inputSeachCandidate.editText?.text?.toString() ?: ""
            Log.d("HomeFragment", "Search query: $searchQuery")
            viewModel.setSearchQuery(searchQuery)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class CandidatePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CandidateFragment.newInstance(showFavoritesOnly = false)
                1 -> CandidateFragment.newInstance(showFavoritesOnly = true)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
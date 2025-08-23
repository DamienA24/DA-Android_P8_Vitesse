package com.quizocr.vitesse.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quizocr.vitesse.databinding.FragmentCandidateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CandidateFragment : Fragment() {

    private var _binding: FragmentCandidateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private val candidateAdapter = CandidateAdapter(
        candidates = emptyList(),
        onCandidateClick = { candidate ->
            // TODO: Navigation vers les détails du candidat
            // findNavController().navigate(...)
        },
        onFavoriteClick = { candidate ->
            // TODO: Mettre à jour le statut favori du candidat
        }
    )

    private var showFavoritesOnly: Boolean = false

    companion object {
        private const val ARG_SHOW_FAVORITES_ONLY = "show_favorites_only"

        fun newInstance(showFavoritesOnly: Boolean): CandidateFragment {
            return CandidateFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_FAVORITES_ONLY, showFavoritesOnly)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showFavoritesOnly = arguments?.getBoolean(ARG_SHOW_FAVORITES_ONLY, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCandidateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()

        if (viewModel.allCandidates.value.isEmpty()) {
            viewModel.fetchAllCandidates()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCandidates.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCandidates.adapter = candidateAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCandidates.collect { allCandidates ->
                val filteredCandidates = if (showFavoritesOnly) {
                    allCandidates.filter { it.isFavorite }
                } else {
                    allCandidates
                }

                candidateAdapter.updateData(filteredCandidates)
            }
        }

        // Observer le loading state
        viewLifecycleOwner.lifecycleScope.launch {
           // viewModel.isLoading.collect { isLoading ->
             //   binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.quizocr.vitesse.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.quizocr.vitesse.databinding.FragmentCandidateBinding
import com.quizocr.vitesse.domain.model.CandidateSummary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CandidateFragment : Fragment() {

    private var _binding: FragmentCandidateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()
    private val candidateAdapter = CandidateAdapter(
        candidates = emptyList(),
        onCandidateClick = { candidateSummary ->
            viewModel.onCandidateSelected(candidateSummary)
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
        observeUiState()

    }

    private fun setupRecyclerView() {
        binding.recyclerViewCandidates.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCandidates.adapter = candidateAdapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Log.d("CandidateFragment_OBSERVE", "Received state: isLoading=${state.isLoading}, candidates.size=${state.candidates.size}, query='${state.searchQuery}'")
                    binding.progressBar.isVisible = state.isLoading

                    val candidatesForThisTab = if (showFavoritesOnly) {
                        state.candidates.filter { it.isFavorite }
                    } else {
                        state.candidates
                    }
                    candidateAdapter.updateData(candidatesForThisTab)

                    if (!state.isLoading) {
                        Log.d("CandidateFragment L", "Updating adapter with ${candidatesForThisTab.size} candidates")
                        if (candidatesForThisTab.isEmpty()) {
                            binding.textViewEmptyState.isVisible = true
                            binding.recyclerViewCandidates.isVisible = false
                        } else {
                            binding.textViewEmptyState.isVisible = false
                            binding.recyclerViewCandidates.isVisible = true
                        }
                    } else {
                        Log.d("CandidateFragment L", "Hiding adapter ${candidatesForThisTab.size} ")
                        binding.recyclerViewCandidates.isVisible = false
                        binding.textViewEmptyState.isVisible = true
                    }
                    state.errorMessage?.let { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        viewModel.clearErrorMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


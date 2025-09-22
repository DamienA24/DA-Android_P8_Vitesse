package com.quizocr.vitesse.ui.addEditCandidate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentAddEditCandidateBinding
import com.quizocr.vitesse.databinding.FragmentResumeCandidateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditCandidateFragment : Fragment() {

    private val viewModel: AddEditCandidateViewModel by viewModels()
    private val args: AddEditCandidateFragmentArgs by navArgs()

    private var _binding: FragmentAddEditCandidateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditCandidateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()


        // TODO: Observer les données du ViewModel pour afficher les informations du candidat
        // TODO: Configurer les listeners pour la sauvegarde du formulaire
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = args.screenTitle
    }

}

package com.quizocr.vitesse.ui.addEditCandidate

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentAddEditCandidateBinding
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.utils.formatDateShort
import com.quizocr.vitesse.utils.formatSalaryLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditCandidateFragment : Fragment() {

    private val viewModel: AddEditCandidateViewModel by viewModels()

    private var _binding: FragmentAddEditCandidateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditCandidateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupObservers()
        // TODO: Configurer les listeners pour la sauvegarde du formulaire
    }

    private fun setupToolbar() = with(binding.toolbar) {
        setNavigationOnClickListener { findNavController().navigateUp() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateLoadingState(uiState.isLoadingCandidateData)

                    binding.toolbar.title = uiState.screenTitle

                    uiState.candidate?.let {
                        renderCandidateDetails(it)
                    } ?: run {
                        if (!uiState.isLoadingCandidateData) {
                            clearForm()
                            renderPhoto(null)
                        }
                    }

                    uiState.errorMessage?.let {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        viewModel.clearErrorMessage()
                    }
                }
            }
        }
    }

    private fun updateLoadingState(isViewModelLoadingData: Boolean) {
        binding.progressBarLoading.isVisible = isViewModelLoadingData
        binding.addEditCandidate.isVisible = !isViewModelLoadingData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun renderCandidateDetails(candidate: Candidate) {
        binding.editTextFirstName.setText(candidate.firstName)
        binding.editTextLastName.setText(candidate.lastName)
        binding.editPhone.setText(candidate.phoneNumber)
        binding.editTextEmail.setText(candidate.email)
        binding.editTextInfo.setText(candidate.notes)
        binding.editTextBirthday.setText(formatDateShort(candidate.dateOfBirth))
        binding.editSalary.setText(formatSalaryLocale(candidate.salaryEuros))

        renderPhoto(candidate.photoUri)
    }

    private fun renderPhoto(photoUri: String?) {
        if (!photoUri.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.ic_android_black_24dp)
                .into(binding.candidateImage)
        } else {
            binding.candidateImage.setImageResource(R.drawable.ic_android_black_24dp)
        }
    }

    private fun clearForm(){
        binding.editTextFirstName.setText("")
        binding.editTextLastName.setText("")
        binding.editPhone.setText("")
        binding.editTextEmail.setText("")
        binding.editTextInfo.setText("")
        binding.editTextBirthday.setText("")
        binding.editSalary.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

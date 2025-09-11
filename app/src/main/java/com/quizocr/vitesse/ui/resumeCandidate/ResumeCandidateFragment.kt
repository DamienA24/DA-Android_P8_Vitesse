package com.quizocr.vitesse.ui.resumeCandidate

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentResumeCandidateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@AndroidEntryPoint
class ResumeCandidateFragment : Fragment() {

    private val viewModel: ResumeCandidateViewModel by viewModels()

    private var _binding: FragmentResumeCandidateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumeCandidateBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (uiState.errorMessage != null) {
                        Toast.makeText(requireContext(), uiState.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    uiState.candidate?.let { candidate ->
                        binding.topAppBar.title = "${candidate.firstName} ${candidate.lastName}"
                        binding.candidateNotes.text = candidate.notes ?: "N/A"

                        val formatSalary = NumberFormat.getNumberInstance(Locale.getDefault())
                        binding.candidateSalary.text = "${formatSalary.format(candidate.salaryEuros)} €"

                        val age = calculateAge(candidate.dateOfBirth)
                        val formattedDateOfBirth = formateDateBirthday(candidate.dateOfBirth)
                        val ageSuffix = getString(R.string.about_age)
                        binding.candidateDateBirthday.text = "$formattedDateOfBirth ($age $ageSuffix)"

                        if (!candidate.photoUri.isNullOrBlank()) {
                            Glide.with(this@ResumeCandidateFragment)
                                .load(candidate.photoUri)
                                .placeholder(R.drawable.ic_android_black_24dp)
                                .error(R.drawable.ic_android_black_24dp)
                                .into(binding.candidateImage)
                        } else {
                            binding.candidateImage.setImageResource(R.drawable.ic_android_black_24dp)
                        }
                    } ?: run {

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formateDateBirthday(dateOfBirthLocalDate: LocalDate): String? {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val today = LocalDate.now()
        val age = ChronoUnit.YEARS.between(dateOfBirthLocalDate, today)
        return dateOfBirthLocalDate.format(dateFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAge(dateOfBirthLocalDate: LocalDate): Long {
        val today = LocalDate.now()
        return ChronoUnit.YEARS.between(dateOfBirthLocalDate, today)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

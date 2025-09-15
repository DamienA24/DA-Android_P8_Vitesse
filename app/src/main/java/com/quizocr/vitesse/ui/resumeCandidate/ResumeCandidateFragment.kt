package com.quizocr.vitesse.ui.resumeCandidate

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.quizocr.vitesse.databinding.FragmentResumeCandidateBinding
import com.quizocr.vitesse.utils.calculateAgeInYears
import com.quizocr.vitesse.utils.formatDateShort
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider

@AndroidEntryPoint
class ResumeCandidateFragment : Fragment() {

    private val viewModel: ResumeCandidateViewModel by viewModels()

    private var _binding: FragmentResumeCandidateBinding? = null
    private val binding get() = _binding!!

    private var starMenuItem: MenuItem? = null

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
        setupFragmentToolbar()
    }

    private fun setupFragmentToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            Log.d("FragmentToolbar", "Item cliqué : ${menuItem.title}, ID: ${menuItem.itemId}")
            when (menuItem.itemId) {
                R.id.action_star -> {
                    Log.d("FragmentToolbar", "ACTION_STAR cliqué")
                    viewModel.toggleFavoriteStatus()
                    true
                }
                R.id.action_edit -> {
                    Toast.makeText(requireContext(), "Edit clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_delete -> {
                    Toast.makeText(requireContext(), "Delete clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateStarIcon(isFavorite: Boolean) {
        if (isFavorite) {
            starMenuItem?.setIcon(R.drawable.baseline_star_24)
        } else {
            starMenuItem?.setIcon(R.drawable.outline_star_24)
        }
    }

    private fun setupClickListeners() {

        binding.callActionLayout.setOnClickListener {
            viewModel.uiState.value.candidate?.phoneNumber?.let { number ->
                if (number.isNotBlank()) {
                    initiatePhoneCall(number)
                } else {
                    Toast.makeText(requireContext(), R.string.error_phone, Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(requireContext(), R.string.miss_data_candidate, Toast.LENGTH_SHORT).show()
        }

        binding.smsActionLayout.setOnClickListener {
            viewModel.uiState.value.candidate?.phoneNumber?.let { number ->
                if (number.isNotBlank()) {
                    initiateSms(number)
                } else {
                    Toast.makeText(requireContext(), R.string.error_phone, Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(requireContext(), R.string.miss_data_candidate, Toast.LENGTH_SHORT).show()
        }

        binding.emailActionLayout.setOnClickListener {
            viewModel.uiState.value.candidate?.email?.let { emailAddress ->
                if (emailAddress.isNotBlank()) {
                    initiateEmail(emailAddress)
                } else {
                    Toast.makeText(requireContext(), R.string.error_mail, Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(requireContext(), R.string.miss_data_candidate, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    binding.loadingProgressBar.isVisible = uiState.isLoading
                    binding.contentScrollView.isVisible = !uiState.isLoading
                    starMenuItem = binding.topAppBar.menu.findItem(R.id.action_star)
                    if (uiState.errorMessage != null) {
                        Toast.makeText(requireContext(), uiState.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    uiState.candidate?.let { candidate ->
                        binding.topAppBar.title = "${candidate.firstName} ${candidate.lastName}"
                        binding.candidateNotes.text = candidate.notes ?: "N/A"

                        val formatSalary = NumberFormat.getNumberInstance(Locale.getDefault())
                        binding.candidateSalary.text = "${formatSalary.format(candidate.salaryEuros)} €"

                        val poundsSuffix = getString(R.string.expected_salary_pounds)
                        binding.candidateSalaryPounds.text = "$poundsSuffix ${uiState.formattedSalaryPounds}"

                        val age = calculateAgeInYears(candidate.dateOfBirth)
                        val formattedDateOfBirth = formatDateShort(candidate.dateOfBirth)
                        val ageSuffix = getString(R.string.about_age)
                        binding.candidateDateBirthday.text = "$formattedDateOfBirth ($age $ageSuffix)"
                        updateStarIcon(candidate.isFavorite)
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

    private fun initiatePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phoneNumber".toUri()
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_call_application, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initiateSms(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "sms to:$phoneNumber".toUri()
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_sms_application, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initiateEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        }
        try {
            val value = getString(R.string.send_email)
            startActivity(Intent.createChooser(intent, value))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_email_application, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.quizocr.vitesse.ui.resumeCandidate

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentResumeCandidateBinding
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.ui.home.HomeFragmentDirections
import com.quizocr.vitesse.utils.calculateAgeInYears
import com.quizocr.vitesse.utils.formatDateShort
import com.quizocr.vitesse.utils.formatSalaryLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

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
        setupToolbar()
    }

    // -------- Toolbar --------
    private fun setupToolbar() = with(binding.topAppBar) {
        setNavigationOnClickListener { findNavController().navigateUp() }
        setOnMenuItemClickListener { handleToolbarAction(it.itemId) }
    }

    private fun handleToolbarAction(itemId: Int): Boolean =
        when (itemId) {
            R.id.action_star -> {
                viewModel.toggleFavoriteStatus()
                true
            }
            R.id.action_edit -> {
                //
                navigateToEditScreen()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> false
        }

    private fun updateStarIcon(isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.baseline_star_24 else R.drawable.outline_star_24
        starMenuItem?.setIcon(iconRes)
    }

    private fun setupClickListeners() = with(binding) {
        callActionLayout.setCandidateClick(
            { it.phoneNumber },
            R.string.error_phone,
            ::initiatePhoneCall
        )
        smsActionLayout.setCandidateClick(
            { it.phoneNumber },
            R.string.error_phone,
            ::initiateSms
        )
        emailActionLayout.setCandidateClick(
            { it.email },
            R.string.error_mail,
            ::initiateEmail
        )
    }

    private fun View.setCandidateClick(
        valueProvider: (Candidate) -> String?,
        errorRes: Int,
        action: (String) -> Unit
    ) {
        setOnClickListener {
            val candidate = viewModel.uiState.value.candidate
            val value = candidate?.let(valueProvider)
            if (!value.isNullOrBlank()) {
                action(value)
            } else {
                Toast.makeText(requireContext(), errorRes, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // -------- Observers / UI --------
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    handleLoading(uiState)
                    handleErrors(uiState)
                    handleNavigation(uiState)

                    uiState.candidate?.let { renderCandidate(it, uiState.formattedSalaryPounds) }
                }
            }
        }
    }

    private fun handleLoading(uiState: ResumeCandidateUiState) {
        binding.loadingProgressBar.isVisible = uiState.isLoading || uiState.isDeleting
        binding.contentScrollView.isVisible = !uiState.isLoading
        starMenuItem = binding.topAppBar.menu.findItem(R.id.action_star)
    }

    private fun handleErrors(uiState: ResumeCandidateUiState) {
        uiState.errorMessage?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNavigation(uiState: ResumeCandidateUiState) {
        if (uiState.navigateBackAfterDeletion) {
            findNavController().navigateUp()
            viewModel.onNavigationDone()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun renderCandidate(candidate: Candidate, salaryPounds: String?) {
        binding.topAppBar.title = "${candidate.firstName} ${candidate.lastName}"
        binding.candidateNotes.text = candidate.notes ?: "N/A"

        val salaryFormat = formatSalaryLocale(candidate.salaryEuros)
        val salaryText = getString(R.string.expected_salary_pounds)
        binding.candidateSalary.text = "$salaryFormat €"

        binding.candidateSalaryPounds.text = "$salaryText ${salaryPounds?:"0"}"

        val age = calculateAgeInYears(candidate.dateOfBirth)
        val dob = formatDateShort(candidate.dateOfBirth)
        binding.candidateDateBirthday.text = "$dob ($age ${getString(R.string.about_age)})"

        updateStarIcon(candidate.isFavorite)
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

    private fun tryStartActivity(intent: Intent, errorRes: Int) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), errorRes, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initiatePhoneCall(phoneNumber: String) =
        tryStartActivity(Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri()), R.string.no_call_application)

    private fun initiateSms(phoneNumber: String) =
        tryStartActivity(Intent(Intent.ACTION_SENDTO, "smsto:$phoneNumber".toUri()), R.string.no_sms_application)

    private fun initiateEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }
        tryStartActivity(Intent.createChooser(intent, getString(R.string.send_email)), R.string.no_email_application)
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title_delete)
            .setMessage(R.string.dialog_text_delete)
            .setPositiveButton(R.string.dialog_button_confirm) { _, _ ->
                viewModel.deleteCandidate()
            }
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .show()
    }

    private fun navigateToEditScreen() {
        val action = ResumeCandidateFragmentDirections.actionResumeCandidateFragmentToAddEditCandidateFragment(
            screenTitle = getString(R.string.title_edit_candidate)
            , candidateId = viewModel.candidateId
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

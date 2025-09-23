package com.quizocr.vitesse.ui.addEditCandidate

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentAddEditCandidateBinding
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.utils.formatDateShort
import com.quizocr.vitesse.utils.formatSalaryLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZoneId
import kotlin.text.format

@AndroidEntryPoint
class AddEditCandidateFragment : Fragment() {

    private val viewModel: AddEditCandidateViewModel by viewModels()

    private var _binding: FragmentAddEditCandidateBinding? = null
    private val binding get() = _binding!!

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var selectedImageUri: Uri? = null
    private var selectedBirthdayInMillis: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupImagePickers()
    }

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
        setupClickListeners()
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
                        loadCandidateImage(it.photoUri)
                    } ?: run {
                        if (!uiState.isLoadingCandidateData) {
                            clearForm()
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

    private fun setupImagePickers() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                loadImageIntoView(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners()= with(binding) {
        cardImagePlaceholder.setOnClickListener {
            openImagePicker()
        }

        editTextBirthday.setOnClickListener {
            showBirthdayDatePicker()
        }
        // binding.btnSubmit.setOnClickListener { saveCandidateData() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showBirthdayDatePicker() {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.anniversary))

         viewModel.uiState.value.candidate?.dateOfBirth?.let { localDateDob ->
             val zonedDateTimeUtc = localDateDob.atStartOfDay(ZoneId.of("UTC"))
             val millisUtc = zonedDateTimeUtc.toInstant().toEpochMilli()
            datePickerBuilder.setSelection(millisUtc)
         }

        val datePicker = datePickerBuilder.build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedBirthdayInMillis = selection

            val outputDateFormat = SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            )
            val formattedDate = outputDateFormat.format(selection)
            binding.editTextBirthday.setText(formattedDate)
        }

        datePicker.show(parentFragmentManager, "BIRTHDAY_DATE_PICKER_TAG")
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

    }

    private fun loadCandidateImage(photoUri: String?) {
        if (selectedImageUri == null && !photoUri.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.ic_android_black_24dp)
                .into(binding.candidateImage)
        }
    }

    private fun loadImageIntoView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_android_black_24dp)
            .error(R.drawable.ic_android_black_24dp)
            .into(binding.candidateImage)
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
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

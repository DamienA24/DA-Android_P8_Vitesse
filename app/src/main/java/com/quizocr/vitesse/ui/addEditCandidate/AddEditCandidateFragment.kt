package com.quizocr.vitesse.ui.addEditCandidate

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.quizocr.vitesse.R
import com.quizocr.vitesse.databinding.FragmentAddEditCandidateBinding
import com.quizocr.vitesse.domain.model.Candidate
import com.quizocr.vitesse.utils.formatDateShort
import com.quizocr.vitesse.utils.formatSalaryLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.ExperimentalTime
import androidx.core.net.toUri
import androidx.navigation.NavOptions
import com.quizocr.vitesse.utils.parseFormattedSalaryString
import java.util.Locale

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
                    binding.btnSubmit.isClickable = !uiState.isSaving

                    if(uiState.saveSuccess) {
                        returnHome()
                        return@collect
                    }

                    if(!uiState.isSaving && !uiState.isLoadingCandidateData) {
                        if (uiState.candidate != null) {
                            renderCandidateDetails(uiState.candidate)
                            loadCandidateImage(uiState.candidate.photoUri)
                        } else {
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
        btnSubmit.setOnClickListener { validateAndSaveCandidate() }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showBirthdayDatePicker() {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.anniversary))

        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setValidator(DateValidatorPointBackward.now())
        datePickerBuilder.setCalendarConstraints(constraintsBuilder.build())


        selectedBirthdayInMillis?.let {
            datePickerBuilder.setSelection(it)
        } ?: run {
            viewModel.uiState.value.candidate?.dateOfBirth?.let { localDateDob ->
                val zonedDateTimeUtc = localDateDob.atStartOfDay(ZoneId.of("UTC"))
                val millisUtc = zonedDateTimeUtc.toInstant().toEpochMilli()
                datePickerBuilder.setSelection(millisUtc)
            }
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
        selectedBirthdayInMillis = candidate.dateOfBirth.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        binding.editSalary.setText(formatSalaryLocale(candidate.salaryEuros))

    }

    private fun loadCandidateImage(photoUri: String?) {
        if (selectedImageUri == null && !photoUri.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.ic_android_black_24dp)
                .into(binding.candidateImage)
            selectedImageUri = photoUri.toUri()
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

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalTime::class)
    private fun validateAndSaveCandidate() {
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()

        var isValid = true

        if (firstName.isEmpty()) {
            binding.textInputLayoutFirstName.error = getString(R.string.mandatory_field)
            isValid = false
        } else {
            binding.textInputLayoutFirstName.error = null
        }

        if (lastName.isEmpty()) {
            binding.textInputLayoutLastName.error = getString(R.string.mandatory_field)
            isValid = false
        } else {
            binding.textInputLayoutLastName.error = null
        }

        if (phone.isEmpty()) {
            binding.textInputLayoutPhone.error = getString(R.string.mandatory_field)
            isValid = false
        } else {
            binding.textInputLayoutPhone.error = null
        }

        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = getString(R.string.mandatory_field)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = getString(R.string.invalid_format)
            isValid = false
        } else {
            binding.textInputLayoutEmail.error = null
        }

        if (selectedBirthdayInMillis == null) {
            binding.textInputLayoutBirthday.error = getString(R.string.mandatory_field)
            isValid = false
        } else {
            binding.textInputLayoutBirthday.error = null
        }

        if (isValid) {
            val dateOfBirthToSave: LocalDate = java.time.Instant.ofEpochMilli(selectedBirthdayInMillis!!)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()

            val salaryString = binding.editSalary.text.toString()
            val salaryDouble = parseFormattedSalaryString(salaryString, Locale.getDefault())

            val notes = binding.editTextInfo.text.toString().trim()
            val photoUriString = selectedImageUri?.toString()

            viewModel.saveCandidate(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                email = email,
                dateOfBirth = dateOfBirthToSave,
                salary = salaryDouble?:0.0,
                notes = notes,
                photoUri = photoUriString?:""
            )

        }
    }

    private fun returnHome() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        try {
            findNavController().navigate(R.id.homeFragment, null, navOptions)
        } catch (e: IllegalArgumentException) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

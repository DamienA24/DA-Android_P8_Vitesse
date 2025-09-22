package com.quizocr.vitesse.ui.addEditCandidate

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quizocr.vitesse.R

class AddEditCandidateFragment : Fragment() {

    companion object {
        fun newInstance() = AddEditCandidateFragment()
    }

    private val viewModel: AddEditCandidateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_edit_candidate, container, false)
    }
}
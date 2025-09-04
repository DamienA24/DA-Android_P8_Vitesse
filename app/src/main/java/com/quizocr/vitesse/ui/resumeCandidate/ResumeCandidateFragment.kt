package com.quizocr.vitesse.ui.resumeCandidate

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quizocr.vitesse.R

class ResumeCandidateFragment : Fragment() {

    companion object {
        fun newInstance() = ResumeCandidateFragment()
    }

    private val viewModel: ResumeCandidateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_resume_candidate, container, false)
    }
}
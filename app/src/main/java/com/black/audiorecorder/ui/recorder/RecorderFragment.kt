package com.black.audiorecorder.ui.recorder

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.black.audiorecorder.R
import com.black.audiorecorder.databinding.FragmentRecorderBinding
import com.black.audiorecorder.ui.MainViewModel
import com.black.audiorecorder.utils.AudioRecorder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class RecorderFragment : Fragment(R.layout.fragment_recorder) {

    private val viewModel: RecorderViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentRecorderBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.stopRecording()
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRecorderBinding.bind(view)
        initViews()
        initObservers()
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.recorderFragment) {
                viewModel.stopRecording()
                viewModel.updateRecorderState(RecorderViewModel.RecorderState.RecordingStopped)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initFeatures()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.recorderState == RecorderViewModel.RecorderState.RecordingStarted) {
            viewModel.resumeRecording()
        }
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.recorderState == RecorderViewModel.RecorderState.RecordingStarted) {
            viewModel.pauseRecording()
        }
    }

    private fun initFeatures() {
        lifecycleScope.launch {
            if (viewModel.recorderState == RecorderViewModel.RecorderState.Idle) {
                viewModel.startRecording()
                viewModel.updateRecorderState(RecorderViewModel.RecorderState.RecordingStarted)
            }
        }
    }

    private fun initViews() {
        with(binding) {
            stopButton.setOnClickListener {
                viewModel.stopRecording()
                viewModel.updateRecorderState(RecorderViewModel.RecorderState.RecordingStopped)
            }

            if (viewModel.recorderState == RecorderViewModel.RecorderState.RecordingPaused) {
                updatePauseButtonUi(true)
            }

            pauseButton.setOnClickListener {
                val currentText = pauseButton.text.toString()
                when (currentText) {
                    getText(R.string.pause_recording) -> {
                        updatePauseButtonUi(true)
                        viewModel.pauseRecording()
                        viewModel.updateRecorderState(RecorderViewModel.RecorderState.RecordingPaused)
                    }

                    getText(R.string.resume_recording) -> {
                        updatePauseButtonUi(false)
                        viewModel.resumeRecording()
                        viewModel.updateRecorderState(RecorderViewModel.RecorderState.RecordingResumed)
                    }
                }
            }
        }
    }

    private fun initObservers() {
        recorderStateObserver()
    }

    private fun recorderStateObserver() {
        lifecycleScope.launch {
            viewModel.recorderStateFlow.collect { state ->
                when (state) {
                    AudioRecorder.RecorderState.Initialized -> Timber.i("Recorder initialized")
                    AudioRecorder.RecorderState.RecordingStarted -> Timber.i("Recording started")
                    AudioRecorder.RecorderState.Paused -> updatePauseButtonUi(true)

                    AudioRecorder.RecorderState.Resumed -> updatePauseButtonUi(false)

                    is AudioRecorder.RecorderState.RecordingStopped -> {
                        activityViewModel.saveAudioRecording(requireContext(), state.audioBytes)
                        findNavController().popBackStack()
                    }

                    AudioRecorder.RecorderState.Released -> {
                        Timber.i("Recorder released")
                    }

                    is AudioRecorder.RecorderState.Error -> {
                        Timber.e(state.message)
                    }
                }
            }
        }
    }

    private fun updatePauseButtonUi(isPaused: Boolean) {
        if (isPaused) {
            binding.lottieAnime.pauseAnimation()
            binding.pauseButton.text = getText(R.string.resume_recording)
            binding.pauseButton.icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null)
        } else {
            binding.lottieAnime.resumeAnimation()
            binding.pauseButton.text = getText(R.string.pause_recording)
            binding.pauseButton.icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        lifecycleScope.coroutineContext.cancelChildren()
    }

}
package com.black.audiorecorder.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.black.audiorecorder.R
import com.black.audiorecorder.adapters.RecordingListAdapter
import com.black.audiorecorder.data.model.RecordingItem
import com.black.audiorecorder.databinding.FragmentHomeBinding
import com.black.audiorecorder.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
    RecordingListAdapter.RecordingItemClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val listAdapter: RecordingListAdapter by lazy {
        RecordingListAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        initViews()
        initObservers()
        activityViewModel.getAudioRecordings(requireContext())
    }

    private fun initViews() {
        with(binding) {
            startRecordingButton.setOnClickListener { checkAudioPermission() }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = listAdapter
                setHasFixedSize(true)
            }
        }
    }

    private fun initObservers() {
        uiObserver()
        recordingsObserver()
    }


    private fun playItem(item: RecordingItem) {
        val file = File(item.path)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/wav")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            // Handle the case where no activity can handle the intent
            AlertDialog.Builder(requireContext())
                .setTitle("No Application Found")
                .setMessage("No application available to play this audio file.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun uiObserver() {
        lifecycleScope.launch {
            activityViewModel.uiStateFlow.collectLatest {
//                when (it) {
//                    is MainViewModel.UiState.Loading -> {
//                        binding.progressBar.visibility = View.VISIBLE
//                    }
//
//                    is MainViewModel.UiState.Success -> {
//                        binding.progressBar.visibility = View.GONE
//                    }
//                }
            }
        }
    }

    private fun recordingsObserver() {
        lifecycleScope.launch {
            activityViewModel.recordingsListFlow.collectLatest {
                when (it) {
                    is MainViewModel.RecordingFileState.NewRecordingAvailable -> {
                        activityViewModel.getAudioRecordings(requireContext())
                    }

                    is MainViewModel.RecordingFileState.Success -> {
                        listAdapter.submitList(it.recordings)
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            navigateToRecorder()
        } else {
            showPermissionDeniedDialog()
        }
    }

    private fun checkAudioPermission() {
        when {
            checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                navigateToRecorder()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                showPermissionDeniedDialog()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Audio recording permission is required to use this feature. Please enable it in the app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToRecorder() {
        findNavController().navigate(HomeFragmentDirections.actionHomeToRecorder())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRecordingItemClicked(recordingItem: RecordingItem) {
        playItem(recordingItem)
    }

}
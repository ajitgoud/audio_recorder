package com.black.audiorecorder.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.black.audiorecorder.R
import com.black.audiorecorder.adapters.RecordingListAdapter
import com.black.audiorecorder.data.model.RecordingItem
import com.black.audiorecorder.databinding.FragmentHomeBinding
import com.black.audiorecorder.ui.MainActivity
import com.black.audiorecorder.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
    RecordingListAdapter.RecordingItemClickListener {

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val listAdapter: RecordingListAdapter by lazy {
        RecordingListAdapter(this, viewModel.recordingList.isNotEmpty())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        Timber.d("HomeFragment onViewCreated")
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
        addOptionMenu()
    }


    private fun addOptionMenu() {
        val activity = requireActivity() as MainActivity? ?: return
        activity.binding.toolbar.addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    private fun removeOptionMenu() {
        val activity = requireActivity() as MainActivity? ?: return
        activity.binding.toolbar.removeMenuProvider(menuProvider)
    }

    private fun toggleMenuItems(isSelectionMode: Boolean) {
        val activity = requireActivity() as MainActivity? ?: return
        val menu = activity.binding.toolbar.menu ?: return
        Timber.d("MENU_CHECK toggleMenuItems: $isSelectionMode")
        menu.findItem(R.id.action_delete).isVisible = isSelectionMode
        menu.findItem(R.id.action_cancel).isVisible = isSelectionMode
        menu.findItem(R.id.action_settings).isVisible = !isSelectionMode
    }

    private val menuProvider = object : MenuProvider {
        override fun onPrepareMenu(menu: Menu) {
            super.onPrepareMenu(menu)
        }

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.home_menu, menu)
            val isSelectionMode = viewModel.recordingList.isNotEmpty()
            Timber.d("MENU_CHECK menuProvider: $isSelectionMode")
            menu.findItem(R.id.action_delete).isVisible = isSelectionMode
            menu.findItem(R.id.action_cancel).isVisible = isSelectionMode
            menu.findItem(R.id.action_settings).isVisible = !isSelectionMode
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_delete -> {
                    activityViewModel.deleteRecordings(viewModel.recordingList)
                    viewModel.clearRecordingList()
                    true
                }

                R.id.action_cancel -> {
                    viewModel.clearRecordingList()
                    true
                }

                R.id.action_settings -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeToSettings())
                    true
                }

                else -> false
            }
        }
    }

    private fun initObservers() {
        recordingsObserver()
        recordingItemSelectionObserver()
    }

    private fun recordingItemSelectionObserver() {
        lifecycleScope.launch {
            viewModel.isRecordingItemSelected.collectLatest { isSelectionMode ->
                if (isSelectionMode) {
                    binding.startRecordingButton.visibility = View.GONE
                    toggleMenuItems(true)
                } else {
                    binding.startRecordingButton.visibility = View.VISIBLE
                    listAdapter.clearSelectionMode()
                }
                toggleMenuItems(isSelectionMode)
            }
        }
    }


    private fun playItem(item: RecordingItem) {
        val file = File(item.path)
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.provider", file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/wav")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            // Handle the case where no activity can handle the intent
            AlertDialog.Builder(requireContext()).setTitle("No Application Found")
                .setMessage("No application available to play this audio file.")
                .setPositiveButton("OK", null).show()
        }
    }

    private fun recordingsObserver() {
        lifecycleScope.launch {
            activityViewModel.recordingsListFlow.collectLatest { state ->
                when (state) {
                    is MainViewModel.RecordingFileState.NewRecordingAvailable -> {
                        activityViewModel.getAudioRecordings(requireContext())
                    }

                    is MainViewModel.RecordingFileState.Success -> {
                        val newList = state.recordings
                        if (newList.isEmpty()) {
                            binding.emptyRecordingGroup.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            binding.emptyRecordingGroup.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                            if (viewModel.recordingList.isNotEmpty()) {
                                val selectedItems = viewModel.recordingList
                                newList.forEach { recording ->
                                    if (selectedItems.any { it.path == recording.path }) {
                                        recording.isSelected = true
                                    }
                                }
                            }
                            listAdapter.submitList(newList)
                        }
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
                requireContext(), Manifest.permission.RECORD_AUDIO
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
        AlertDialog.Builder(requireContext()).setTitle("Permission Required")
            .setMessage("Audio recording permission is required to use this feature. Please enable it in the app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }.setNegativeButton("Cancel", null).show()
    }

    private fun navigateToRecorder() {
        findNavController().navigate(HomeFragmentDirections.actionHomeToRecorder())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeOptionMenu()
        _binding = null
        lifecycleScope.coroutineContext.cancelChildren()
    }

    override fun onRecordingItemClicked(recordingItem: RecordingItem) {
        playItem(recordingItem)
    }

    override fun onRecordingItemSelectionChanged(recordingItem: RecordingItem) {
        if (recordingItem.isSelected) {
            viewModel.addRecording(recordingItem)
        } else {
            viewModel.removeRecording(recordingItem)
        }
    }

    override fun onRecordingItemLongClicked(recordingItem: RecordingItem) {
        viewModel.addRecording(recordingItem)
    }

}
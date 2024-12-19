package com.black.audiorecorder.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.black.audiorecorder.R
import com.black.audiorecorder.databinding.FragmentBottomSheetSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsBottomSheet : BottomSheetDialogFragment(R.layout.fragment_bottom_sheet_settings) {
    private var _binding: FragmentBottomSheetSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        _binding = FragmentBottomSheetSettingsBinding.bind(view)

    }
}
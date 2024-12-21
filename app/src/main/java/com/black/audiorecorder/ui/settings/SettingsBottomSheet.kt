package com.black.audiorecorder.ui.settings

import android.media.AudioFormat
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.black.audiorecorder.R
import com.black.audiorecorder.databinding.FragmentBottomSheetSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsBottomSheet : BottomSheetDialogFragment(R.layout.fragment_bottom_sheet_settings) {
    private var _binding: FragmentBottomSheetSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private val sampleRatesAdapter: ArrayAdapter<Int> by lazy {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            viewModel.getSampleRates()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBottomSheetSettingsBinding.bind(view)
        initViews()
    }

    private fun initViews() {
        with(binding) {
            sampleRateEditText.setText(viewModel.audioSampleRate.toString(), false)
            sampleRateEditText.setAdapter(sampleRatesAdapter)

            if (viewModel.audioChannel == AudioFormat.CHANNEL_IN_MONO) {
                monoChannelButton.isChecked = true
            } else {
                stereoChannelButton.isChecked = true
            }

            updateSettingsButton.setOnClickListener {
                updateSettings()
            }

        }
    }

    private fun updateSettings() {
        with(binding) {
            val sampleRate = sampleRateEditText.text.toString().toInt()
            val channel =
                if (monoChannelButton.isChecked) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO

            if (sampleRate == viewModel.audioSampleRate && channel == viewModel.audioChannel) {
                findNavController().popBackStack()
            } else {
                viewModel.updateSettings(sampleRate, channel)
                findNavController().popBackStack()
            }
        }
    }
}
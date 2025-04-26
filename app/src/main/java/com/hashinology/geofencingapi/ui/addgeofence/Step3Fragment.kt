package com.hashinology.geofencingapi.ui.addgeofence

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.databinding.FragmentStep3Binding
import com.hashinology.geofencingapi.viewmodels.SharedViewModel


class Step3Fragment : Fragment() {
    private var _binding: FragmentStep3Binding? = null
    private val binding get() = _binding!!

    private val sharedVM: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStep3Binding.inflate(inflater, container, false)
        binding.sharedViewModel = sharedVM

        binding.step3Back.setOnClickListener {
            sharedVM.geoRadius = binding.slider.value
            sharedVM.geofenceReady = true
            findNavController().navigate(R.id.action_step3Fragment_to_step2Fragment)
        }

        binding.step3Done.setOnClickListener {
            sharedVM.geofenceReady = true
            findNavController().navigate(R.id.action_step3Fragment_to_mapsFragment)
            Log.d("Step3Fragment", sharedVM.geoRadius.toString())
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.hashinology.geofencingapi.ui.geofence

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.adapters.GeofencesAdapter
import com.hashinology.geofencingapi.databinding.FragmentGeofencesBinding
import com.hashinology.geofencingapi.viewmodels.SharedViewModel

class GeofencesFragment : Fragment() {

    private var _binding: FragmentGeofencesBinding? = null
    private val binding: FragmentGeofencesBinding get() = _binding!!

    private val sharedVM: SharedViewModel by activityViewModels()
    private val geofencesAdapter by lazy { GeofencesAdapter(sharedVM) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGeofencesBinding.inflate(layoutInflater, container, false)

        binding.sharedViewModel = sharedVM
        setupToolbar()
        setupRecyclerView()
        observeDatabase()
        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            /*// Old Deprecated
            requireActivity().onBackPressed()*/
            // Latest Back
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.geofencesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = geofencesAdapter
        }
    }

    private fun observeDatabase() {
        sharedVM.readGeofences.observe(viewLifecycleOwner, {
            geofencesAdapter.setData(it)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
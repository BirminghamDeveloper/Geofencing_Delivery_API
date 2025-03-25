package com.hashinology.geofencingapi.ui.addgeofence

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.databinding.FragmentStep1Binding
import com.hashinology.geofencingapi.viewmodels.SharedViewModel
import kotlinx.coroutines.launch


class Step1Fragment : Fragment() {
    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!

    private val sharedVM: SharedViewModel by activityViewModels()

    private lateinit var geoCoder: Geocoder
    private lateinit var placeClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_API_key))
        placeClient = Places.createClient(requireContext())
        geoCoder = Geocoder(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStep1Binding.inflate(inflater, container, false)

        binding.step1Back.setOnClickListener {
            onStep1BackClicked()
        }

        getCountryCodeFromCurrentLocation()

        return binding.root
    }

    private fun onStep1BackClicked() {
        findNavController().navigate(R.id.action_step1Fragment_to_permissionFragment)
    }

    @SuppressLint("MissingPermission")
    private fun getCountryCodeFromCurrentLocation() {
        lifecycleScope.launch {
            val placeField = listOf(Place.Field.LAT_LNG)
            val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeField)

            val placeResponse = placeClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val reposnse = task.result
                    val latlng = reposnse.placeLikelihoods[0].place.latLng!!
                    val address = geoCoder.getFromLocation(
                        latlng.latitude,
                        latlng.longitude,
                        1
                    )
                    sharedVM.geoCountryCode = address!![0].countryCode
                }else{
                    val exception = task.exception
                    if (exception is ApiException){
                        Log.e("Step1Fragment", "Exception: ${exception.statusCode.toString()}", )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
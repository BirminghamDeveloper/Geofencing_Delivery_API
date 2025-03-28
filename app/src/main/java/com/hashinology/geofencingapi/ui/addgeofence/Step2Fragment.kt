package com.hashinology.geofencingapi.ui.addgeofence

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.adapters.PredictionsAdapter
import com.hashinology.geofencingapi.databinding.FragmentStep2Binding
import com.hashinology.geofencingapi.util.ExtensionFunctions.hide
import com.hashinology.geofencingapi.viewmodels.SharedViewModel
import com.hashinology.geofencingapi.viewmodels.Step2ViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Step2Fragment : Fragment() {
    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!

    private val predictionAdapter by lazy { PredictionsAdapter() }

    private val sharedVM: SharedViewModel by activityViewModels()
    private val step2VML: Step2ViewModel by viewModels()

    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(requireContext(), getString(R.string.google_maps_API_key))
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStep2Binding.inflate(inflater, container, false)

        binding.predictionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = predictionAdapter
        }

        binding.geofenceLocationEt.doOnTextChanged { text, _, _, _ ->
            handleNextButton(text)
            getPlaces(text)
        }

        binding.step2Back.setOnClickListener {
            findNavController().navigate(R.id.action_step2Fragment_to_step1Fragment)
        }

        binding.step2Next.setOnClickListener {
            findNavController().navigate(R.id.action_step2Fragment_to_step3Fragment)
        }

        subscribeToObservers()

        return binding.root
    }

    private fun handleNextButton(text: CharSequence?) {
        if(text.isNullOrEmpty()){
            step2VML.enabledNextButton(false)
        }
    }

    private fun subscribeToObservers() {
        lifecycleScope.launch {
            predictionAdapter.placeId.collectLatest { placeID ->
                if(placeID.isNotEmpty()){
                    onCitySelected(placeID)
                }
            }
        }
    }

    private fun onCitySelected(placeID: String) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.DISPLAY_NAME
        )

        val request = FetchPlaceRequest.builder(
            placeID,
            placeFields
        ).build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { reposne ->
                sharedVM.geoLatLng = reposne.place.latLng!!
                sharedVM.geoLocationName = reposne.place.displayName!!
                sharedVM.geoCitySelected = true

                binding.geofenceLocationEt.setText(sharedVM.geoLocationName)
                binding.geofenceLocationEt.setSelection(sharedVM.geoLocationName.length)
                binding.predictionsRecyclerView.hide()
                step2VML.enabledNextButton(true)

                Log.d("Step2fragment", sharedVM.geoLatLng.toString())
                Log.d("Step2fragment", sharedVM.geoLocationName)
                Log.d("Step2fragment", sharedVM.geoCitySelected.toString())
            }
            .addOnFailureListener { exception ->
                Log.e("Step2fragment", exception.message.toString() )
            }
    }

    private fun getPlaces(text: CharSequence?) {
        if (sharedVM.checkDeviceLocationSetting(requireContext())){
            lifecycleScope.launch {
                if (text.isNullOrEmpty()){
                    predictionAdapter.setData(emptyList())
                }else{
                    val token = AutocompleteSessionToken.newInstance()

                    val request = FindAutocompletePredictionsRequest.builder()
                        .setCountries(sharedVM.geoCountryCode)
                        // .setTypeFilter is Deprecated
                        .setTypeFilter(TypeFilter.CITIES)
                        .setSessionToken(token)
                        .setQuery(text.toString())
                        .build()
                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            predictionAdapter.setData(response.autocompletePredictions)
                        }
                        .addOnFailureListener { exception: Exception? ->
                            if (exception is ApiException) {
                                Log.e("Step2Fragment", exception.statusCode.toString() )
                            }
                        }
                }
            }
        }else{
            Toast.makeText(requireContext(), "Please Enable Location Settings.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.hashinology.geofencingapi.ui.maps

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.databinding.FragmentMapsBinding
import com.hashinology.geofencingapi.util.ExtensionFunctions.disable
import com.hashinology.geofencingapi.util.ExtensionFunctions.enable
import com.hashinology.geofencingapi.util.ExtensionFunctions.hide
import com.hashinology.geofencingapi.util.ExtensionFunctions.show
import com.hashinology.geofencingapi.util.Permissions.hasBackgroundLocationPermission
import com.hashinology.geofencingapi.util.Permissions.requestBackgroundLocationPermission
import com.hashinology.geofencingapi.viewmodels.SharedViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    EasyPermissions.PermissionCallbacks, GoogleMap.SnapshotReadyCallback {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val shareedVM: SharedViewModel by activityViewModels()

    private lateinit var map: GoogleMap
    private lateinit var circle: Circle

    private val args by navArgs<MapsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        binding.addGeofenceFab.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_add_geofence_graph)
        }

        binding.geofencesFab.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_geofencesFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap!!
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle))
        map.isMyLocationEnabled = true
        map.setOnMapLongClickListener(this)
        map.uiSettings.apply {
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = true
        }
        onGeofenceReady()
        observeDatabase()
        backFromGeofencesFragment()
    }

    private fun onGeofenceReady() {
        if (shareedVM.geofenceReady){
            shareedVM.geofenceReady = false
            shareedVM.geofencePrepared = true
            displayInfoMessage()
            zoomToSelectedLocation()
        }
    }

    private fun displayInfoMessage() {
        lifecycleScope.launch {
            binding.infoMessageTextView.show()
            delay(2000)
            binding.infoMessageTextView
                .animate()
                .alpha(0f)
                .duration = 800
            delay(1000)
            binding.infoMessageTextView.hide()
        }
    }

    private fun zoomToSelectedLocation() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(shareedVM.geoLatLng, 10f),
            2000,
            null
        )
    }

    private fun observeDatabase() {
        shareedVM.readGeofences.observe(viewLifecycleOwner, { geofenceEntity ->
            map.clear()
            geofenceEntity.forEach{ geofence ->
                drawCircle(LatLng(geofence.latitude, geofence.longitude), geofence.radius)
                drawMarker(LatLng(geofence.latitude, geofence.longitude), geofence.name)
            }
        })
    }

    private fun backFromGeofencesFragment() {
        if (args.geofenceEntity != null){
            val selectedGeofence = LatLng(
                args.geofenceEntity!!.latitude,
                args.geofenceEntity!!.longitude
            )
            zoomToGeofence(selectedGeofence, args.geofenceEntity!!.radius)
        }
    }

    override fun onMapLongClick(location: LatLng) {
        if (hasBackgroundLocationPermission(requireContext())){
            if (shareedVM.geofencePrepared){
                setupGeofence(location)
            }else{
                Toast.makeText(requireContext(), "You Need to create a new Geofence First..", Toast.LENGTH_SHORT).show()
            }
        }else{
            requestBackgroundLocationPermission(this)
        }
    }

    private fun setupGeofence(location: LatLng) {
        lifecycleScope.launch { 
            if (shareedVM.checkDeviceLocationSetting(requireContext())){
                binding.addGeofenceFab.disable()
                binding.addGeofenceFab.disable()
                binding.geofenceProgressBar.show()

                drawCircle(location, shareedVM.geoRadius)
                drawMarker(location, shareedVM.geoName)
                zoomToGeofence(circle.center, circle.radius.toFloat())

                delay(1500)
                map.snapshot(this@MapsFragment)
                delay(5000)
                if(shareedVM.geoSnapshot != null){
                    shareedVM.addGeofenceToDatabase(location)
                }
                delay(2000)
                shareedVM.startGeofence(location.latitude, location.longitude)
                shareedVM.resetSharedValue()

                binding.addGeofenceFab.enable()
                binding.addGeofenceFab.enable()
                binding.geofenceProgressBar.hide()
            }else{
                Toast.makeText(requireContext(), "Please Enable Location Settings..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun drawCircle(location: LatLng, radius: Float) {
        circle = map.addCircle(
            CircleOptions()
                .center(location)
                .radius(radius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.blue_700))
        )
    }

    private fun drawMarker(location: LatLng, name: String) {
        map.addMarker(
            MarkerOptions().position(location).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    private fun zoomToGeofence(center: LatLng, radius: Float) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                shareedVM.getBoounds(center, radius), 10
            ), 1000, null
        )
    }

    override fun onSnapshotReady(snapshot: Bitmap?) {
        shareedVM.geoSnapshot = snapshot
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionDenied(this, perms[0])){
            SettingsDialog.Builder(requireActivity()).build().show()
        }else{
            requestBackgroundLocationPermission(this)
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onGeofenceReady()
        Toast.makeText(requireContext(), "Permission Granted! Long Press on the Map to add a Geofence.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
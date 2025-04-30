package com.hashinology.geofencingapi.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.hashinology.geofencingapi.data.DataStoreRepository
import com.hashinology.geofencingapi.data.GeofenceEntity
import com.hashinology.geofencingapi.data.GeofenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val dataStoreRepo: DataStoreRepository,
    private val geofenceRepository: GeofenceRepository
): AndroidViewModel(application) {
    val app = application

    var geoID: Long = 0L
    var geoName: String = "Default"
    var geoCountryCode: String = ""
    var geoLocationName: String = "Search a City"
    var geoLatLng = LatLng(0.0, 0.0)

    var geoRadius: Float = 500f
    var geoSnapshot: Bitmap? = null

    var geoCitySelected = false
    var geofenceReady = false
    var geofencePrepared = false


    // DataStore to create readfirstlaunch variable and converted from Flow to liveData
    val readFirstLaunch = dataStoreRepo.readfirstLaunch.asLiveData()

    fun saveFirstLaunch(firstLaunch: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.saveFirstLaunch(firstLaunch)
        }
    }
    // Database
    val readGeofences = geofenceRepository.readGeofences.asLiveData()
    fun addGeofence(geofenceEntity: GeofenceEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.addGeofence(geofenceEntity)
        }
    fun removeGeofence(geofenceEntity: GeofenceEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.removeGeofence(geofenceEntity)
        }

    fun addGeofenceToDatabase(location: LatLng){
        val geofenceEntity = GeofenceEntity(
            geoID,
            geoName,
            geoLocationName,
            location.latitude,
            location.longitude,
            geoRadius,
            geoSnapshot!!
        )
        addGeofence(geofenceEntity)
    }

    fun getBoounds(center: LatLng, radius: Float): LatLngBounds{
        val distanceFromCenterToCorner = radius * sqrt(2.0)
        val southEestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northEastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(southEestCorner, northEastCorner)
    }

    fun checkDeviceLocationSetting(context: Context): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            /* // Long way to create Locationmanager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager*/
            // Easy Way to Create the LocationManager
            val locationManager = context.getSystemService(LocationManager::class.java)
            locationManager.isLocationEnabled
        }else{
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }
}
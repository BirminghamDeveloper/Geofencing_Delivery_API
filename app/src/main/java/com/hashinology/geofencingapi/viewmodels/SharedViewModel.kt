package com.hashinology.geofencingapi.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.hashinology.geofencingapi.broadcastreceiver.GeofenceBroadcastReceiver
import com.hashinology.geofencingapi.data.DataStoreRepository
import com.hashinology.geofencingapi.data.GeofenceEntity
import com.hashinology.geofencingapi.data.GeofenceRepository
import com.hashinology.geofencingapi.util.Permissions
import kotlinx.coroutines.CompletableDeferred
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
    private var geofencingClient = LocationServices.getGeofencingClient(app.applicationContext)

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
//    var geofenceRemoved = false

    fun resetSharedValue(){
        geoID = 0L
        geoName = "Default"
        geoCountryCode = ""
        geoLocationName = "Search a City"
        geoLatLng = LatLng(0.0,0.0)
        geoRadius = 500f
        geoSnapshot = null
        geoCitySelected = false
        geofenceReady = false
        geofencePrepared = false
//        geofenceRemoved = false
    }

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

    suspend fun stopGeofence(geoIDs: List<Long>): Boolean{
        return if(Permissions.hasBackgroundLocationPermission(app)){
            val result = CompletableDeferred<Boolean>()
            geofencingClient.removeGeofences(setPendingIntent(geoIDs.first().toInt()))
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        result.complete(true)
                    }else{
                        result.complete(false)
                    }
                }
            result.await()
        }else{
            return false
        }
    }

    private fun setPendingIntent(geoID: Int): PendingIntent{
        val intent = Intent(app, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            app,
            geoID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun startGeofence(
        latitude: Double,
        longitude: Double
    ){
        if (Permissions.hasBackgroundLocationPermission(app)){
            val geofence = Geofence.Builder()
                .setRequestId(geoID.toString())
                .setCircularRegion(
                    latitude,
                    longitude,
                    geoRadius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_EXIT or
                    Geofence.GEOFENCE_TRANSITION_DWELL
                )
                .setLoiteringDelay(5000)
                .build()
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(
                            Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT or
                            Geofence.GEOFENCE_TRANSITION_DWELL
                )
                .addGeofence(geofence)
                .build()
            geofencingClient.addGeofences(geofencingRequest, setPendingIntent(geoID.toInt())).run {
                addOnSuccessListener {
                    Log.d("Geofence", "Successfully added: ")
                }
                addOnFailureListener {
                    Log.d("Geofence", it.message.toString())
                }
            }
        }else{
            Log.d("Geofence", "Permission not Granted: ")
        }
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
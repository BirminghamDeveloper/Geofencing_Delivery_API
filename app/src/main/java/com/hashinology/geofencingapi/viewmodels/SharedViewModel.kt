package com.hashinology.geofencingapi.viewmodels

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hashinology.geofencingapi.data.DataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val dataStoreRepo: DataStoreRepository
): AndroidViewModel(application) {
    val app = application

    var geoID = 0L
    var geoName = "Default"
    var geoCountryCode = ""

    // DataStore to create readfirstlaunch variable and converted from Flow to liveData
    val readFirstLaunch = dataStoreRepo.readfirstLaunch.asLiveData()

    fun saveFirstLaunch(firstLaunch: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.saveFirstLaunch(firstLaunch)
        }
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
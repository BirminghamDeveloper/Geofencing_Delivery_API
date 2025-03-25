package com.hashinology.geofencingapi.viewmodels

import android.app.Application
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

    var geoName = "Default"
    var geoCountryCode = ""

    // DataStore to create readfirstlaunch variable and converted from Flow to liveData
    val readFirstLaunch = dataStoreRepo.readfirstLaunch.asLiveData()

    fun saveFirstLaunch(firstLaunch: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.saveFirstLaunch(firstLaunch)
        }
    }
}
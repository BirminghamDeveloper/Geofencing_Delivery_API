package com.hashinology.geofencingapi.viewmodels

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import com.hashinology.geofencingapi.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.last
import javax.inject.Inject
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val dataStoreRepo: DataStoreRepository
): AndroidViewModel(application) {
    val app = application

    // DataStore
    val readFirstLaunch = dataStoreRepo.readfirstLaunch.asLiveData()

    fun saveFirstLaunch(firstLaunch: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.saveFirstLaunch(firstLaunch)
        }
    }
}
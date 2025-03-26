package com.hashinology.geofencingapi.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Step1ViewModel: ViewModel() {
    private val _nextbuttonEnabled = MutableLiveData<Boolean>(false)
    val nextButtonEnabled : LiveData<Boolean> get() = _nextbuttonEnabled

    fun enableNextButton(enable: Boolean){
        _nextbuttonEnabled.value = enable
    }
}
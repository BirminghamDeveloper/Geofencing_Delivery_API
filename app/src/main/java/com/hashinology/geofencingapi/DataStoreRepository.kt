package com.hashinology.geofencingapi

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.hashinology.geofencingapi.util.Constants.PREFERENCE_NAME

private val Context.dataStore by preferencesDataStore(PREFERENCE_NAME)

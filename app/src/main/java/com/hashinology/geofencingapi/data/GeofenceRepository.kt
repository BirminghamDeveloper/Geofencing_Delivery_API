package com.hashinology.geofencingapi.data

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GeofenceRepository @Inject constructor(private val geofenceDao: GeofenceDao) {

    val readGeofences: Flow<MutableList<GeofenceEntity>> = geofenceDao.readGeoFences()

    suspend fun addGeofence(geofenceEntity: GeofenceEntity){
        geofenceDao.addGeofence(geofenceEntity)
    }

    suspend fun removeGeofence(geofenceEntity: GeofenceEntity){
        geofenceDao.removeGeofence(geofenceEntity)
    }
}
package com.hashinology.geofencingapi.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.util.Permissions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.
            findFragmentById(R.id.navHostFragment) as NavHostFragment

        val navController = navHostFragment.navController
        if (Permissions.hasLocationPermission(this)){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
        }
    }
}
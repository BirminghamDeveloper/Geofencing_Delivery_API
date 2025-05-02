package com.hashinology.geofencingapi.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.util.Permissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ granted -> 
        if (granted){
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Permission Denied: ", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestNotificationPermission()
        }

        val navHostFragment = supportFragmentManager.
            findFragmentById(R.id.navHostFragment) as NavHostFragment

        val navController = navHostFragment.navController
        if (Permissions.hasLocationPermission(this)){
            navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission(){
        when{
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "Permission Already Granted", Toast.LENGTH_SHORT).show()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Toast.makeText(this, "Permission Rational Needed", Toast.LENGTH_SHORT).show()
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS).not() -> {
                SettingsDialog.Builder(this).build().show()
            }
            else -> {
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
package com.hashinology.geofencingapi.broadcastreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.hashinology.geofencingapi.R
import com.hashinology.geofencingapi.util.Constants.NOTIFICATION_CHANNEL_ID
import com.hashinology.geofencingapi.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.hashinology.geofencingapi.util.Constants.NOTIFICATION_ID

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        if (geofenceEvent!!.hasError()){
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofenceEvent.errorCode)
            Log.e("BroadcastReceiver", errorMessage )
            return
        }
        when(geofenceEvent.geofenceTransition){
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("BroadcastReceiver", "Geofence: Entered")
                displayNotification(context, "Geofence Entered")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT ->{
                Log.d("BroadcastReceiver", "Geofence: Exit")
                displayNotification(context, "Geofence Exited")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL ->{
                Log.d("BroadcastReceiver", "Geofence: Dwelled")
                displayNotification(context, "Geofence Dwelled")
            }
            else ->{
                Log.d("BroadcastReceiver", "Invalid Type")
                displayNotification(context, "Geofence Invalid Type")
            }
        }
    }
    private fun displayNotification(context: Context, geofenceTransition: String){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Geofence")
            .setContentText(geofenceTransition)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
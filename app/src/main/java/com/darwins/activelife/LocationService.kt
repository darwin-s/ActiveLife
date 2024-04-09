package com.darwins.activelife

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class LocationService : Service() {
    private lateinit var destination: LatLng
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                destination = LatLng(intent.getDoubleExtra("LAT", 0.0),
                    intent.getDoubleExtra("LON", 0.0))
                start()
            }
            "STOP" -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun start() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("STOP_TRIP", true)
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Walking towards your destination")
            .setContentText("Press to cancel your trip")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest.Builder(10000L).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                val loc = location.lastLocation
                if (loc != null) {
                    val dst = Location(loc)
                    dst.latitude = destination.latitude
                    dst.longitude = destination.longitude
                    val distance = loc.distanceTo(dst)

                    if (distance < 10.0) {
                        intent.putExtra("DST_REACH", true)
                        pendingIntent = PendingIntent.getActivity(this@LocationService, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        val updatedNotification = notification.setContentTitle("Destination reached!")
                            .setContentText("Press to finish the trip")
                            .setContentIntent(pendingIntent)

                        notificationManager.notify(1, updatedNotification.build())
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        startForeground(1, notification.build())
    }

    private fun stop() {
        fusedClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}
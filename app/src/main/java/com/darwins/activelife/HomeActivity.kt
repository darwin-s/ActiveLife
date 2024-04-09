package com.darwins.activelife

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERM_REQ = 1
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gMap: GoogleMap
    private lateinit var fusedClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val logoutButton = findViewById<Button>(R.id.logout)

        logoutButton.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                firebaseAuth.signOut()
            }

            setResult(RESULT_OK)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            gMap.isMyLocationEnabled = true
            return
        }

        ActivityCompat.requestPermissions(this, arrayOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"), PERM_REQ)
    }

    override fun onMapReady(gmap: GoogleMap) {
        gMap = gmap
        enableMyLocation()

        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
            fusedClient.lastLocation.addOnSuccessListener {
                val loc = LatLng(it.latitude, it.longitude)
                gMap.animateCamera(CameraUpdateFactory.newLatLng(loc))
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != PERM_REQ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
           if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
               && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
               enableMyLocation()
           } else {
               Toast.makeText(this, "The application cannot run without location permissions!", Toast.LENGTH_LONG).show()
           }
        }
    }
}
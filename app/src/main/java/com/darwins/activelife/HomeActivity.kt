package com.darwins.activelife

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.darwins.activelife.api.Road
import com.darwins.activelife.api.RoadsApi
import com.darwins.activelife.api.RoadsInterface
import com.darwins.activelife.databinding.ActivityHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Random
import kotlin.math.min

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERM_REQ = 1
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var gMap: GoogleMap
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var binding: ActivityHomeBinding
    private var currentMarker: Marker? = null
    private var lastDestination: LatLng? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.main, R.string.open_nav, R.string.close_nav)
        binding.main.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navmenu.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings_nav -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.logout_nav -> {
                    if (firebaseAuth.currentUser != null) {
                        firebaseAuth.signOut()
                    }

                    setResult(RESULT_OK)

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        val sharedPref = getSharedPreferences("ActiveLife", Context.MODE_PRIVATE)
        if (!sharedPref.contains("METERS_MAX")) {
            with(sharedPref.edit()) {
                putInt("METERS_MAX", 500)
                apply()
            }
        }

        binding.findLoc.setOnClickListener {
            val minDist = min(10, sharedPref.getInt("METERS_MAX", 10))
            val maxDist = sharedPref.getInt("METERS_MAX", 500)
            generateRandomCord(minDist, maxDist)
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("CURRENT_LAT")
                && savedInstanceState.containsKey("CURRENT_LON")) {
                lastDestination = LatLng(savedInstanceState.getDouble("CURRENT_LAT"),
                    savedInstanceState.getDouble("CURRENT_LON"))
            }
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        binding.startTrip.setOnClickListener {
            if (currentMarker == null) {
                Toast.makeText(this, "Please choose a destination first!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, LocationService::class.java)
            intent.action = "START"
            currentMarker?.position?.let { it1 -> intent.putExtra("LAT", it1.latitude) }
            currentMarker?.position?.let { it1 -> intent.putExtra("LON", it1.longitude) }
            startService(intent)
        }

        if (intent.getBooleanExtra("STOP_TRIP", false)) {
            if (intent.getBooleanExtra("DST_REACH", false)) {
                Toast.makeText(this, "Congratulations!", Toast.LENGTH_LONG).show()
            }

            val intent = Intent(this, LocationService::class.java)
            intent.action = "STOP"
            startService(intent)

            val newIntent = Intent(this, HomeActivity::class.java)
            startActivity(newIntent)
        }

        if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 0)
        }
    }

    private fun generateRandomCord(min: Int, max: Int) {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedClient.lastLocation.addOnSuccessListener {
            val meterCord = 0.00900900900901 / 1000
            val rand = Random()

            val randomMeter = rand.nextInt(max + min)
            val method = rand.nextInt(6)
            val metersCord = meterCord * randomMeter.toDouble()

            var loc = when (method) {
                0 -> {
                    LatLng(it.latitude + metersCord, it.longitude + metersCord)
                }
                1 -> {
                    LatLng(it.latitude - metersCord, it.longitude - metersCord)
                }
                2 -> {
                    LatLng(it.latitude + metersCord, it.longitude - metersCord)
                }
                3 -> {
                    LatLng(it.latitude - metersCord, it.longitude + metersCord)
                }
                4 -> {
                    LatLng(it.latitude, it.longitude - metersCord)
                }
                else -> {
                    LatLng(it.latitude - metersCord, it.longitude)
                }
            }

            val roadApi = RoadsApi().instance.create(RoadsInterface::class.java)
            val pts = loc.latitude.toString() + "," + loc.longitude.toString()
            val call = roadApi.nearestRoad(pts, BuildConfig.MAPS_API_KEY)
            call.enqueue(object : Callback<Road> {
                override fun onResponse(call: Call<Road>, response: Response<Road>) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        if (body?.snappedPoints != null && body.snappedPoints.isNotEmpty()) {
                            loc = LatLng(body.snappedPoints[0].location.latitude.toDouble(),
                                body.snappedPoints[0].location.longitude.toDouble())
                        }
                    } else {
                        Toast.makeText(this@HomeActivity, "Failed to access road API. Please try again", Toast.LENGTH_SHORT).show()
                    }
                    currentMarker?.remove()
                    currentMarker = gMap.addMarker(MarkerOptions().position(loc))
                }

                override fun onFailure(call: Call<Road>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Failed to access road API. Please try again", Toast.LENGTH_SHORT).show()
                }
            })
        }
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

        if (lastDestination != null) {
            currentMarker?.remove()
            currentMarker = gMap.addMarker(MarkerOptions().position(lastDestination!!))
            lastDestination = null
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        currentMarker?.position?.let { outState.putDouble("CURRENT_LAT", it.latitude) }
        currentMarker?.position?.let { outState.putDouble("CURRENT_LON", it.longitude) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}